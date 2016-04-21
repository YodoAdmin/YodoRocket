package co.yodo.launcher.main;

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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import co.yodo.launcher.R;
import co.yodo.launcher.adapter.CurrencyAdapter;
import co.yodo.launcher.component.MemoryBMCache;
import co.yodo.launcher.component.ClearEditText;
import co.yodo.launcher.component.ToastMaster;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.data.Currency;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.helper.Intents;
import co.yodo.launcher.net.YodoRequest;
import co.yodo.launcher.scanner.QRScanner;
import co.yodo.launcher.scanner.QRScannerFactory;
import co.yodo.launcher.service.LocationService;
import co.yodo.launcher.service.RESTService;

public class LauncherActivity extends AppCompatActivity implements YodoRequest.RESTListener, QRScanner.QRScannerListener {
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
    //private ImageView mLogoImage;
    NetworkImageView mAvatarImage;
    private ImageView mPaymentButton;
    private ProgressBar mBalanceBar;

    /** Popup Window for Tips */
    private PopupWindow mPopupMessage;
    private BigDecimal equivalentTender;

    /** Selected Text View */
    private TextView selectedView;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** ImageLoader */
    private ImageLoader imageLoader;
    private MemoryBMCache imageCache;

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

    /** External data */
    private Bundle externBundle;
    private boolean prompt_response = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( LauncherActivity.this );
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the listener for the request (this activity)
        YodoRequest.getInstance().setListener( this );
        // Request the fare value in the rate of the merchant currency
        YodoRequest.getInstance().requestCurrencies( ac );
        // Enable the advertising service
        AppUtils.setupAdvertising( ac, AppUtils.isAdvertisingServiceRunning( ac ), false );
        // Start the scanner if necessary
        if( currentScanner != null && isScanning ) {
            isScanning = false;
            currentScanner.startScan();
        }
        // Sets Background
        mRootLayout.setBackgroundColor( AppUtils.getCurrentBackground( ac ) );
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
        // unregister from event bus
        EventBus.getDefault().unregister( this );
        // Stop location service while app is in background
        if( AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }
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
        // Loads images from urls
        imageCache  = new MemoryBMCache( ac );
        imageLoader = new ImageLoader( YodoRequest.getRequestQueue( ac ), imageCache );
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
        //mLogoImage       = (ImageView) findViewById( R.id.companyLogo );
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
                    AppUtils.Logger( TAG, scanner.getText().toString() );
                AppUtils.saveScanner( ac, position );
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
        int position = AppUtils.getScanner( ac );
        if( position >= QRScannerFactory.SupportedScanners.length ) {
            position = AppConfig.DEFAULT_SCANNER;
            AppUtils.saveScanner( ac, position );
        }
        // Set the current scanner
        mScannersSpinner.setAdapter( adapter );
        mScannersSpinner.setSelection( AppUtils.getScanner( ac ) );
        // Start the messages handler
        handlerMessages   = new YodoHandler( LauncherActivity.this );
        // Set the currency icon
        AppUtils.setCurrencyIcon( ac, mCashTenderView, false );
        // Set selected view as the total
        selectedView = mTotalView;
        selectedView.setBackgroundResource( R.drawable.selected_text_field );
        // Setup the preview of the Total with discount
        mTotalView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                // Get subtotal with discount
                BigDecimal discount = new BigDecimal( AppUtils.getDiscount( ac ) ).movePointLeft( 2 );
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
                YodoRequest.getInstance().requestCurrencies( ac );
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
        if( !AppUtils.getDiscount( ac ).equals( AppConfig.DEFAULT_DISCOUNT ) ) {
            AppUtils.setViewIcon( ac, mTotalView, R.drawable.discount );
        }
        // If it is the first login, show the navigation panel
        if( AppUtils.isFirstLogin( ac ) ) {
            mSlidingLayout.openPane();
            AppUtils.saveFirstLogin( ac, false );
        }
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

            if( total.signum() > 0 ) {
                BigDecimal tip = new BigDecimal( AppUtils.getCurrentTip( ac ) );
                tip = tip.scaleByPowerOfTen( -2 ).multiply( total );
                mTotalView.setText( total.add( tip ).setScale( 2, RoundingMode.DOWN ).toString() );
            }
            if( cashTender.signum() > 0 ) mCashTenderView.setText( cashTender.setScale( 2, RoundingMode.DOWN ).toString() );
            if( cashBack.signum() > 0 ) mCashBackView.setText( cashBack.setScale( 2, RoundingMode.DOWN ).toString() );

            viewClick( mCashTenderView );

            // Handle the prompt of the response message from the server
            prompt_response = externBundle.getBoolean( Intents.PROMPT_RESPONSE, true );
        }
        /*****************************/

        hardwareToken = AppUtils.getHardwareToken( ac );
        String logo_url = AppUtils.getLogoUrl( ac );
        AppUtils.Logger( TAG, logo_url );
        if( logo_url.equals( "" ) ) {
            YodoRequest.getInstance().requestLogo( LauncherActivity.this, hardwareToken );
        } else {
            mAvatarImage.setImageUrl( logo_url, imageLoader );
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
        boolean locationPermission = AppUtils.requestPermission(
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
        if( !AppUtils.hasLocationService( ac ) ) {
            ToastMaster.makeText( ac, R.string.message_no_gps_support, Toast.LENGTH_SHORT ).show();
            finish();
        } else if( AppUtils.isLocationEnabled( ac ) ) {
            // Start the location service
            Intent iLoc = new Intent( ac, LocationService.class );
            if( !AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) )
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
            AppUtils.setMerchantCurrencyIcon( ac, cashTender );

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
        debitsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.subtract( result );

        result = new BigDecimal( historyData.get( ServerResponse.CREDIT ) );
        creditsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.add( result );

        balanceText.setText(
                total.negate().setScale( 2, RoundingMode.DOWN ).toString()
        );
        total = BigDecimal.ZERO;

        result = new BigDecimal( todayData.get( ServerResponse.DEBIT ) );
        todayDebitsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.subtract( result );

        result = new BigDecimal( todayData.get( ServerResponse.CREDIT ) );
        todayCreditsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.add( result );

        todayBalanceText.setText(
                total.negate().setScale( 2, RoundingMode.DOWN ).toString()
        );

        AlertDialogHelper.showAlertDialog( ac, getString( R.string.yodo_title ), layout );
        todayData = historyData = null;
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
            currencyList[i] = new Currency( currency[i], AppUtils.getDrawableByName( ac, icons[i] ) );

        final String title        = ((Button) v).getText().toString();
        final ListAdapter adapter = new CurrencyAdapter( ac, currencyList );
        final int current         = AppUtils.getCurrency( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                AppUtils.saveCurrency( ac, item );

                Drawable icon = AppUtils.getDrawableByName( ac, icons[ item ] );
                icon.setBounds( 0, 0, mCashTenderView.getLineHeight(), (int) ( mCashTenderView.getLineHeight() * 0.9 ) );
                mCashTenderView.setCompoundDrawables( icon, null, null, null );
                // Request the fare value in the rate of the merchant currency
                YodoRequest.getInstance().requestCurrencies( ac );

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
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( LauncherActivity.this );

                YodoRequest.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        YodoRequest.ProgressDialogType.NORMAL
                );

                YodoRequest.getInstance().requestPIPAuthentication( ac, hardwareToken, pip );
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
                AppUtils.hideSoftKeyboard( LauncherActivity.this );

                if( remember.isChecked() )
                    AppUtils.savePassword( ac, pip );
                else
                    AppUtils.savePassword( ac, null );

                YodoRequest.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        YodoRequest.ProgressDialogType.NORMAL
                );

                YodoRequest.getInstance().requestHistory(
                        LauncherActivity.this,
                        hardwareToken, pip
                );

                YodoRequest.getInstance().requestDailyHistory(
                        LauncherActivity.this,
                        hardwareToken, pip
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
                               AppUtils.getHardwareToken( ac ) + "\n" +
                               getString( R.string.label_currency )    + " " +
                               AppUtils.getMerchantCurrency( ac ) + "\n" +
                               getString( R.string.version_label ) + " " +
                               getString( R.string.version_value ) + "/" +
                               RESTService.getSwitch();

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
        imageCache.clear();
        AppUtils.saveLoginStatus( ac, false );
        AppUtils.saveLogoUrl( ac, "" );
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
            selectedView.setText(
                    temp.multiply( BigDecimal.TEN ).setScale( 2, RoundingMode.DOWN ).toString()
            );
        }
        // Request the fare value in the rate of the merchant currency
        YodoRequest.getInstance().requestCurrencies( ac );
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
            selectedView.setText( value.setScale( 2, RoundingMode.DOWN ).toString() );
        }
        // Request the fare value in the rate of the merchant currency
        YodoRequest.getInstance().requestCurrencies( ac );
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

        AppUtils.rotateImage( mPaymentButton );
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
        boolean cameraPermission = AppUtils.requestPermission(
                LauncherActivity.this,
                R.string.message_permission_camera,
                Manifest.permission.CAMERA,
                PERMISSIONS_REQUEST_CAMERA
        );

        if( cameraPermission )
            showCamera();
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_PIP_REQUEST:
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
                                    "(([1-9]{1})([0-9]{0,"+( maxDigitsBeforeDecimalPoint - 1 ) +
                                    "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

                            )) {
                                if( source.length() == 0 )
                                    return dest.subSequence( dstart, dend );
                                return "";
                            }

                            return null;
                        }
                    };
                    inputBox.setFilters( new InputFilter[] { filter } );
                    inputBox.setText( AppUtils.getDiscount( ac ) );

                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String discount = inputBox.getText().toString();
                            AppUtils.hideSoftKeyboard( LauncherActivity.this );
                            if( discount.length() > 0 && Integer.parseInt( discount ) > 0 ) {
                                AppUtils.saveDiscount( ac, discount );
                                AppUtils.setViewIcon( ac, mTotalView, R.drawable.discount );
                            } else {
                                AppUtils.saveDiscount( ac, AppConfig.DEFAULT_DISCOUNT );
                                AppUtils.setViewIcon( ac, mTotalView, null );
                            }
                            YodoRequest.getInstance().requestCurrencies( ac );
                        }
                    };

                    AlertDialogHelper.showAlertDialog(
                            ac,
                            title,
                            inputBox,
                            onClick
                    );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    AppUtils.errorSound( ac );

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

            case QUERY_BAL_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    historyData = response.getParams();
                    if( todayData != null )
                        balanceDialog();
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    AppUtils.errorSound( ac );

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

            case QUERY_DAY_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    todayData = response.getParams();
                    if( historyData != null )
                        balanceDialog();
                }
                break;

            case QUERY_LOGO_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String logoName = response.getParam( ServerResponse.LOGO );

                    if( logoName != null ) {
                        String logo_url = AppConfig.LOGO_PATH + logoName;
                        AppUtils.saveLogoUrl( ac, logo_url );
                        mAvatarImage.setImageUrl( logo_url, imageLoader );
                    }
                }
                break;

            case CURRENCIES_REQUEST:
                final String sMerchRate = response.getParam( ServerResponse.MERCH_RATE );
                final String sFareRate  = response.getParam( ServerResponse.FARE_RATE );

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
                    if( AppConfig.URL_CURRENCY.equals( currencies[ AppUtils.getCurrency( ac ) ] ) ) {
                        equivalentTender = temp_tender.multiply( merchRate );
                    } else {
                        BigDecimal currency_rate = merchRate.divide( fareRate, 2 );
                        equivalentTender = temp_tender.multiply( currency_rate );
                    }
                    // Get subtotal with discount
                    BigDecimal discount = new BigDecimal( AppUtils.getDiscount( ac ) ).movePointLeft( 2 );
                    BigDecimal subTotal = new BigDecimal( totalPurchase ).multiply(
                            BigDecimal.ONE.subtract( discount )
                    );
                    // Get total balance
                    BigDecimal total = equivalentTender.subtract(
                            subTotal.add( new BigDecimal( cashBack ) )
                    );
                    mBalanceView.setText( total.setScale( 2, RoundingMode.DOWN ).toString() );
                    mBalanceView.setVisibility( View.VISIBLE );
                    mBalanceBar.setVisibility( View.GONE );
                    // If the popup is showing, then use the new value
                    if( mPopupMessage.isShowing() ) {
                        // Disappear the progress bar and show the value
                        mPopupMessage.getContentView().findViewById( R.id.progressBarPopUp ).setVisibility( View.GONE );
                        TextView value = (TextView) mPopupMessage.getContentView().findViewById( R.id.cashTenderText );
                        value.setVisibility( View.VISIBLE );
                        value.setText( equivalentTender.setScale( 2, RoundingMode.DOWN ).toString() );
                    }
                }

                break;

            case EXCH_MERCH_REQUEST:
            case ALT_MERCH_REQUEST:
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

                    if( prompt_response )
                        AlertDialogHelper.showAlertDialog( ac, response.getCode(), message , onClick );
                    else finish();
                } else {
                    AppUtils.errorSound( ac );

                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage() + "\n" + response.getParam( ServerResponse.PARAMS );

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                }
                break;
        }
    }

    @Override
    public void onNewData( String data ) {
        final String[] currency = getResources().getStringArray( R.array.currency_array );

        // Get subtotal with discount
        BigDecimal discount = new BigDecimal( AppUtils.getDiscount( ac ) ).movePointLeft( 2 );
        BigDecimal subTotal = new BigDecimal( mTotalView.getText().toString() ).multiply(
                BigDecimal.ONE.subtract( discount )
        );

        String totalPurchase = subTotal.setScale( 2, RoundingMode.DOWN ).toString();
        String cashTender    = mCashTenderView.getText().toString();
        String cashBack      = mCashBackView.getText().toString();

        switch( data.length() ) {
            case AppConfig.SKS_SIZE:
                YodoRequest.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        YodoRequest.ProgressDialogType.TRANSPARENT
                );

                YodoRequest.getInstance().requestExchange(
                        LauncherActivity.this,
                        hardwareToken,
                        data,
                        totalPurchase,
                        cashTender,
                        cashBack,
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        currency[ AppUtils.getCurrency( ac ) ]
                );
                break;

            case AppConfig.ALT_SIZE:
                YodoRequest.getInstance().createProgressDialog(
                        LauncherActivity.this,
                        YodoRequest.ProgressDialogType.TRANSPARENT
                );

                YodoRequest.getInstance().requestAlternate(
                        LauncherActivity.this,
                        hardwareToken,
                        data,
                        totalPurchase,
                        cashTender,
                        cashBack,
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        currency[ AppUtils.getCurrency( ac ) ]
                );
                break;

            default:
                ToastMaster.makeText( LauncherActivity.this, R.string.exchange_error, Toast.LENGTH_LONG ).show();
                break;
        }
    }

    @SuppressWarnings("unused") // it receives events from the Location Service
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onLocationEvent( Location location ) {
        // Remove Sticky Event
        EventBus.getDefault().removeStickyEvent( Location.class );
        // Process the Event
        mLocation = location;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if( !AppUtils.isLocationEnabled( ac ) )
                    finish();
                break;

            case REQUEST_SETTINGS:
                if( resultCode == RESULT_FIRST_USER ) {
                    setResult( RESULT_FIRST_USER );
                    finish();
                }
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
}
