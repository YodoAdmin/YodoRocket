package yodo.co.yodolauncher.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import yodo.co.yodolauncher.R;
import yodo.co.yodolauncher.component.ToastMaster;
import yodo.co.yodolauncher.component.YodoHandler;
import yodo.co.yodolauncher.helper.AppUtils;
import yodo.co.yodolauncher.data.ServerResponse;
import yodo.co.yodolauncher.net.YodoRequest;

public class MainActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    private static final String TAG = MainActivity.class.getSimpleName();

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
        String token = AppUtils.getHardwareToken( ac );
        if( token == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else if( !AppUtils.isLoggedIn( ac ) ) {
            YodoRequest.getInstance().requestAuthentication( this, token );
        } else {
            Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
            startActivity( intent );
            finish();
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        switch( type ) {
            case ERROR:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_REQUEST:
                finish();
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.saveLoginStatus( ac, true );
                    Intent intent = new Intent( MainActivity.this, LauncherActivity.class );
                    startActivity( intent );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    //Intent intent = new Intent( MainActivity.this, RegistrationActivity.class);
                    //startActivity( intent );
                }

                break;
        }
    }
}
