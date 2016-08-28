package co.yodo.launcher.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.component.SKS;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.FormatUtils;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.component.Intents;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.manager.PromotionManager;
import co.yodo.launcher.service.LocationService;
import co.yodo.launcher.ui.adapter.ScannerAdapter;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.launcher.ui.option.AboutOption;
import co.yodo.launcher.ui.option.BalanceOption;
import co.yodo.launcher.ui.option.CurrencyOption;
import co.yodo.launcher.ui.option.DiscountOption;
import co.yodo.launcher.ui.scanner.QRScanner;
import co.yodo.launcher.ui.scanner.QRScannerFactory;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.builder.ServerRequest;
import co.yodo.restapi.network.handler.XMLHandler;
import co.yodo.restapi.network.model.ServerResponse;

public class LauncherActivity extends AppCompatActivity implements
        PromotionManager.IPromotionListener,
        YodoRequest.RESTListener,
        QRScanner.QRScannerListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** POS Data */
    private String mHardwareToken;

    /** Messages Handler */
    private MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    YodoRequest mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** GUI controllers */
    @BindView( R.id.llRoot )
    LinearLayout llRoot;

    @BindView( R.id.splActivityLauncher )
    SlidingPaneLayout splActivityLauncher;

    @BindView( R.id.nivCompanyLogo )
    NetworkImageView nivCompanyLogo;

    @BindView( R.id.sScannerSelector )
    Spinner sScannerSelector;

    @BindView( R.id.pgBalance )
    ProgressBar pgBalance;

    @BindView( R.id.tvTotal )
    TextView tvTotal;

    @BindView( R.id.tvCashtender )
    TextView tvCashtender;

    @BindView( R.id.tvCashback )
    TextView tvCashback;

    @BindView( R.id.tvBalance )
    TextView tvBalance;

    @BindView( R.id.ivYodoGear )
    ImageView ivYodoGear;

    /** Popup Window and GUI for tender */
    private PopupWindow mPopupTender;
    private ProgressBar pbpTender;
    private TextView tvpTender;

    /** Popup Window and GUI for discount */
    private PopupWindow mPopupDiscount;
    private TextView tvpDiscount;

    /** Selected Text View (total, cashtender, cashback) */
    private TextView tvSelected;

    /** Options from the navigation window */
    private CurrencyOption mCurrencyOption;
    private DiscountOption mDiscountOption;
    private BalanceOption mBalanceOption;
    private AboutOption mAboutOption;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager mPromotionManager;
    private boolean isPublishing = false;

    /** Current Scanners */
    private QRScannerFactory mScannerFactory;
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** Location */
    private Location mLocation =  new Location( "flp" );

    /** Code for the error dialog */
    private static final int REQUEST_CODE_LOCATION_SERVICES = 0;
    private static final int REQUEST_SETTINGS               = 1;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA   = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    /** External data */
    private Bundle externBundle;
    private boolean prompt_response = true;
    private String ex_tip;

    /** Response codes for the queries */
    private static final int QRY_LOG_REQ = 0x00;
    private static final int EXCH_REQ    = 0x01;
    private static final int ALT_REQ     = 0x02;
    private static final int CURR_REQ    = 0x03;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_launcher );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register listener for requests and  broadcast receivers
        mRequestManager.setListener( this );

        // Start the scanner if necessary
        if( currentScanner != null && isScanning ) {
            isScanning = false;
            currentScanner.startScan();
        }

        // Sets Background
        llRoot.setBackgroundColor( PrefUtils.getCurrentBackground( ac ) );
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
    public void onStart() {
        super.onStart();
        // register to event bus
        EventBus.getDefault().register( this );

        // Setup the required permissions for location
        if( PrefUtils.isLocating( ac ) ) {
            LocationService.setup(
                    this,
                    PERMISSIONS_REQUEST_LOCATION,
                    REQUEST_CODE_LOCATION_SERVICES
            );
        }
    }

    @Override
    public void onStop() {
        // Unregister from event bus
        EventBus.getDefault().unregister( this );

        // Stop location service while app is in background
        if( SystemUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }

        super.onStop();
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
        mHandlerMessages = new MessageHandler( LauncherActivity.this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );
        mRequestManager.setListener( this );

        // Setup promotion manager start it
        mPromotionManager = new PromotionManager( this );
        mPromotionManager.startService();

        // Start scanner factory
        mScannerFactory = new QRScannerFactory( this );

        // Set selected view as the total
        selectClick( tvTotal );

        // Global options (navigation window)
        mCurrencyOption = new CurrencyOption( this );
        mDiscountOption = new DiscountOption( this, mHandlerMessages, mPromotionManager );
        mBalanceOption  = new BalanceOption( this, mHandlerMessages, mPromotionManager );
        mAboutOption    = new AboutOption( this );

        // Sliding Panel Configurations
        splActivityLauncher.setParallaxDistance( 30 );

        // Sets up the spinner, listeners, popup, and set currency
        initializeScannerSpinner();
        initializeTextListeners();
        initializePopups();
        setDefaultCurrency();

        // Set default Logo
        nivCompanyLogo.setDefaultImageResId( R.drawable.no_image );

        // Set an icon there is a discount
        if( !PrefUtils.getDiscount( ac ).equals( AppConfig.DEFAULT_DISCOUNT ) ) {
            GUIUtils.setViewIcon( ac, tvTotal, R.drawable.discount );
        }

        // If it is the first login, show the navigation panel
        if( PrefUtils.isFirstLogin( ac ) ) {
            splActivityLauncher.openPane();
            PrefUtils.saveFirstLogin( ac, false );
        }
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        /********************************* Handle external Requests ****************************************/
        /***************************************************************************************************/
        externBundle = getIntent().getExtras();
        if( externBundle != null ) {
            BigDecimal total = new BigDecimal( externBundle.getString( Intents.TOTAL, "0.00" ) );
            BigDecimal cashTender = new BigDecimal( externBundle.getString( Intents.CASH_TENDER, "0.00" ) );
            BigDecimal cashBack = new BigDecimal( externBundle.getString( Intents.CASH_BACK, "0.00" ) );
            final String currency = externBundle.getString( Intents.TENDER_CURRENCY );

            // Checks a positive total value
            if( total.signum() > 0 ) {
                final String totalValue = total.setScale( 2, RoundingMode.DOWN ).toString();
                tvTotal.setText( totalValue );
            }

            // Checks a positive cash tender value
            if( cashTender.signum() > 0 ) {
                final String cashtenderValue = cashTender.setScale( 2, RoundingMode.DOWN ).toString();
                tvCashtender.setText( cashtenderValue );
            }

            // Checks a positive cash back value
            if( cashBack.signum() > 0 ) {
                final String cashbackValue = cashBack.setScale( 2, RoundingMode.DOWN ).toString();
                tvCashback.setText( cashbackValue );
            }

            // Check if the currency is supported
            final String[] currencies = getResources().getStringArray( R.array.currency_array );
            if( Arrays.asList( currencies ).contains( currency ) ) {
                PrefUtils.saveTenderCurrency( ac, currency );
                GUIUtils.setTenderCurrencyIcon( ac, tvCashtender );
            }

            selectClick( tvCashtender );
            tvTotal.setOnClickListener( null );

            // Handle the prompt of the response message from the server
            prompt_response = externBundle.getBoolean( Intents.PROMPT_RESPONSE, true );
        }
        /***************************************************************************************************/

        // Get the device hardware token
        mHardwareToken = PrefUtils.getHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }

        // Get the Logo now that we have the hardware token
        String logo_url = PrefUtils.getLogoUrl( ac );
        if( logo_url.equals( "" ) ) {
            mRequestManager.requestQuery(
                    QRY_LOG_REQ,
                    mHardwareToken,
                    ServerRequest.QueryRecord.MERCHANT_LOGO );
        } else {
            nivCompanyLogo.setImageUrl( logo_url, mRequestManager.getImageLoader() );
        }
    }

    /**
     * Initializes the spinner for the scanners
     */
    private void initializeScannerSpinner() {
        // Add item listener to the spinner
        sScannerSelector.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parentView, View selectedItemView, int position, long id ) {
                TextView scanner = (TextView) selectedItemView;
                if( scanner != null )
                    SystemUtils.Logger( TAG, scanner.getText().toString() );
                PrefUtils.saveScanner( ac, position );
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
            }
        });

        // Create the adapter for the supported qr scanners
        ArrayAdapter<QRScannerFactory.SupportedScanner> adapter = new ScannerAdapter(
                this,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanner.values()
        );

        // Set the current scanner
        sScannerSelector.setAdapter( adapter );
        sScannerSelector.setSelection( PrefUtils.getScanner( ac ) );
    }

    /**
     * It re-calculates the balance every time the value TextViews change
     */
    private void initializeTextListeners() {
        TextWatcher onChange = new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {}

            @Override
            public void afterTextChanged( Editable s ) {
                requestBalance();
            }
        };

        tvTotal.addTextChangedListener( onChange );
        tvCashtender.addTextChangedListener( onChange );
        tvCashback.addTextChangedListener( onChange );
    }

    /**
     * Generates the main layout of the popups
     */
    private void initializePopups() {
        LinearLayout viewGroup = (LinearLayout) findViewById( R.id.popup_window );
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // Popup for discount
        mPopupDiscount = new PopupWindow( ac );
        View lDiscount = layoutInflater.inflate( R.layout.popup_window, viewGroup );

        tvpDiscount = (TextView) lDiscount.findViewById( R.id.cashTenderText );
        GUIUtils.setMerchantCurrencyIcon( ac, tvpDiscount );

        mPopupDiscount.setWidth( ViewGroup.LayoutParams.WRAP_CONTENT );
        mPopupDiscount.setHeight( ViewGroup.LayoutParams.WRAP_CONTENT );
        mPopupDiscount.setContentView( lDiscount );

        // Popup for tender
        mPopupTender = new PopupWindow( ac );
        View lTender = layoutInflater.inflate( R.layout.popup_window, viewGroup );

        tvpTender = (TextView) lTender.findViewById( R.id.cashTenderText );
        pbpTender = (ProgressBar) lTender.findViewById( R.id.progressBarPopUp );
        GUIUtils.setMerchantCurrencyIcon( ac, tvpTender );

        mPopupTender.setWidth( ViewGroup.LayoutParams.WRAP_CONTENT );
        mPopupTender.setHeight( ViewGroup.LayoutParams.WRAP_CONTENT );
        mPopupTender.setContentView( lTender );

        // Setup the preview of the Total with discount
        tvTotal.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                mPopupDiscount.showAtLocation( v, Gravity.CENTER, 0, 0 );
                return false;
            }
        } );

        // Setup the dismiss of the preview
        tvTotal.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch( event.getAction() ) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupDiscount != null )
                            mPopupDiscount.dismiss();
                        break;
                }
                return false;
            }
        } );

        // Setup the preview of the Tender in the current currency
        tvCashtender.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                mPopupTender.showAtLocation( v, Gravity.CENTER, 0, 0 );
                return false;
            }
        } );

        // Setup the dismiss of the preview
        tvCashtender.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch( event.getAction() ) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupTender != null )
                            mPopupTender.dismiss();
                        break;
                }
                return false;
            }
        } );
    }

    /**
     * Gets the used currencies (merchant and tender) and requests their rates from the
     * server or cache
     */
    private void requestBalance() {
        // Balance GUI
        tvBalance.setVisibility( View.GONE );
        pgBalance.setVisibility( View.VISIBLE );

        // Popup GUI
        tvpTender.setVisibility( View.GONE );
        pbpTender.setVisibility( View.VISIBLE );

        String merchantCurr = PrefUtils.getMerchantCurrency( ac );
        String tenderCurr = PrefUtils.getTenderCurrency( ac );
        mRequestManager.requestCurrencies( CURR_REQ, merchantCurr, tenderCurr );
    }

    /**
     * Sets the default currency and sets the icon
     */
    private void setDefaultCurrency() {
        PrefUtils.saveTenderCurrency( ac, PrefUtils.getMerchantCurrency( ac ) );
        GUIUtils.setTenderCurrencyIcon( ac, tvCashtender );
        requestBalance();
    }

    /**
     * Button Actions.
     * {{ ==============================================
     */

    /**
     * Sets the currency for a transaction
     * @param v View, used to get the title
     */
    public void setCurrencyClick(View v) {
        splActivityLauncher.closePane();
        mCurrencyOption.execute();
    }

    public void currency( Drawable icon ) {
        icon.setBounds( 0, 0, tvCashtender.getLineHeight(), (int) ( tvCashtender.getLineHeight() * 0.9 ) );
        tvCashtender.setCompoundDrawables( icon, null, null, null );
        // Request the tender value in the rate of the merchant currency
        requestBalance();
    }

    /**
     * Sets a discount for all the transactions
     * @param v The view is not used
     */
    public void setDiscountClick( View v ) {
        splActivityLauncher.closePane();
        mDiscountOption.execute();
    }

    public void discount( EditText inputBox ) {
        final String discount = inputBox.getText().toString();
        if( discount.length() > 0 ) {
            PrefUtils.saveDiscount( ac, discount );
            GUIUtils.setViewIcon( ac, tvTotal, R.drawable.discount );
        } else {
            PrefUtils.saveDiscount( ac, AppConfig.DEFAULT_DISCOUNT );
            GUIUtils.setViewIcon( ac, tvTotal, null );
        }
        requestBalance();
    }

    /**
     * Opens the settings activity
     * @param v View, not used
     */
    public void settingsClick( View v ) {
        splActivityLauncher.closePane();
        Intent intent = new Intent( ac, SettingsActivity.class );
        startActivityForResult( intent, REQUEST_SETTINGS );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick( View v ) {
        splActivityLauncher.closePane();
        mBalanceOption.execute();
    }

    /**
     * Shows some basic information about the POS
     * @param v View, not used
     */
    public void aboutClick( View v ) {
        splActivityLauncher.closePane();
        mAboutOption.execute();
    }

    /**
     * Logout the user
     * @param v View, not used
     */
    public void logoutClick( View v ) {
        PrefUtils.saveLoginStatus( ac, false );
        PrefUtils.saveLogoUrl( ac, "" );
        finish();
    }

    /**
     * Change the selected view (purchase, cash tender, or cash back)
     * @param v The selected view
     */
    public void selectClick( View v ) {
        if( tvSelected != null )
            tvSelected.setBackgroundResource( R.drawable.show_text_field );

        tvSelected = ( (TextView) v );
        tvSelected.setBackgroundResource( R.drawable.selected_text_field );
    }

    /**
     * Resets the values to 0.00
     * @param v The View, not used
     */
    public void resetClick(View v) {
        String zero = getString( R.string.zero );

        if( externBundle == null )
            tvTotal.setText( zero );
        tvCashtender.setText( zero );
        tvCashback.setText( zero );
        tvBalance.setText( zero );
    }

    /**
     * Handle numeric button clicked
     * @param v View, used to get the number
     */
    @SuppressWarnings( "unused" )
    public void valueClick( View v ) {
        final String value = ( (Button) v ).getText().toString();

        // This button is not working value_.
        if( value.equals( getString( R.string.value__ ) ) )
            return;

        for( int i = 0; i < value.length(); i++ ) {
            final String current = tvSelected.getText().toString();

            BigDecimal temp = new BigDecimal( current + value );
            final String tvNewValue = temp.multiply( BigDecimal.TEN ).setScale( 2, RoundingMode.DOWN ).toString();
            tvSelected.setText( tvNewValue );
        }
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    @SuppressWarnings( "unused" )
    public void addClick( View v ) {
        final String amount  = ((Button)v).getText().toString();
        final String current = tvSelected.getText().toString();

        if( amount.equals( getString( R.string.coins_0 ) ) ) {
            tvSelected.setText( getString( R.string.zero ) );
        } else {
            BigDecimal value = new BigDecimal( current ).add( new BigDecimal( amount ) );
            final String tvNewValue = value.setScale( 2, RoundingMode.DOWN ).toString();
            tvSelected.setText( tvNewValue );
        }
    }

    /**
     * Handles on back pressed
     * @param v View, not used
     */
    public void backClick( View v ) {
        onBackPressed();
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

    /**
     * Request the permission for the camera
     */
    private void showCamera() {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.stopScan();
            return;
        }

        GUIUtils.rotateImage( ivYodoGear );
        currentScanner = mScannerFactory.getScanner(
                (QRScannerFactory.SupportedScanner) sScannerSelector.getSelectedItem()
        );

        if( currentScanner != null )
            currentScanner.startScan();
    }

    @Override
    public void onScanResult( String data ) {
        // Get subtotal with discount
        BigDecimal discount = new BigDecimal( PrefUtils.getDiscount( ac ) ).movePointLeft( 2 );
        BigDecimal subTotal = new BigDecimal( tvTotal.getText().toString() ).multiply(
                BigDecimal.ONE.subtract( discount )
        );

        final String cashtender = tvCashtender.getText().toString();
        final String cashback   = tvCashback.getText().toString();

        SystemUtils.Logger( TAG, data );

        SKS code = SKS.build( data );
        if( code == null ) {
            ToastMaster.makeText( LauncherActivity.this, R.string.exchange_error, Toast.LENGTH_LONG ).show();
        } else {
            final String client = code.getClient();
            final SKS.PAYMENT method = code.getPaymentMethod();
            ex_tip = subTotal.multiply(
                    code.getTip()
            ).setScale( 2, RoundingMode.DOWN ).toString();

            final String total = subTotal.multiply(
                    BigDecimal.ONE.add( code.getTip() )
            ).setScale( 2, RoundingMode.DOWN ).toString();

            mProgressManager.createProgressDialog(
                    LauncherActivity.this,
                    ProgressDialogHelper.ProgressDialogType.TRANSPARENT
            );

            switch( method ) {
                case YODO:
                    mRequestManager.requestExchange(
                            EXCH_REQ,
                            mHardwareToken,
                            client,
                            total,
                            cashtender,
                            cashback,
                            mLocation.getLatitude(),
                            mLocation.getLongitude(),
                            PrefUtils.getTenderCurrency( ac )
                    );
                    break;

                case HEART:
                    mRequestManager.requestAlternate(
                            ALT_REQ,
                            String.valueOf( method.ordinal() ),
                            mHardwareToken,
                            client,
                            total,
                            cashtender,
                            cashback,
                            mLocation.getLatitude(),
                            mLocation.getLongitude(),
                            PrefUtils.getTenderCurrency( ac )
                    );
                    break;
            }
        }
    }

    @Override
    public void onPrepare() {
        if( PrefUtils.isAdvertising( ac ) ) {
            isPublishing = true;
            mPromotionManager.unpublish();
        }
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();
        String code, message;

        // If it was publishing before the request
        if( isPublishing ) {
            isPublishing = false;
            mPromotionManager.publish();
        }

        switch( responseCode ) {

            case QRY_LOG_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String logoName = response.getParam( ServerResponse.LOGO );

                    if( logoName != null ) {
                        String logo_url = AppConfig.LOGO_PATH + logoName;
                        PrefUtils.saveLogoUrl( ac, logo_url );
                        nivCompanyLogo.setImageUrl( logo_url, mRequestManager.getImageLoader() );
                    }
                }
                break;

            case CURR_REQ:
                final String sMerchRate  = response.getParam( ServerResponse.MERCH_RATE );
                final String sTenderRate = response.getParam( ServerResponse.TENDER_RATE );

                if( sMerchRate != null && sTenderRate != null ) {
                    // Get the rates in BigDecimals
                    BigDecimal merchRate  = new BigDecimal( sMerchRate );
                    BigDecimal tenderRate = new BigDecimal( sTenderRate );

                    // Get the values of the money TextViews
                    BigDecimal total      = new BigDecimal( tvTotal.getText().toString() );
                    BigDecimal cashtender = new BigDecimal( tvCashtender.getText().toString() );
                    BigDecimal cashback   = new BigDecimal( tvCashback.getText().toString() );

                    // Get the tender in merchant currency
                    BigDecimal merchTender = cashtender.multiply(
                            merchRate.divide( tenderRate, 2, RoundingMode.DOWN )
                    );

                    tvpTender.setText( FormatUtils.truncateDecimal( merchTender.toString() ) );
                    pbpTender.setVisibility( View.GONE );
                    tvpTender.setVisibility( View.VISIBLE );

                    // Get subtotal with discount
                    BigDecimal discount = new BigDecimal( PrefUtils.getDiscount( ac ) ).movePointLeft( 2 );
                    BigDecimal subTotal = total.multiply(
                            BigDecimal.ONE.subtract( discount )
                    );

                    tvpDiscount.setText( FormatUtils.truncateDecimal( subTotal.toString() ) );

                    // Get balance
                    BigDecimal balance = merchTender.subtract(
                            subTotal.add( cashback )
                    );

                    tvBalance.setText( FormatUtils.truncateDecimal( balance.toString() ) );
                    tvBalance.setVisibility( View.VISIBLE );
                    pgBalance.setVisibility( View.GONE );
                }

                break;

            case EXCH_REQ:
            case ALT_REQ:
                code = response.getCode();
                final String ex_code       = response.getCode();
                final String ex_authbumber = response.getAuthNumber();
                final String ex_message    = response.getMessage();
                final String ex_account    = response.getParam( ServerResponse.ACCOUNT );
                final String ex_purchase   = response.getParam( ServerResponse.PURCHASE );
                final String ex_delta      = response.getParam( ServerResponse.AMOUNT_DELTA );

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    message = getString( R.string.exchange_auth ) + " " + ex_authbumber + "\n" +
                              getString( R.string.exchange_message ) + " " + ex_message;

                    DialogInterface.OnClickListener onClick;

                    if( externBundle != null ) {
                        final Intent data = new Intent();
                        data.putExtra( Intents.RESULT_CODE, ex_code );
                        data.putExtra( Intents.RESULT_AUTH, ex_authbumber );
                        data.putExtra( Intents.RESULT_MSG, ex_message );
                        data.putExtra( Intents.RESULT_ACC, ex_account );
                        data.putExtra( Intents.RESULT_PUR, ex_purchase );
                        data.putExtra( Intents.RESULT_TIP, ex_tip );
                        data.putExtra( Intents.RESULT_DEL, ex_delta );
                        setResult( RESULT_OK, data );

                        onClick = new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int item ) {
                                finish();
                            }
                        };
                    } else {
                        onClick = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick( DialogInterface dialog, int which ) {
                                setDefaultCurrency();
                                resetClick( null );
                            }
                        };
                    }

                    if( prompt_response )
                        AlertDialogHelper.showAlertDialog( ac, response.getCode(), message , onClick );
                    else finish();
                } else {
                    message  = response.getMessage() + "\n" + response.getParam( XMLHandler.PARAMS );
                    MessageHandler.sendMessage( mHandlerMessages, code, message );
                }
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
    public void onConnected( @Nullable Bundle bundle ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connected" );
        if( PrefUtils.isAdvertising( ac ) )
            mPromotionManager.publish();
    }

    @Override
    public void onConnectionSuspended( int i ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connection suspended" );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult result ) {
        SystemUtils.Logger( TAG, "connection to GoogleApiClient failed" );
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
                    LocationService.enable( this, REQUEST_CODE_LOCATION_SERVICES );
                } else {
                    // Permission Denied
                    PrefUtils.saveLocating( ac, false );
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        switch( requestCode ) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if( !SystemUtils.isLocationEnabled( ac ) )
                    PrefUtils.saveLocating( ac, false );
                break;

            case REQUEST_SETTINGS:
                if( resultCode == RESULT_FIRST_USER ) {
                    setResult( RESULT_FIRST_USER );
                    finish();
                }
                break;
        }
    }
}
