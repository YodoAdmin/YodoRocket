package yodo.co.yodolauncher.main;

import android.app.Activity;
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

import yodo.co.yodolauncher.R;
import yodo.co.yodolauncher.adapter.CurrencyAdapter;
import yodo.co.yodolauncher.component.ToastMaster;
import yodo.co.yodolauncher.data.Currency;
import yodo.co.yodolauncher.helper.AlertDialogHelper;
import yodo.co.yodolauncher.helper.AppUtils;
import yodo.co.yodolauncher.data.ServerResponse;
import yodo.co.yodolauncher.net.YodoRequest;

public class LauncherActivity extends Activity implements YodoRequest.RESTListener {
    /** DEBUG */
    private static final String TAG = LauncherActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Gui controllers */
    private TextView totalText;
    private TextView paidText;
    private TextView cashBackText;
    private TextView balanceText;
    private TextView actualText;
    private SlidingPaneLayout mSlidingLayout;
    private CheckBox advertisingBox;
    private Spinner available_scanners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        setupGUI();
    }

    private void setupGUI() {
        ac = LauncherActivity.this;

        mSlidingLayout = (SlidingPaneLayout) findViewById( R.id.sliding_panel_layout );
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
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick(View v) {
        mSlidingLayout.closePane();
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

    }
}
