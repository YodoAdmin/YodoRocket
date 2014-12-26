package co.yodo.launcher.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import co.yodo.launcher.R;
import co.yodo.launcher.component.ToastMaster;
import co.yodo.launcher.component.YodoHandler;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.net.YodoRequest;

public class MainActivity extends Activity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_main );

        setupGUI();
        updateData();
    }

    private void setupGUI() {
        ac = MainActivity.this;

        handlerMessages = new YodoHandler( MainActivity.this );
        YodoRequest.getInstance().setListener( this );
    }

    private void updateData() {
        String hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else if( !AppUtils.isLoggedIn( ac ) ) {
            YodoRequest.getInstance().requestAuthentication( MainActivity.this, hardwareToken );
        } else {
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            startActivity( intent );
            finish();
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        finish();

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_REQUEST:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.saveLoginStatus( ac, true );
                    Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
                    startActivity( intent );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    AppUtils.saveLoginStatus( ac, false );
                    Intent intent = new Intent( MainActivity.this, RegistrationActivity.class);
                    startActivity( intent );
                }

                break;
        }
    }
}
