package co.yodo.launcher.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import co.yodo.launcher.R;
import co.yodo.launcher.adapter.CurrencyAdapter;
import co.yodo.launcher.broadcastreceiver.BroadcastMessage;
import co.yodo.launcher.component.ClearEditText;
import co.yodo.launcher.component.ImageLoader;
import co.yodo.launcher.component.JsonParser;
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
import co.yodo.launcher.scanner.QRScannerListener;
import co.yodo.launcher.service.RESTService;

public class LauncherActivity extends AppCompatActivity implements YodoRequest.RESTListener, QRScannerListener {
    /** DEBUG */
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** The Local Broadcast Manager */
    private LocalBroadcastManager lbm;

    /** Orientation detector */
    private OrientationEventListener mOrientationListener;
    private int mLastRotation;

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
    private ImageView mLogoImage;
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

    /** Balance Temp */
    private HashMap<String, String> historyData;
    private HashMap<String, String> todayData;

    /** Location */
    private Location location;

    /** Mock data for location */
    private static final String PROVIDER = "flp";
    private static final double LAT = 0.00;
    private static final double LNG = 0.00;

    /** Current Scanners */
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** External data */
    private Bundle externBundle;
    private boolean prompt_response = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_launcher);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcasts();

        AppUtils.setupAdvertising( ac, AppUtils.isAdvertisingServiceRunning( ac ), false );

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
        unregisterBroadcasts();

        if( currentScanner != null && currentScanner.isScanning() )
            isScanning = true;

        QRScannerFactory.destroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mOrientationListener.disable();
        QRScannerFactory.destroy();
    }

    @Override
    public void onBackPressed() {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.destroy();
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
        // get local broadcast
        lbm = LocalBroadcastManager.getInstance( ac );
        // Loads images from urls
        imageLoader = new ImageLoader( ac );

        // Globals
        mRootLayout      = (LinearLayout) findViewById( R.id.screenRootView );
        mSlidingLayout   = (SlidingPaneLayout) findViewById( R.id.sliding_panel_layout );
        mBalanceView     = (TextView) findViewById( R.id.balanceText );
        mScannersSpinner = (Spinner) findViewById(R.id.scannerSpinner);
        mTotalView       = (TextView) findViewById( R.id.totalText );
        mCashTenderView  = (TextView) findViewById( R.id.cashTenderText );
        mCashBackView    = (TextView) findViewById( R.id.cashBackText );
        mLogoImage       = (ImageView) findViewById( R.id.companyLogo );
        mBalanceBar      = (ProgressBar) findViewById( R.id.progressBarBalance );

        // Popup
        mPopupMessage  = new PopupWindow( ac );

        // Sliding Panel Configurations
        mSlidingLayout.setParallaxDistance( 30 );

        mScannersSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if( mSlidingLayout.isOpen() )
                    mSlidingLayout.closePane();

                ( (TextView) parentView.getChildAt( 0 ) ).setTextColor( Color.WHITE );
                AppUtils.saveScanner( ac, position );
                AppUtils.Logger(TAG, ((TextView) selectedItemView).getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<QRScannerFactory.SupportedScanners> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanners.values()
        );

        mScannersSpinner.setAdapter( adapter );
        mScannersSpinner.setSelection( AppUtils.getScanner( ac ) );

        handlerMessages   = new YodoHandler( LauncherActivity.this );
        YodoRequest.getInstance().setListener( this );

        selectedView = mTotalView;
        selectedView.setBackgroundResource( R.drawable.selected_text_field );

        if( AppUtils.hasLocationService( ac ) && !AppUtils.isLocationEnabled( ac ) ) {
            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                    startActivity( intent );
                }
            };

            AlertDialogHelper.showAlertDialog( ac, R.string.gps_enable, onClick );
        }

        mLastRotation = getWindowManager().getDefaultDisplay().getRotation();
        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = getWindowManager().getDefaultDisplay().getRotation();

                if( rotation != mLastRotation ) {

                    if( currentScanner != null && currentScanner.isScanning() ) {
                        currentScanner.close();
                        currentScanner.startScan();
                    }

                    mLastRotation = rotation;
                }
            }
        };

        if( mOrientationListener.canDetectOrientation() ) {
            mOrientationListener.enable();
        }

        if( AppUtils.isFirstLogin( ac ) ) {
            /*new ShowcaseView.Builder( this )
                    .setTarget( new ViewTarget( R.id.optionsView, this ) )
                    .setContentTitle( R.string.tutorial_title )
                    .setContentText( R.string.tutorial_message )
                    .build();*/

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
            /*String total = String.format( Locale.US, "%.2f", externBundle.getDouble( Intents.TOTAL, 0.00 ) );
            String cashTender = String.format( Locale.US, "%.2f", externBundle.getDouble( Intents.CASH_TENDER, 0.00 ) );
            String cashBack = String.format( Locale.US, "%.2f", externBundle.getDouble( Intents.CASH_BACK, 0.00 ) );

            if( Double.valueOf( total ) > 0.00 ) mTotalView.setText( total );
            if( Double.valueOf( cashTender ) > 0.00 ) mCashTenderView.setText( cashTender );
            if( Double.valueOf( cashBack ) > 0.00 ) mCashBackView.setText( cashBack );*/

            BigDecimal total = new BigDecimal( externBundle.getString( Intents.TOTAL, "0.00" ) );
            BigDecimal cashTender = new BigDecimal( externBundle.getString( Intents.CASH_TENDER, "0.00" ) );
            BigDecimal cashBack = new BigDecimal( externBundle.getString( Intents.CASH_BACK, "0.00" ) );
            String currency = externBundle.getString( Intents.CURRENCY );

            if( total.signum() > 0 ) mTotalView.setText( total.setScale( 2, RoundingMode.DOWN ).toString() );
            if( cashTender.signum() > 0 ) mCashTenderView.setText( cashTender.setScale( 2, RoundingMode.DOWN ).toString() );
            if( cashBack.signum() > 0 ) mCashBackView.setText( cashBack.setScale( 2, RoundingMode.DOWN ).toString() );

            viewClick( mCashTenderView );

            // Handle the prompt of the response message from the server
            prompt_response = externBundle.getBoolean( Intents.PROMPT_RESPONSE, true );
        }
        /*****************************/

        hardwareToken = AppUtils.getHardwareToken( ac );
        String logo_url = AppUtils.getLogoUrl( ac );

        if( logo_url.equals( "" ) ) {
            YodoRequest.getInstance().requestLogo( LauncherActivity.this, hardwareToken );
        } else {
            imageLoader.DisplayImage( logo_url, mLogoImage );
        }

        AppUtils.setCurrencyIcon( ac, mCashTenderView, false );

        mCashTenderView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setupPopup( v );
                return false;
            }
        });

        mCashTenderView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupMessage != null )
                            mPopupMessage.dismiss();
                        break;
                }
                return false;
            }
        });

        new getCurrentBalance().execute();

        location = new Location( PROVIDER );
        location.setLatitude( LAT );
        location.setLongitude( LNG );
    }

    /**
     * Setup a PopupWindow below a View
     * @param v The view for te popup
     */
    private void setupPopup(View v) {
        LinearLayout viewGroup = (LinearLayout) findViewById( R.id.popup_window );
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = layoutInflater.inflate( R.layout.popup_window, viewGroup );

        TextView cashTender     = (TextView) layout.findViewById( R.id.cashTenderText );
        ProgressBar progressBar = (ProgressBar) layout.findViewById( R.id.progressBarPopUp );
        cashTender.setText( equivalentTender.setScale( 2, RoundingMode.DOWN ).toString() );
        AppUtils.setCurrencyIcon( ac, cashTender, true );

        if( equivalentTender == null ) {
            cashTender.setVisibility( View.GONE );
            progressBar.setVisibility( View.VISIBLE );
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
     * Changes the current language
     * @param v View, used to get the title
     */
    public void setLanguageClick(View v) {
        mSlidingLayout.closePane();

        final String title       = ((Button) v).getText().toString();
        final String[] languages = getResources().getStringArray( R.array.languages_array );
        final int current        = AppUtils.getLanguage( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();

                ToastMaster.makeText( ac, languages[item], Toast.LENGTH_SHORT ).show();
                AppUtils.saveLanguage( ac, item );

                setResult( RESULT_FIRST_USER );
                finish();
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                languages,
                current,
                onClick
        );
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
            public void onClick(DialogInterface dialog, int item) {
                AppUtils.saveCurrency( ac, item );

                Drawable icon = AppUtils.getDrawableByName( ac, icons[ item ] );
                icon.setBounds( 0, 0, mCashTenderView.getLineHeight(), (int)( mCashTenderView.getLineHeight() * 0.9 ) );
                mCashTenderView.setCompoundDrawables( icon, null, null, null );
                new getCurrentBalance().execute();

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

    /**
     * Opens the settings activity
     * @param v View, not used
     */
    public void settingsClick(View v) {
        mSlidingLayout.closePane();

        Intent intent = new Intent( ac, SettingsActivity.class );
        startActivity( intent );
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
        remember.setText( R.string.remember_pass);

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( LauncherActivity.this );

                if( remember.isChecked() )
                    AppUtils.savePassword( ac, pip );
                else
                    AppUtils.savePassword( ac, null );

                YodoRequest.getInstance().createProgressDialog(
                        LauncherActivity.this ,
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
                inputBox, true, true,
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
        final String message = getString( R.string.imei )       + " " +
                               AppUtils.getHardwareToken( ac ) + "\n" +
                               getString( R.string.version ) + "/" + RESTService.getSwitch();

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
        imageLoader.clearCache();
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
    public void valueClick(View v) {
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

        new getCurrentBalance().execute();
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    public void addClick(View v) {
        final String amount  = ((Button)v).getText().toString();
        final String current = selectedView.getText().toString();

        if( amount.equals( getString( R.string.coins_0 ) ) ) {
            selectedView.setText( getString( R.string.zero ) );
        } else {
            BigDecimal value = new BigDecimal( current ).add( new BigDecimal( amount ) );
            selectedView.setText( value.setScale( 2, RoundingMode.DOWN ).toString() );
        }
        new getCurrentBalance().execute();
    }

    /**
     * Handles on back pressed
     * @param v View, not used
     */
    public void backClick(View v) {
        onBackPressed();
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void yodoPayClick(View v) {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.close();
            return;
        }

        AppUtils.rotateImage( v );

        currentScanner = QRScannerFactory.getInstance(
                this,
                (QRScannerFactory.SupportedScanners) mScannersSpinner.getSelectedItem()
        );

        currentScanner.startScan();
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
                        AppUtils.saveLogoUrl(ac, AppConfig.LOGO_PATH + logoName );
                        imageLoader.DisplayImage( AppConfig.LOGO_PATH + logoName, mLogoImage );
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
                        data.putExtra( Intents.RESULT_AMO, ex_amount );
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
    public void onNewData(String data) {
        String totalPurchase = mTotalView.getText().toString();
        String cashTender    = mCashTenderView.getText().toString();
        String cashBack      = mCashBackView.getText().toString();

        final String[] currency = getResources().getStringArray( R.array.currency_array );

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
                        location.getLatitude(),
                        location.getLongitude(),
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
                        location.getLatitude(),
                        location.getLongitude(),
                        currency[ AppUtils.getCurrency( ac ) ]
                );
                break;

            default:
                ToastMaster.makeText( LauncherActivity.this, R.string.exchange_error, Toast.LENGTH_LONG ).show();
                break;
        }
    }

    /**
     * Register/Unregister the broadcast receiver.
     */
    private void registerBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction( BroadcastMessage.ACTION_NEW_LOCATION );

        lbm.registerReceiver(mLauncherBroadcastReceiver, filter);
    }

    private void unregisterBroadcasts() {
        lbm.unregisterReceiver(mLauncherBroadcastReceiver);
    }

    /**
     * The broadcast receiver for the location service, it will receive all the
     * updates from the location service and send it to the gateway.
     */
    private BroadcastReceiver mLauncherBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {

            String action = i.getAction();
			/* Broadcast: ACTION_NEW_LOCATION */
			/* ****************************** */
            if( action.equals( BroadcastMessage.ACTION_NEW_LOCATION ) ) {
                AppUtils.Logger(TAG, ">> LauncherActivity >> ACTION_NEW_LOCATION");

                Parcelable p = i.getParcelableExtra( BroadcastMessage.EXTRA_NEW_LOCATION );
                if( p != null && p instanceof Location ) {
                    location = (Location) p;
                }
            }
        }
    };

    public class getCurrentBalance extends AsyncTask<String, String, JSONArray> {
        /** JSON Url */
        private String url = RESTService.getRoot() + "/yodo/currency/index.json";

        /** JSON Tags */
        private String TAG = "YodoCurrency";
        private String CURRENCY_TAG = "currency";
        private String RATE_TAG = "rate";

        /** Currencies */
        private String[] currencies = ac.getResources().getStringArray( R.array.currency_array );
        private String URL_CURRENCY  = "EUR";
        //private String BASE_CURRENCY = "CAD";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            equivalentTender = null;
            mBalanceView.setVisibility( View.GONE );
            mBalanceBar.setVisibility( View.VISIBLE );
        }

        @Override
        protected JSONArray doInBackground(String... arg0) {
            // instantiate our json parser
            JsonParser jParser = new JsonParser( ac );
            // get json string from url
            return jParser.getJSONFromUrl( url );
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            if( json != null ) {
                BigDecimal cad_currency = null, current_currency = null;
                for( int i = 0; i < json.length(); i++ ) {
                    try {
                        JSONObject temp = json.getJSONObject( i );
                        JSONObject c    = (JSONObject) temp.get( TAG );
                        String currency = (String) c.get( CURRENCY_TAG );
                        String rate     = (String) c.get( RATE_TAG );

                        // Gets the CAD Currency
                        if( currency.equals( currencies[ AppConfig.DEFAULT_CURRENCY ] ) )
                            cad_currency = new BigDecimal( rate );

                        if( currency.equals( currencies[ AppUtils.getCurrency( ac ) ]) )
                            current_currency = new BigDecimal( rate );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String totalPurchase   = mTotalView.getText().toString();
                String cashBack        = mCashBackView.getText().toString();
                BigDecimal temp_tender = new BigDecimal( mCashTenderView.getText().toString() );

                if( cad_currency != null ) {
                    if( URL_CURRENCY.equals( currencies[ AppUtils.getCurrency( ac ) ] ) ) {
                        equivalentTender = temp_tender.multiply( cad_currency );
                    } else if( current_currency != null ) {
                        BigDecimal currency_rate = cad_currency.divide( current_currency, 2 );
                        equivalentTender = temp_tender.multiply( currency_rate );
                    }

                    BigDecimal total = equivalentTender.subtract(
                            new BigDecimal( totalPurchase ).add( new BigDecimal( cashBack ) )
                    );

                    mBalanceView.setText( total.setScale( 2, RoundingMode.DOWN ).toString() );
                    mBalanceView.setVisibility( View.VISIBLE );
                    mBalanceBar.setVisibility( View.GONE );

                    if( mPopupMessage.isShowing() ) {
                        mPopupMessage.getContentView().findViewById( R.id.progressBarPopUp ).setVisibility( View.GONE );
                        mPopupMessage.getContentView().findViewById( R.id.cashTenderText ).setVisibility( View.VISIBLE );
                    }
                }
            }
        }
    }
}
