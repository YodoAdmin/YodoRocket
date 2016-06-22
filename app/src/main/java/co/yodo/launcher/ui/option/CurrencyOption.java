package co.yodo.launcher.ui.option;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.ListAdapter;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.adapter.data.Currency;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.adapter.CurrencyAdapter;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.option.contract.IOption;

/**
 * Created by hei on 22/06/16.
 * Implements the Currency Option of the Launcher
 */
public class CurrencyOption extends IOption {
    /** Elements for the AlertDialog */
    private final String mTitle;
    private final String[] mCurrencies;
    private final String[] mIcons;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CurrencyOption( Activity activity ) {
        super( activity );
        // AlertDialog
        this.mTitle = this.mActivity.getString( R.string.set_currency );
        this.mCurrencies = this.mActivity.getResources().getStringArray( R.array.currency_array );
        this.mIcons = this.mActivity.getResources().getStringArray( R.array.currency_icon_array );
    }

    @Override
    public void execute() {
        Currency[] currencyList = new Currency[mCurrencies.length];
        for( int i = 0; i < mCurrencies.length; i++ )
            currencyList[i] = new Currency( mCurrencies[i], GUIUtils.getDrawableByName( mActivity, mIcons[i] ) );

        final ListAdapter adapter = new CurrencyAdapter( mActivity, currencyList );
        final int current = PrefUtils.getCurrency( mActivity );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                PrefUtils.saveCurrency( mActivity, item );

                Drawable icon = GUIUtils.getDrawableByName( mActivity, mIcons[ item ] );
                ( (LauncherActivity) mActivity ).currency( icon );

                dialog.dismiss();
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                adapter,
                current,
                onClick
        );
    }
}
