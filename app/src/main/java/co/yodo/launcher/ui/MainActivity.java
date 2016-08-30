package co.yodo.launcher.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import javax.inject.Inject;

import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.component.Intents;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.AuthenticateRequest;
import co.yodo.restapi.network.request.QueryRequest;

public class MainActivity extends AppCompatActivity implements ApiClient.RequestsListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = MainActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String mHardwareToken;

    /** Messages Handler */
    private MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

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
        GUIUtils.setLanguage( this );
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
        mHandlerMessages = new MessageHandler( MainActivity.this );
        YodoApplication.getComponent().inject( this );
        mRequestManager.setListener( this );
    }

    private void updateData() {
        /*********** Handle external Requests *************/
        /**************************************************/
        Intent intent = getIntent();
        if( intent != null ) {
            String action = intent.getAction();
            if( Intents.ACTION.equals( action ) ) {
                bundle = intent.getExtras();
                //if( bundle == null ) bundle = new Bundle();
            }
        }
        /**************************************************/

        // Get the main system booleans
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                MainActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );
        boolean isLegacy = PrefUtils.isLegacy( ac );

        // Get the main user booleans
        boolean isLoggedIn = PrefUtils.isLoggedIn( ac );
        boolean hasMerchCurr = PrefUtils.getMerchantCurrency( ac ) != null;
        boolean hasTenderCurr = PrefUtils.getTenderCurrency( ac ) != null;

        // Verify Google Play Services
        if( hasServices || isLegacy ) {
            mHardwareToken = PrefUtils.getHardwareToken( ac );
            if( mHardwareToken == null ) {
                setupPermissions();
            } else if( !isLoggedIn || !hasMerchCurr || !hasTenderCurr ) {
                mRequestManager.invoke(
                        new AuthenticateRequest(
                                AUTH_REQ,
                                mHardwareToken
                        )
                );
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
        boolean phoneStatePermission = SystemUtils.requestPermission(
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
        mHardwareToken = PrefUtils.generateHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            // We have the hardware token, now let's verify if the user exists
            PrefUtils.saveHardwareToken( ac, mHardwareToken );
            mRequestManager.invoke(
                    new AuthenticateRequest(
                            AUTH_REQ,
                            mHardwareToken
                    )
            );
        }
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Get response values
        String code    = response.getCode();
        String message = response.getMessage();

        switch( responseCode ) {
            case AUTH_REQ:

                switch( code ) {
                    case ServerResponse.AUTHORIZED:
                        // Get the merchant currency
                        mRequestManager.invoke(
                                new QueryRequest(
                                        QUERY_REQ,
                                        mHardwareToken,
                                        QueryRequest.Record.MERCHANT_CURRENCY
                                )
                        );
                        break;

                    case ServerResponse.ERROR_FAILED:
                        PrefUtils.saveLoginStatus( ac, false );
                        Intent intent = new Intent( ac, RegistrationActivity.class );
                        startActivityForResult( intent, ACTIVITY_REGISTRATION_REQUEST );
                        break;

                    default:
                        MessageHandler.sendMessage( MessageHandler.INIT_ERROR,
                                mHandlerMessages,
                                code,
                                message
                        );
                        break;
                }

                break;

            case QUERY_REQ:

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Set currencies
                    String currency = response.getParams().getCurrency();
                    PrefUtils.saveMerchantCurrency( ac, currency );
                    PrefUtils.saveTenderCurrency( ac, currency );

                    // Start the app
                    PrefUtils.saveLoginStatus( ac, true );
                    Intent intent = new Intent( ac, LauncherActivity.class );
                    if( bundle != null ) intent.putExtras( bundle );
                    startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
                } else {
                    MessageHandler.sendMessage( MessageHandler.INIT_ERROR,
                            mHandlerMessages,
                            code,
                            message
                    );
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
                    PrefUtils.setLegacy( ac, false );
                    Intent iSplash = new Intent( this, MainActivity.class );
                    iSplash.putExtras( bundle );
                    startActivity( iSplash );
                    finish();
                    break;

                case ACTIVITY_REGISTRATION_REQUEST:
                    // Get the merchant currency
                    mRequestManager.invoke(
                            new QueryRequest(
                                    QUERY_REQ,
                                    mHardwareToken,
                                    QueryRequest.Record.MERCHANT_CURRENCY
                            )
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
                    PrefUtils.setLegacy( ac, true );
                    Intent iSplash = new Intent( ac, MainActivity.class );
                    iSplash.putExtras( bundle );
                    startActivity( iSplash );
                    finish();
                    break;
            }
        }
        else if( resultCode == RESULT_FIRST_USER ) {
            Intent intent = new Intent( ac, LauncherActivity.class );
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
