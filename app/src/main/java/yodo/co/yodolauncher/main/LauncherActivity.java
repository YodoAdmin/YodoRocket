package yodo.co.yodolauncher.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import yodo.co.yodolauncher.R;
import yodo.co.yodolauncher.adapter.CurrencyAdapter;
import yodo.co.yodolauncher.component.ToastMaster;
import yodo.co.yodolauncher.component.YodoHandler;
import yodo.co.yodolauncher.data.Currency;
import yodo.co.yodolauncher.helper.AlertDialogHelper;
import yodo.co.yodolauncher.helper.AppConfig;
import yodo.co.yodolauncher.helper.AppUtils;
import yodo.co.yodolauncher.data.ServerResponse;
import yodo.co.yodolauncher.net.YodoRequest;

public class LauncherActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Bluetooth Admin */
    private BluetoothAdapter mBluetoothAdapter;

    /** Hardware Token */
    private String hardwareToken;

    /** Gui controllers */
    private SlidingPaneLayout mSlidingLayout;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if( AppUtils.isAdvertisingServiceRunning(ac) )
            setupAdvertising( false );
    }

    /**
     * Initialized all the GUI main components
     */
    private void setupGUI() {
        ac = LauncherActivity.this;

        // Globals
        mSlidingLayout       = (SlidingPaneLayout) findViewById( R.id.sliding_panel_layout );

        // Only used at creation
        CheckBox mAdvertisingCheckBox = (CheckBox) findViewById(R.id.advertisingCheckBox);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handlerMessages   = new YodoHandler( LauncherActivity.this );
        YodoRequest.getInstance().setListener( this );

        if( AppUtils.isFirstLogin( ac ) ) {
            mSlidingLayout.openPane();

            new ShowcaseView.Builder( this )
                    .setTarget( new ViewTarget( R.id.optionsView, this ) )
                    .setContentTitle( R.string.tutorial_title )
                    .setContentText( R.string.tutorial_message )
                    .build();

            AppUtils.saveFirstLogin(ac, false);
        }

        if( mBluetoothAdapter == null )
            mAdvertisingCheckBox.setEnabled(false);
        else {
            final boolean isAdvertisingRunning =  AppUtils.isAdvertisingServiceRunning( ac );
            mAdvertisingCheckBox.setChecked( isAdvertisingRunning );

            if( isAdvertisingRunning )
                setupAdvertising( false );
        }
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );
    }

    /**
     * Set-up bluetooth for advertising
     * @param force To force the require for being discoverable
     */
    private void setupAdvertising(boolean force) {
        if( !mBluetoothAdapter.isEnabled() || force ) {
            mBluetoothAdapter.enable();

            Intent discoverableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0 );
            startActivity( discoverableIntent );
        }

        mBluetoothAdapter.setName( AppConfig.YODO_POS + AppUtils.getBeaconName( ac ) );
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
                ToastMaster.makeText( ac, languages[item], Toast.LENGTH_SHORT ).show();
                AppUtils.saveLanguage( ac, item );

                Intent intent = new Intent( LauncherActivity.this, MainActivity.class );
                startActivity( intent );
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

        String[] currency = getResources().getStringArray( R.array.currency_array );
        String[] icons    = getResources().getStringArray( R.array.currency_icon_array );

        Currency[] currencyList = new Currency[currency.length];
        for( int i = 0; i < currency.length; i++ )
            currencyList[i] = new Currency( currency[i], AppUtils.getDrawableByName( ac, icons[i] ) );

        final String title        = ((Button) v).getText().toString();
        final ListAdapter adapter = new CurrencyAdapter( ac, currencyList );
        final int current         = AppUtils.getCurrency( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                AppUtils.saveCurrency( ac, item );
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
     * Changes the current username for bluetooth
     * @param v View, not used
     */
    public void setUsernameClick(View v) {
        mSlidingLayout.closePane();

        final String title  = ((Button) v).getText().toString();
        final String beacon = AppUtils.getBeaconName( ac );

        final EditText inputBox = new EditText( ac );
        inputBox.setText( beacon );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                AppUtils.saveBeaconName( ac, inputBox.getText().toString() );
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                inputBox,
                onClick
        );
    }

    /**
     * Enables or disables advertising
     * @param v View, checkbox for enable or disable
     */
    public void setAdvertisingClick(View v) {
        mSlidingLayout.closePane();

        final boolean isAdvertisingRunning = ((CheckBox) v).isChecked();
        AppUtils.saveAdvertisingServiceRunning( ac , isAdvertisingRunning );

        if( isAdvertisingRunning )
            setupAdvertising( true );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick(View v) {
        mSlidingLayout.closePane();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new EditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                YodoRequest.getInstance().requestHistory( LauncherActivity.this, hardwareToken, pip );
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                inputBox, true, true,
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
        final String message = getString( R.string.imei)       + " " +
                               AppUtils.getHardwareToken( ac ) + "\n" +
                               getString( R.string.version );

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                message
        );
    }

    /**
     * Logout the user
     * @param v View, not used
     */
    public void logoutClick(View v) {
        finish();
        AppUtils.saveLoginStatus( ac, false);
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        //AppUtils.Logger(TAG, response.toString());

        switch( type ) {
            case ERROR:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case QUERY_BAL_REQUEST:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {

                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {

                }

                break;
        }
    }
}
