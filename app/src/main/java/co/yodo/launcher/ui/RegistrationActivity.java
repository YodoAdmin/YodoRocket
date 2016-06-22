package co.yodo.launcher.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.notification.YodoHandler;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.model.ServerResponse;

public class RegistrationActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String hardwareToken;

    /** GUI Controllers */
    private EditText etPassword;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Manager for the server requests */
    private YodoRequest mRequestManager;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Response codes for the queries */
    private static final int REG_REQ = 0x00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GUIUtils.setLanguage( RegistrationActivity.this );
        setContentView(R.layout.activity_registration);

        setupGUI();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        // Get the context and handler for the messages
        ac = RegistrationActivity.this;
        handlerMessages = new YodoHandler( RegistrationActivity.this );
        mRequestManager = YodoRequest.getInstance( ac );
        mRequestManager.setListener( this );

        // GUI global components
        etPassword = (EditText) findViewById( R.id.merchTokenText );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.registrationBar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );
    }

    private void updateData() {
        if( PrefUtils.isLoggedIn( ac ) )
            finish();

        // Gets the hardware token - account identifier
        hardwareToken = PrefUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        // Get the token
        String token = etPassword.getText().toString();

        if( token.isEmpty() ) {
            etPassword.startAnimation( aShake );
        } else {
            GUIUtils.hideSoftKeyboard( this );

            ProgressDialogHelper.getInstance().createProgressDialog(
                    ac,
                    ProgressDialogHelper.ProgressDialogType.NORMAL
            );
            mRequestManager.requestMerchReg(
                    REG_REQ,
                    hardwareToken,
                    token
            );
        }
    }

    public void showPasswordClick( View v ) {
        GUIUtils.showPassword( (CheckBox) v, etPassword );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        ProgressDialogHelper.getInstance().destroyProgressDialog();

        switch( responseCode ) {
            case REG_REQ:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    setResult( RESULT_OK );
                    finish();
                } else {
                    String message = response.getMessage();
                    YodoHandler.sendMessage( handlerMessages, code, message );
                }
                break;
        }
    }
}
