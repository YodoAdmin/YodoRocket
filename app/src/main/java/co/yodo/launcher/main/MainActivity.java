package co.yodo.launcher.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import co.yodo.launcher.R;
import co.yodo.launcher.component.ToastMaster;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.helper.Intents;
import co.yodo.launcher.net.YodoRequest;
import co.yodo.launcher.service.LocationService;

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

    /** Bundle in case of external call */
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        AppUtils.setLanguage( this );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_main );

        setupGUI();
        updateData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }
    }

    private void setupGUI() {
        ac = MainActivity.this;

        handlerMessages = new YodoHandler( MainActivity.this );
        YodoRequest.getInstance().setListener( this );

        Intent iLoc = new Intent( ac, LocationService.class );
        if( AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) )
            stopService( iLoc );
        startService( iLoc );
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

        hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else if( AppUtils.getMerchantCurrency( ac ) == null ||  !AppUtils.isLoggedIn( ac ) ) {
            YodoRequest.getInstance().requestAuthentication( MainActivity.this, hardwareToken );
        } else {
            intent = new Intent( MainActivity.this, LauncherActivity.class );
            if( bundle != null ) intent.putExtras( bundle );
            startActivityForResult( intent, AppConfig.LAUNCHER_REQUEST );
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
                    startActivityForResult( intent, AppConfig.REGISTRATION_REQUEST );
                }

                break;

            case QUERY_CUR_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String currency = response.getParam( ServerResponse.CURRENCY );
                    AppUtils.saveMerchantCurrency( ac, currency );
                    // Start the app
                    AppUtils.saveLoginStatus( ac, true );
                    Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
                    if( bundle != null ) intent.putExtras( bundle );
                    startActivityForResult( intent , AppConfig.LAUNCHER_REQUEST );
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
                case AppConfig.REGISTRATION_REQUEST:
                    YodoRequest.getInstance().requestCurrency( MainActivity.this, hardwareToken );
                    break;

                case AppConfig.LAUNCHER_REQUEST:
                    setResult( RESULT_OK, data );
                    finish();
                    break;
            }
        }
        else if( resultCode == RESULT_CANCELED ) {
            finish();
        }
        else if( resultCode == RESULT_FIRST_USER ) {
            AppUtils.setLanguage( this );
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            if( bundle != null ) intent.putExtras( bundle );
            startActivityForResult( intent, AppConfig.LAUNCHER_REQUEST );
        }
    }
}
