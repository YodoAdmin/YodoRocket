package co.yodo.launcher.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Arrays;

import co.yodo.launcher.R;
import co.yodo.launcher.component.ToastMaster;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.helper.Intents;
import co.yodo.launcher.net.YodoRequest;

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

    /** Code for the error dialog */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    /** Request Code (Activity Results) */
    private static final int ACTIVITY_REGISTRATION_REQUEST = 1;
    private static final int ACTIVITY_LAUNCHER_REQUEST     = 2;

    /** Bundle in case of external call */
    private Bundle bundle;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    private void setupGUI() {
        // Get the context and handler for the messages
        ac = MainActivity.this;
        handlerMessages = new YodoHandler( MainActivity.this );
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
                YodoRequest.getInstance().requestAuthentication( MainActivity.this, hardwareToken );
            } else {
                intent = new Intent( MainActivity.this, LauncherActivity.class );
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
            YodoRequest.getInstance().requestAuthentication( ac, hardwareToken );
        } else {
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            if( bundle != null ) intent.putExtras( bundle );
            startActivityForResult( intent, ACTIVITY_LAUNCHER_REQUEST );
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                finish();
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                finish();
                break;

            case AUTH_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get the merchant currency
                    YodoRequest.getInstance().requestCurrency( MainActivity.this, hardwareToken );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    AppUtils.saveLoginStatus( ac, false );
                    Intent intent = new Intent( MainActivity.this, RegistrationActivity.class);
                    startActivityForResult( intent, ACTIVITY_REGISTRATION_REQUEST );
                }

                break;

            case QUERY_CUR_REQUEST:
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
                    startActivityForResult( intent , ACTIVITY_LAUNCHER_REQUEST );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage();

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                    finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == RESULT_OK ) {
            switch( requestCode ) {
                case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                    // Google play services installed
                    AppUtils.setLegacy( ac, false );
                    Intent iSplash = new Intent( this, MainActivity.class );
                    iSplash.putExtras( bundle );
                    startActivity( iSplash );
                    finish();
                    break;

                case ACTIVITY_REGISTRATION_REQUEST:
                    YodoRequest.getInstance().requestCurrency( MainActivity.this, hardwareToken );
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
                    // Denied to install
                    //ToastMaster.makeText( ac, R.string.message_play_services, Toast.LENGTH_SHORT ).show();
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
