package co.yodo.launcher.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.ConnectionResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;

import co.yodo.launcher.R;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.data.Currency;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.Intents;
import co.yodo.launcher.helper.PrefsUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.manager.PromotionManager;
import co.yodo.launcher.scanner.QRScanner;
import co.yodo.launcher.scanner.QRScannerFactory;
import co.yodo.launcher.service.LocationService;
import co.yodo.launcher.ui.adapter.CurrencyAdapter;
import co.yodo.launcher.ui.component.ClearEditText;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.builder.ServerRequest;
import co.yodo.restapi.network.handler.XMLHandler;
import co.yodo.restapi.network.model.ServerResponse;

public class LauncherActivity extends AppCompatActivity implements
        PromotionManager.IPromotionListener,
        YodoRequest.RESTListener,
        QRScanner.QRScannerListener {
    /** DEBUG */
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String hardwareToken;

    /** Gui controllers */
    private LinearLayout mRootLayout;
    private SlidingPaneLayout mSlidingLayout;
    private TextView mBalanceView;
    private Spinner mScannersSpinner;
    private TextView mTotalView;
    private TextView mCashTenderView;
    private TextView mCashBackView;
    private NetworkImageView mAvatarImage;
    private ImageView mPaymentButton;
    private ProgressBar mBalanceBar;

    /** Popup Window for Tips */
    private PopupWindow mPopupMessage;
    private BigDecimal equivalentTender;

    /** Selected Text View */
    private TextView selectedView;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager mPromotionManager;

    /** Balance Temp */
    private HashMap<String, String> historyData = null;
    private HashMap<String, String> todayData = null;

    /** Current Scanners */
    private QRScannerFactory mScannerFactory;
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** Location */
    private Location mLocation;

    /** Code for the error dialog */
    private static final int REQUEST_CODE_LOCATION_SERVICES = 0;
    private static final int REQUEST_SETTINGS               = 1;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA   = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    /** Request code to use when launching the resolution activity */
    public static final int REQUEST_RESOLVE_ERROR = 1001;

    /** External data */
    private Bundle externBundle;
    private boolean prompt_response = true;

    /** Response codes for the queries */
    private static final int QRY_LOG_REQ  = 0x00;
    private static final int AUTH_REQ     = 0x01;
    private static final int QRY_HBAL_REQ = 0x02; // History Balance
    private static final int QRY_TBAL_REQ = 0x03; // Today Balance
    private static final int EXCH_REQ     = 0x04;
    private static final int ALT_REQ      = 0x05;
    private static final int CURR_REQ     = 0x06;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( LauncherActivity.this );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_launcher );

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();
        // register to event bus
        EventBus.getDefault().register( this );
        // Setup the required permissions
        setupPermissions();
        // Setup the advertisement service
        if( PrefsUtils.isAdvertising( ac ) )
            this.mPromotionManager.startService();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register listener for requests and  broadcast receivers
        mRequestManager.setListener( this );
        // Request the fare value in the rate of the merchant currency
        requestCurrencies();
        // Start the scanner if necessary
        if( currentScanner != null && isScanning ) {
            isScanning = false;
            currentScanner.startScan();
        }
        // Sets Background
        mRootLayout.setBackgroundColor( PrefsUtils.getCurrentBackground( ac ) );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the scanner while app is not focus (only if scanning)
        if( currentScanner != null && currentScanner.isScanning() ) {
            isScanning = true;
            currentScanner.stopScan();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister from event bus
        EventBus.getDefault().unregister( this );

        // Stop location service while app is in background
        if( SystemUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }

        // Disconnect the advertise service
        this.mPromotionManager.stopService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerFactory.destroy();
    }

    @Override
    public void onBackPressed() {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.stopScan();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Initialized all the GUI main components
     */
    private void setupGUI() {
        // get the context
        ac = LauncherActivity.this;
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );
        // Loads images from urls
        //imageCache  = new MemoryBMCache( ac );
        //imageLoader = new ImageLoader( YodoRequest.getRequestQueue( ac ), imageCache );
        // creates the factory for the scanners
        mScannerFactory = new QRScannerFactory( this );
        // Globals
        mRootLayout      = (LinearLayout) findViewById( R.id.screenRootView );
        mSlidingLayout   = (SlidingPaneLayout) findViewById( R.id.sliding_panel_layout );
        mBalanceView     = (TextView) findViewById( R.id.balanceText );
        mScannersSpinner = (Spinner) findViewById(R.id.scannerSpinner);
        mTotalView       = (TextView) findViewById( R.id.totalText );
        mCashTenderView  = (TextView) findViewById( R.id.cashTenderText );
        mCashBackView    = (TextView) findViewById( R.id.cashBackText );
        mAvatarImage     = (NetworkImageView) findViewById( R.id.companyLogo );
        mPaymentButton   = (ImageView) findViewById( R.id.ivYodoGear );
        mBalanceBar      = (ProgressBar) findViewById( R.id.progressBarBalance );
        // Logo
        mAvatarImage.setDefaultImageResId( R.drawable.no_image );
        // Popup
        mPopupMessage  = new PopupWindow( ac );
        // Sliding Panel Configurations
        mSlidingLayout.setParallaxDistance( 30 );
        mScannersSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parentView, View selectedItemView, int position, long id ) {
                TextView scanner = (TextView) selectedItemView;
                if( scanner != null )
                    SystemUtils.Logger( TAG, scanner.getText().toString() );
                PrefsUtils.saveScanner( ac, position );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // Create the adapter for the supported qr scanners
        ArrayAdapter<QRScannerFactory.SupportedScanners> adapter = new ArrayAdapter<QRScannerFactory.SupportedScanners>(
                this,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanners.values()
        ){
            @Override
            public View getView( int position, View convertView, ViewGroup parent ) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        };
        // Get current selected scanner and verify it exists
        int position = PrefsUtils.getScanner( ac );
        if( position >= QRScannerFactory.SupportedScanners.length ) {
            position = AppConfig.DEFAULT_SCANNER;
            PrefsUtils.saveScanner( ac, position );
        }
        // Set the current scanner
        mScannersSpinner.setAdapter( adapter );
        mScannersSpinner.setSelection( PrefsUtils.getScanner( ac ) );
        // Start the messages handler
        handlerMessages   = new YodoHandler( LauncherActivity.this );
        // Set the currency icon
        GUIUtils.setCurrencyIcon( ac, mCashTenderView );
        // Set selected view as the total
        selectedView = mTotalView;
        selectedView.setBackgroundResource( R.drawable.selected_text_field );
        // Setup the preview of the Total with discount
        mTotalView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                // Get subtotal with discount
                BigDecimal discount = new BigDecimal( PrefsUtils.getDiscount( ac ) ).movePointLeft( 2 );
                BigDecimal subTotal = new BigDecimal( mTotalView.getText().toString() ).multiply(
                        BigDecimal.ONE.subtract( discount )
                );
                setupPopup( v, subTotal.setScale( 2, RoundingMode.DOWN ).toString() );
                return false;
            }
        } );
        // Setup the dismiss of the preview
        mTotalView.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch( event.getAction() ) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupMessage != null ) {
                            mPopupMessage.dismiss();
                            equivalentTender = null;
                        }
                        break;
                }
                return false;
            }
        } );
        // Setup the preview of the Tender in the current currency
        mCashTenderView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                setupPopup( v, null );
                // Request the fare value in the rate of the merchant currency
                requestCurrencies();
                return false;
            }
        } );
        // Setup the dismiss of the preview
        mCashTenderView.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch( event.getAction() ) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupMessage != null ) {
                            mPopupMessage.dismiss();
                            equivalentTender = null;
                        }
                        break;
                }
                return false;
            }
        } );
        // Set an icon there is a discount
        if( !PrefsUtils.getDiscount( ac ).equals( AppConfig.DEFAULT_DISCOUNT ) ) {
            GUIUtils.setViewIcon( ac, mTotalView, R.drawable.discount );
        }
        // If it is the first login, show the navigation panel
        if( PrefsUtils.isFirstLogin( ac ) ) {
            mSlidingLayout.openPane();
            PrefsUtils.saveFirstLogin( ac, false );
        }

        // Setup promotion manager
        mPromotionManager = new PromotionManager( this, REQUEST_RESOLVE_ERROR );
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        /** Handle external Requests */
        externBundle = getIntent().getExtras();
        if( externBundle != null ) {
            BigDecimal total = new BigDecimal( externBundle.getString( Intents.TOTAL, "0.00" ) );
            BigDecimal cashTender = new BigDecimal( externBundle.getString( Intents.CASH_TENDER, "0.00" ) );
            BigDecimal cashBack = new BigDecimal( externBundle.getString( Intents.CASH_BACK, "0.00" ) );
            //String currency = externBundle.getString( Intents.CURRENCY );

            // Checks a positive total value
            if( total.signum() > 0 ) {
                //BigDecimal tip = new BigDecimal( PrefsUtils.getCurrentTip( ac ) );
                //tip = tip.scaleByPowerOfTen( -2 ).multiply( total );
                //mTotalView.setText( total.add( tip ).setScale( 2, RoundingMode.DOWN ).toString() );
                final String tvTotal = total.setScale( 2, RoundingMode.DOWN ).toString();
                mTotalView.setText( tvTotal );
            }

            // Checks a positive cash tender value
            if( cashTender.signum() > 0 ) {
                final String tvCashTender = cashTender.setScale( 2, RoundingMode.DOWN ).toString();
                mCashTenderView.setText( tvCashTender );
            }

            // Checks a positive cash back value
            if( cashBack.signum() > 0 ) {
                final String tvCashBack = cashBack.setScale( 2, RoundingMode.DOWN ).toString();
                mCashBackView.setText( tvCashBack );
            }

            viewClick( mCashTenderView );

            // Handle the prompt of the response message from the server
            prompt_response = externBundle.getBoolean( Intents.PROMPT_RESPONSE, true );
        }
        /*****************************/

        hardwareToken = PrefsUtils.getHardwareToken( ac );
        String logo_url = PrefsUtils.getLogoUrl( ac );
        SystemUtils.Logger( TAG, logo_url );
        if( logo_url.equals( "" ) ) {
            mRequestManager.requestQuery(
                    QRY_LOG_REQ,
                    hardwareToken,
                    ServerRequest.QueryRecord.MERCHANT_LOGO );
        } else {
            mAvatarImage.setImageUrl( logo_url, mRequestManager.getImageLoader() );
        }
        // Mock location
        mLocation = new Location( "flp" );
        mLocation.setLatitude( 0.00 );
        mLocation.setLongitude( 0.00 );
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        if( PrefsUtils.isLegacy( ac ) )
            return;

        boolean locationPermission = SystemUtils.requestPermission(
                LauncherActivity.this,
                R.string.message_permission_location,
                Manifest.permission.ACCESS_FINE_LOCATION,
                PERMISSIONS_REQUEST_LOCATION
        );
        // We have permission, it is time to see if location is enabled, if not just request
        if( locationPermission )
            enableLocation();
    }

    /**
     * Asks the user to enable the location services, otherwise it
     * closes the application
     */
    private void enableLocation() {
        // If the device doesn't support the Location service, just finish the app
        if( !SystemUtils.hasLocationService( ac ) ) {
            ToastMaster.makeText( ac, R.string.message_no_gps_support, Toast.LENGTH_SHORT ).show();
            finish();
        } else if( SystemUtils.isLocationEnabled( ac ) ) {
            // Start the location service
            Intent iLoc = new Intent( ac, LocationService.class );
            if( !SystemUtils.isMyServiceRunning( ac, LocationService.class.getName() ) )
                startService( iLoc );
        } else {
            // If location not enabled, then request
            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int item ) {
                    Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                    startActivityForResult( intent, REQUEST_CODE_LOCATION_SERVICES );
                }
            };

            DialogInterface.OnClickListener onCancel = new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int item ) {
                    finish();
                }
            };

            AlertDialogHelper.showAlertDialog( ac, R.string.message_gps_enable, onClick, onCancel );
        }
    }

    /**
     * Setup a PopupWindow below a View (tender)
     * @param v The view for te popup
     */
    private void setupPopup( View v, String value ) {
        LinearLayout viewGroup = (LinearLayout) findViewById( R.id.popup_window );
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = layoutInflater.inflate( R.layout.popup_window, viewGroup );

        TextView cashTender     = (TextView) layout.findViewById( R.id.cashTenderText );
        ProgressBar progressBar = (ProgressBar) layout.findViewById( R.id.progressBarPopUp );

        // If there is no value to set, then start the progress bar
        if( value == null ) {
            GUIUtils.setMerchantCurrencyIcon( ac, cashTender );

            cashTender.setVisibility( View.GONE );
            progressBar.setVisibility( View.VISIBLE );
        } else {
            cashTender.setCompoundDrawables( null, null, null, null );
            cashTender.setText( value );
        }

        //mPopupMessage.setWidth( mCashTenderView.getWidth() );
        mPopupMessage.setWidth( LinearLayout.LayoutParams.WRAP_CONTENT );
        mPopupMessage.setHeight( LinearLayout.LayoutParams.WRAP_CONTENT );
        mPopupMessage.setContentView( layout );
        mPopupMessage.showAtLocation( v, Gravity.CENTER, 0, 0 );
    }

    /**
     * Creates a dialog to show the balance information
     */
    private void balanceDialog() {
        if( historyData.size() == 1 || todayData.size() == 1 ) {
            ToastMaster.makeText( ac, R.string.no_balance, Toast.LENGTH_SHORT ).show();
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout root = (LinearLayout) findViewById( R.id.layoutDialogRoot );
        View layout = inflater.inflate( R.layout.dialog_balance, root, false );

        TextView todayCreditsText = ( (TextView) layout.findViewById( R.id.yodoTodayCashTextView ) );
        TextView todayDebitsText  = ( (TextView) layout.findViewById( R.id.yodoTodayDebitsTextView ) );
        TextView todayBalanceText = ( (TextView) layout.findViewById( R.id.yodoTodayBalanceTextView ) );

        TextView creditsText = ( (TextView) layout.findViewById( R.id.yodoCashTextView ) );
        TextView debitsText  = ( (TextView) layout.findViewById( R.id.yodoDebitsTextView ) );
        TextView balanceText = ( (TextView) layout.findViewById( R.id.yodoBalanceTextView ) );

        BigDecimal total = BigDecimal.ZERO;

        BigDecimal result = new BigDecimal( historyData.get( ServerResponse.DEBIT ) );
        final String tvResult = result.setScale( 2, RoundingMode.HALF_DOWN ).toString();
        debitsText.setText( tvResult );
        total = total.subtract( result );

        result = new BigDecimal( historyData.get( ServerResponse.CREDIT ) );
        final String tvCredits = result.setScale( 2, RoundingMode.HALF_DOWN ).toString();
        creditsText.setText( tvCredits );
        total = total.add( result );

        final String tvBalance = total.negate().setScale( 2, RoundingMode.DOWN ).toString();
        balanceText.setText( tvBalance );
        total = BigDecimal.ZERO;

        result = new BigDecimal( todayData.get( ServerResponse.DEBIT ) );
        final String tvTodayDebit = result.setScale( 2, RoundingMode.HALF_DOWN ).toString();
        todayDebitsText.setText( tvTodayDebit );
        total = total.subtract( result );

        result = new BigDecimal( todayData.get( ServerResponse.CREDIT ) );
        final String tvTodayCredit = result.setScale( 2, RoundingMode.HALF_DOWN ).toString();
        todayCreditsText.setText( tvTodayCredit );
        total = total.add( result );

        final String tvTodayBalance = total.negate().setScale( 2, RoundingMode.DOWN ).toString();
        todayBalanceText.setText( tvTodayBalance );

        AlertDialogHelper.showAlertDialog( ac, getString( R.string.yodo_title ), layout );
        todayData = historyData = null;
    }

    /**
     * Gets the used currencies (merchant and tender) and requests their rates from the
     * server or cache
     */
    private void requestCurrencies() {
        String[] currencies = ac.getResources().getStringArray( R.array.currency_array );
        String merchantCurr = PrefsUtils.getMerchantCurrency( ac );
        String tenderCurr   = currencies[ PrefsUtils.getCurrency( ac ) ];
        mRequestManager.requestCurrencies( CURR_REQ, merchantCurr, tenderCurr );
    }

    /**
     * Changes the current currency
     * @param v View, used to get the title
     */
    public void setCurrencyClick(View v) {
        mSlidingLayout.closePane();

        final String[] currency = getResources().getStringArray( R.array.currency_array );
        final String[] icons    = getResources().getStringArray( R.array.currency_icon_array );

        Currency[] currencyList = new Currency[currency.length];
        for( int i = 0; i < currency.length; i++ )
            currencyList[i] = new Currency( currency[i], GUIUtils.getDrawableByName( ac, icons[i] ) );

        final String title        = ((Button) v).getText().toString();
        final ListAdapter adapter = new CurrencyAdapter( ac, currencyList );
        final int current         = PrefsUtils.getCurrency( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                PrefsUtils.saveCurrency( ac, item );

                Drawable icon = GUIUtils.getDrawableByName( ac, icons[ item ] );
                icon.setBounds( 0, 0, mCashTenderView.getLineHeight(), (int) ( mCashTenderView.getLineHeight() * 0.9 ) );
                mCashTenderView.setCompoundDrawables( icon, null, null, null );
                // Request the tender value in the rate of the merchant currency
                requestCurrencies();

                dialog.dismiss();
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                adapter,
                current,
                onClick
        );
    }

    public void setDiscountClick( View v ) {
        mSlidingLayout.closePane();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int item ) {
                String pip = inputBox.getText().toString();
                GUIUtils.hideSoftKeyboard( LauncherActivity.this );

                ProgressDialogHelper.getInstance().createProgressDialog(
                        ac,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );
                mRequestManager.requestMerchAuth(
                        AUTH_REQ,
                        hardwareToken,
                        pip
                );
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                inputBox, true, false,
                null,
                onClick
        );
    }

    /**
     * Opens the settings activity
     * @param v View, not used
     */
    public void settingsClick( View v ) {
        mSlidingLayout.closePane();

        Intent intent = new Intent( ac, SettingsActivity.class );
        startActivityForResult( intent, REQUEST_SETTINGS );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick(View v) {
        mSlidingLayout.closePane();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );
        final CheckBox remember = new CheckBox( ac );
        remember.setText( R.string.remember_pass );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                GUIUtils.hideSoftKeyboard( LauncherActivity.this );

                if( remember.isChecked() )
                    PrefsUtils.savePassword( ac, pip );
                else
                    PrefsUtils.savePassword( ac, null );

                ProgressDialogHelper.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );

                mRequestManager.requestQuery(
                        QRY_HBAL_REQ,
                        hardwareToken,
                        pip,
                        ServerRequest.QueryRecord.HISTORY_BALANCE
                );

                mRequestManager.requestQuery(
                        QRY_TBAL_REQ,
                        hardwareToken,
                        pip,
                        ServerRequest.QueryRecord.TODAY_BALANCE
                );
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                inputBox, false, true,
                remember,
                onClick
        );
    }

    /**
     * Shows some basic information about the POS
     * @param v View, not used
     */
    public void aboutClick(View v) {
        mSlidingLayout.closePane();

        final String title   = ((Button) v).getText().toString();
        final String message = getString( R.string.imei )    + " " +
                               PrefsUtils.getHardwareToken( ac ) + "\n" +
                               getString( R.string.label_currency )    + " " +
                               PrefsUtils.getMerchantCurrency( ac ) + "\n" +
                               getString( R.string.version_label ) + " " +
                               getString( R.string.version_value ) + "/" +
                               YodoRequest.getSwitch();

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                message,
                null
        );
    }

    /**
     * Logout the user
     * @param v View, not used
     */
    public void logoutClick(View v) {
        //imageCache.clear();
        PrefsUtils.saveLoginStatus( ac, false );
        PrefsUtils.saveLogoUrl( ac, "" );
        finish();
    }

    /**
     * Change the selected view (purchase, cash tender, or cash back)
     * @param v The selected view
     */
    public void viewClick(View v) {
        if( selectedView != null )
            selectedView.setBackgroundResource( R.drawable.show_text_field );

        selectedView = ((TextView) v);
        selectedView.setBackgroundResource( R.drawable.selected_text_field );
    }

    /**
     * Resets the values to 0.00
     * @param v The View, not used
     */
    public void resetClick(View v) {
        String zero = getString( R.string.zero );

        mTotalView.setText( zero );
        mCashTenderView.setText( zero );
        mCashBackView.setText( zero );

        mBalanceView.setText( zero );
    }

    /** Handle numeric button clicked
     *  @param v View, used to get the number
     */
    @SuppressWarnings( "unused" )
    public void valueClick( View v ) {
        final String value   = ((Button)v).getText().toString();

        // This button is not working
        if( value.equals( getString( R.string.value__ ) ) )
            return;

        for( int i = 0; i < value.length(); i++ ) {
            final String current = selectedView.getText().toString();

            BigDecimal temp = new BigDecimal( current + value );
            final String tvNewValue = temp.multiply( BigDecimal.TEN ).setScale( 2, RoundingMode.DOWN ).toString();
            selectedView.setText( tvNewValue );
        }
        // Request the fare value in the rate of the merchant currency
        requestCurrencies();
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    @SuppressWarnings( "unused" )
    public void addClick( View v ) {
        final String amount  = ((Button)v).getText().toString();
        final String current = selectedView.getText().toString();

        if( amount.equals( getString( R.string.coins_0 ) ) ) {
            selectedView.setText( getString( R.string.zero ) );
        } else {
            BigDecimal value = new BigDecimal( current ).add( new BigDecimal( amount ) );
            final String tvNewValue = value.setScale( 2, RoundingMode.DOWN ).toString();
            selectedView.setText( tvNewValue );
        }
        // Request the fare value in the rate of the merchant currency
        requestCurrencies();
    }

    /**
     * Handles on back pressed
     * @param v View, not used
     */
    public void backClick(View v) {
        onBackPressed();
    }

    /**
     * Request the permission for the camera
     */
    private void showCamera() {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.stopScan();
            return;
        }

        GUIUtils.rotateImage( mPaymentButton );
        currentScanner = mScannerFactory.getScanner(
                (QRScannerFactory.SupportedScanners) mScannersSpinner.getSelectedItem()
        );

        if( currentScanner != null )
            currentScanner.startScan();
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void yodoPayClick(View v) {
        boolean cameraPermission = SystemUtils.requestPermission(
                LauncherActivity.this,
                R.string.message_permission_camera,
                Manifest.permission.CAMERA,
                PERMISSIONS_REQUEST_CAMERA
        );

        if( cameraPermission )
            showCamera();
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        String code, message;

        switch( responseCode ) {

            case AUTH_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    final String title      = getString( R.string.text_set_discount ) + " (%)";
                    final EditText inputBox = new ClearEditText( ac );
                    inputBox.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL );

                    InputFilter filter = new InputFilter() {
                        final int maxDigitsBeforeDecimalPoint = 2;
                        final int maxDigitsAfterDecimalPoint  = 2;

                        @Override
                        public CharSequence filter( CharSequence source, int start, int end, Spanned dest, int dstart, int dend ) {
                            StringBuilder builder = new StringBuilder( dest );
                            builder.replace( dstart, dend, source.subSequence( start, end ).toString() );
                            if( !builder.toString().matches(
                                    "(([" +( maxDigitsBeforeDecimalPoint - 1 ) +
                                    "-9])([0-9]?)?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

                            )) {
                                if( source.length() == 0 )
                                    return dest.subSequence( dstart, dend );
                                return "";
                            }

                            return null;
                        }
                    };
                    inputBox.setFilters( new InputFilter[] { filter } );
                    inputBox.setText( PrefsUtils.getDiscount( ac ) );

                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String discount = inputBox.getText().toString();
                            GUIUtils.hideSoftKeyboard( LauncherActivity.this );
                            if( discount.length() > 0 ) {
                                PrefsUtils.saveDiscount( ac, discount );
                                GUIUtils.setViewIcon( ac, mTotalView, R.drawable.discount );
                            } else {
                                PrefsUtils.saveDiscount( ac, AppConfig.DEFAULT_DISCOUNT );
                                GUIUtils.setViewIcon( ac, mTotalView, null );
                            }
                            requestCurrencies();
                        }
                    };

                    AlertDialogHelper.showAlertDialog(
                            ac,
                            title,
                            inputBox,
                            onClick
                    );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    GUIUtils.errorSound( ac );

                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = getString( R.string.message_incorrect_pip );

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                }
                break;

            case QRY_HBAL_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    historyData = response.getParams();
                    if( todayData != null )
                        balanceDialog();
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    GUIUtils.errorSound( ac );

                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage();

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                    todayData = historyData = null;
                }
                break;

            case QRY_TBAL_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    todayData = response.getParams();
                    if( historyData != null )
                        balanceDialog();
                }
                break;

            case QRY_LOG_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String logoName = response.getParam( ServerResponse.LOGO );

                    if( logoName != null ) {
                        String logo_url = AppConfig.LOGO_PATH + logoName;
                        PrefsUtils.saveLogoUrl( ac, logo_url );
                        mAvatarImage.setImageUrl( logo_url, mRequestManager.getImageLoader() );
                    }
                }
                break;

            case CURR_REQ:
                final String sMerchRate = response.getParam( ServerResponse.MERCH_RATE );
                final String sFareRate  = response.getParam( ServerResponse.TENDER_RATE );

                if( sMerchRate != null && sFareRate != null ) {
                    // List of Currencies
                    String[] currencies = ac.getResources().getStringArray( R.array.currency_array );
                    // Get the rates in BigDecimals
                    BigDecimal merchRate = new BigDecimal( sMerchRate );
                    BigDecimal fareRate = new BigDecimal( sFareRate );
                    // Get the values of the total and cashback
                    String totalPurchase   = mTotalView.getText().toString();
                    String cashBack        = mCashBackView.getText().toString();
                    // Get raw value, in order to transform
                    BigDecimal temp_tender = new BigDecimal( mCashTenderView.getText().toString() );

                    // Transform the currencies using the rate
                    if( AppConfig.URL_CURRENCY.equals( currencies[ PrefsUtils.getCurrency( ac ) ] ) ) {
                        equivalentTender = temp_tender.multiply( merchRate );
                    } else {
                        BigDecimal currency_rate = merchRate.divide( fareRate, 2, RoundingMode.DOWN );
                        equivalentTender = temp_tender.multiply( currency_rate );
                    }
                    // Get subtotal with discount
                    BigDecimal discount = new BigDecimal( PrefsUtils.getDiscount( ac ) ).movePointLeft( 2 );
                    BigDecimal subTotal = new BigDecimal( totalPurchase ).multiply(
                            BigDecimal.ONE.subtract( discount )
                    );
                    // Get total balance
                    BigDecimal total = equivalentTender.subtract(
                            subTotal.add( new BigDecimal( cashBack ) )
                    );

                    final String tvBalance = total.setScale( 2, RoundingMode.DOWN ).toString();
                    mBalanceView.setText( tvBalance );
                    mBalanceView.setVisibility( View.VISIBLE );
                    mBalanceBar.setVisibility( View.GONE );
                    // If the popup is showing, then use the new value
                    if( mPopupMessage.isShowing() ) {
                        // Disappear the progress bar and show the value
                        mPopupMessage.getContentView().findViewById( R.id.progressBarPopUp ).setVisibility( View.GONE );
                        TextView value = (TextView) mPopupMessage.getContentView().findViewById( R.id.cashTenderText );
                        value.setVisibility( View.VISIBLE );
                        final String tvTender = equivalentTender.setScale( 2, RoundingMode.DOWN ).toString();
                        value.setText( tvTender );
                    }
                }

                break;

            case EXCH_REQ:
            case ALT_REQ:
                code = response.getCode();
                final String ex_code       = response.getCode();
                final String ex_authNumber = response.getAuthNumber();
                final String ex_message    = response.getMessage();
                final String ex_account    = response.getParam( ServerResponse.ACCOUNT );
                final String ex_purchase   = response.getParam( ServerResponse.PURCHASE );
                final String ex_amount     = response.getParam( ServerResponse.AMOUNT_DELTA );

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    message = getString( R.string.exchange_auth ) + " " + ex_authNumber + "\n" +
                              getString( R.string.exchange_message ) + " " + ex_message;

                    DialogInterface.OnClickListener onClick = null;

                    if( externBundle != null ) {
                        final Intent data = new Intent();
                        data.putExtra( Intents.RESULT_CODE, ex_code );
                        data.putExtra( Intents.RESULT_AUTH, ex_authNumber );
                        data.putExtra( Intents.RESULT_MSG, ex_message );
                        data.putExtra( Intents.RESULT_ACC, ex_account );
                        data.putExtra( Intents.RESULT_PUR, ex_purchase );
                        data.putExtra( Intents.RESULT_DEL, ex_amount );
                        setResult( RESULT_OK, data );

                        onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                finish();
                            }
                        };
                    }

                    final String[] currency = getResources().getStringArray( R.array.currency_array );

                    // Gets the merchant currency and its position in the array of currencies
                    final String merchCurrency = PrefsUtils.getMerchantCurrency( ac );
                    if( merchCurrency != null ) {
                        final int currPosition = Arrays.asList( currency ).indexOf( merchCurrency );
                        // Saves the currency and sets the icon
                        PrefsUtils.saveCurrency( ac, currPosition );
                        GUIUtils.setCurrencyIcon( ac, mCashTenderView );
                    }

                    if( prompt_response )
                        AlertDialogHelper.showAlertDialog( ac, response.getCode(), message , onClick );
                    else finish();
                } else {
                    GUIUtils.errorSound( ac );
                    message  = response.getMessage() + "\n" + response.getParam( XMLHandler.PARAMS );
                    PrefsUtils.sendMessage( handlerMessages, code, message );
                }
                break;
        }
    }

    @Override
    public void onNewData( String data ) {
        final String[] currency = getResources().getStringArray( R.array.currency_array );

        // Get subtotal with discount
        BigDecimal discount = new BigDecimal( PrefsUtils.getDiscount( ac ) ).movePointLeft( 2 );
        BigDecimal subTotal = new BigDecimal( mTotalView.getText().toString() ).multiply(
                BigDecimal.ONE.subtract( discount )
        );

        String totalPurchase = subTotal.setScale( 2, RoundingMode.DOWN ).toString();
        String cashTender    = mCashTenderView.getText().toString();
        String cashBack      = mCashBackView.getText().toString();

        switch( data.length() ) {
            case AppConfig.SKS_SIZE:
                ProgressDialogHelper.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        ProgressDialogHelper.ProgressDialogType.TRANSPARENT
                );

                mRequestManager.requestExchange(
                        EXCH_REQ,
                        hardwareToken,
                        data,
                        totalPurchase,
                        cashTender,
                        cashBack,
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        currency[ PrefsUtils.getCurrency( ac ) ]
                );
                break;

            case AppConfig.ALT_SIZE:
                String clientData  = data.substring( 0, data.length() - 1 );
                String accountType = data.substring( data.length() - 1 );

                ProgressDialogHelper.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        ProgressDialogHelper.ProgressDialogType.TRANSPARENT
                );

                mRequestManager.requestAlternate(
                        ALT_REQ,
                        accountType,
                        hardwareToken,
                        clientData,
                        totalPurchase,
                        cashTender,
                        cashBack,
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        currency[ PrefsUtils.getCurrency( ac ) ]
                );
                break;

            default:
                ToastMaster.makeText( LauncherActivity.this, R.string.exchange_error, Toast.LENGTH_LONG ).show();
                break;
        }
    }

    @SuppressWarnings( "unused" ) // it receives events from the Location Service
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onLocationEvent( Location location ) {
        // Remove Sticky Event
        EventBus.getDefault().removeStickyEvent( Location.class );
        // Process the Event
        mLocation = location;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch( requestCode ) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if( !SystemUtils.isLocationEnabled( ac ) )
                    finish();
                break;

            case REQUEST_SETTINGS:
                if( resultCode == RESULT_FIRST_USER ) {
                    setResult( RESULT_FIRST_USER );
                    finish();
                }
                break;

            case REQUEST_RESOLVE_ERROR:
                this.mPromotionManager.onResolutionResult();
                if( resultCode == RESULT_OK )
                    this.mPromotionManager.publish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    showCamera();
                }
                break;

            case PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    enableLocation();
                } else {
                    // Permission Denied
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }

    @Override
    public void onConnected( @Nullable Bundle bundle ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connected" );
        this.mPromotionManager.publish();
    }

    @Override
    public void onConnectionSuspended( int i ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connection suspended" );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult result ) {
        SystemUtils.Logger( TAG, "connection to GoogleApiClient failed" );
    }
}
