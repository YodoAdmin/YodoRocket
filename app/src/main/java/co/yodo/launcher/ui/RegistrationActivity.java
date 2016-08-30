package co.yodo.launcher.ui;

import android.content.Context;
import android.content.Intent;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.RegisterRequest;

public class RegistrationActivity extends AppCompatActivity implements ApiClient.RequestsListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Account identifiers */
    private String hardwareToken;

    /** GUI Controllers */
    @BindView( R.id.etActivationCode )
    EditText etActivationCode;

    /** Messages Handler */
    private static MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Response codes for the queries */
    private static final int REG_REQ = 0x00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( RegistrationActivity.this );
        setContentView( R.layout.activity_registration );

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
        mHandlerMessages = new MessageHandler( RegistrationActivity.this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );
        mRequestManager.setListener( this );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.actionBar );

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
        String token = etActivationCode.getText().toString();

        if( token.isEmpty() ) {
            etActivationCode.startAnimation( aShake );
        } else {
            GUIUtils.hideSoftKeyboard( this );

            mProgressManager.createProgressDialog(
                    ac,
                    ProgressDialogHelper.ProgressDialogType.NORMAL
            );
            mRequestManager.invoke(
                    new RegisterRequest(
                            REG_REQ,
                            hardwareToken,
                            token
                    )
            );
        }
    }

    /**
     * Restarts the application to authenticate the user
     * @param v The view of the button, not used
     */
    public void restartClick( View v ) {
        Intent i = new Intent( ac, MainActivity.class );
        i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( i );
    }

    public void showPasswordClick( View v ) {
        GUIUtils.showPassword( (CheckBox) v, etActivationCode );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();

        switch( responseCode ) {
            case REG_REQ:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    setResult( RESULT_OK );
                    finish();
                } else {
                    String message = response.getMessage();
                    MessageHandler.sendMessage( mHandlerMessages, code, message );
                }
                break;
        }
    }
}
