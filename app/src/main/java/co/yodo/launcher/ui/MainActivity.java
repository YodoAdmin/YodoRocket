package co.yodo.launcher.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Arrays;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.component.ToastMaster;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.helper.Intents;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.builder.ServerRequest;
import co.yodo.restapi.network.model.ServerResponse;

public class MainActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** Code for the error dialog */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    /** Request Code (Activity Results) */
    private static final int ACTIVITY_REGISTRATION_REQUEST = 1;
    private static final int ACTIVITY_LAUNCHER_REQUEST     = 2;

    /** Response codes for the server requests */
    private static final int AUTH_REQ  = 0x00;
    private static final int QUERY_REQ = 0x01;

    /** Bundle in case of external call */
    private Bundle bundle;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );
        setContentView( R.layout.activity_splash );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
    }

    private void setupGUI() {
        // Get the context and handler for the messages
        ac = MainActivity.this;
        handlerMessages = new YodoHandler( MainActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );
    }

    private void updateData() {
        /** Handle external Requests */
        Intent intent = getIntent();
        if( intent != null ) {
            String action = intent.getAction();
            if( Intents.ACTION.equals( action ) ) {
                bundle = intent.getExtras();
                if( bundle == null ) bundle = new Bundle();
            }
        }
        /*****************************/

        // Get the main booleans
        boolean hasServices = AppUtils.isGooglePlayServicesAvailable(
                MainActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );
        boolean isLegacy = AppUtils.isLegacy( ac );

        // Verify Google Play Services
        if( hasServices || isLegacy ) {
            hardwareToken = AppUtils.getHardwareToken( ac );
            if( hardwareToken == null ) {
                setupPermissions();
            } else if( !AppUtils.isLoggedIn( ac ) || AppUtils.getMerchantCurrency( ac ) == null ) {
                mRequestManager.requestMerchAuth( AUTH_REQ, hardwareToken );
            } else {
                intent = new Intent( ac, LauncherActivity.class );
                if( bundle != null ) intent.putExtras( bundle );
                startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = AppUtils.requestPermission(
                MainActivity.this,
                R.string.message_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission )
            authenticateUser();
    }

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    private void authenticateUser() {
        hardwareToken = AppUtils.generateHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else if( AppUtils.getMerchantCurrency( ac ) == null || !AppUtils.isLoggedIn( ac ) ) {
            AppUtils.saveHardwareToken( ac, hardwareToken );
            mRequestManager.requestMerchAuth( AUTH_REQ, hardwareToken );
        } else {
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            if( bundle != null ) intent.putExtras( bundle );
            startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
        }
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        String code, message;

        switch( responseCode ) {
            case AUTH_REQ:
                code = response.getCode();

                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        // Get the merchant currency
                        mRequestManager.requestQuery(
                                QUERY_REQ,
                                hardwareToken,
                                ServerRequest.QueryRecord.MERCHANT_CURRENCY
                        );
                        break;

                    case ServerResponse.ERROR_FAILED:
                        AppUtils.saveLoginStatus( ac, false );
                        Intent intent = new Intent( MainActivity.this, RegistrationActivity.class );
                        startActivityForResult( intent, ACTIVITY_REGISTRATION_REQUEST );
                        break;

                    default:
                        message = response.getMessage();
                        AppUtils.sendMessage( YodoHandler.INIT_ERROR, handlerMessages, code, message );
                        break;
                }

                break;

            case QUERY_REQ:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Merchant Currency
                    String currency = response.getParam( ServerResponse.CURRENCY );
                    AppUtils.saveMerchantCurrency( ac, currency );
                    // POS Currency
                    final String[] currencies = getResources().getStringArray( R.array.currency_array );
                    int position = Arrays.asList( currencies ).indexOf( currency );
                    AppUtils.saveCurrency( ac, position );
                    // Start the app
                    AppUtils.saveLoginStatus( ac, true );
                    Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
                    if( bundle != null ) intent.putExtras( bundle );
                    startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
                } else {
                    message = response.getMessage();
                    AppUtils.sendMessage( YodoHandler.INIT_ERROR, handlerMessages, code, message );
                }

                break;
        }
    }

    /**
     * Gets the result from the activities called with the function startActivityForResult
     * @param requestCode The code that indicates which activity was started with startActivityForResult
     * @param resultCode If the result was ok or not
     * @param data The result intent sent by the activity
     */
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( resultCode == RESULT_OK ) {
            switch( requestCode ) {
                case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                    // Google play services installed, start the app again
                    AppUtils.setLegacy( ac, false );
                    Intent iSplash = new Intent( this, MainActivity.class );
                    iSplash.putExtras( bundle );
                    startActivity( iSplash );
                    finish();
                    break;

                case ACTIVITY_REGISTRATION_REQUEST:
                    // Get the merchant currency
                    mRequestManager.requestQuery(
                            QUERY_REQ,
                            hardwareToken,
                            ServerRequest.QueryRecord.MERCHANT_CURRENCY
                    );
                    break;

                case ACTIVITY_LAUNCHER_REQUEST:
                    setResult( RESULT_OK, data );
                    finish();
                    break;
            }
        }
        else if( resultCode == RESULT_CANCELED ) {
            finish();
            switch( requestCode ) {
                case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                    // Denied to install, restart in legacy mode
                    AppUtils.setLegacy( ac, true );
                    Intent iSplash = new Intent( this, MainActivity.class );
                    iSplash.putExtras( bundle );
                    startActivity( iSplash );
                    finish();
                    break;
            }
        }
        else if( resultCode == RESULT_FIRST_USER ) {
            AppUtils.setLanguage( this );
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            if( bundle != null ) intent.putExtras( bundle );
            startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    authenticateUser();
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