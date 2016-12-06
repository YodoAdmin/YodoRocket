package co.yodo.launcher.ui.option;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.ListAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.adapter.CurrencyAdapter;
import co.yodo.launcher.ui.adapter.data.Currency;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.option.contract.IOption;

/**
 * Created by hei on 22/06/16.
 * Implements the Currency Option of the Launcher
 */
public class CurrencyOption extends IOption {
    /** Elements for the AlertDialog */
    private final String mTitle;
    private final ListAdapter mAdapter;
    private final int current;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CurrencyOption( Activity activity ) {
        super( activity );
        // AlertDialog
        this.mTitle = this.mActivity.getString( R.string.set_currency );
        final String[] mCurrencies = this.mActivity.getResources().getStringArray( R.array.currency_array );
        final String[] mIcons = this.mActivity.getResources().getStringArray( R.array.currency_icon_array );

        // Set adapter
        Currency[] currencyList = new Currency[ mCurrencies.length];
        for( int i = 0; i < mCurrencies.length; i++ ) {
            currencyList[ i ] = new Currency(
                    mCurrencies[ i ],
                    GUIUtils.getDrawableByName( mActivity, mIcons[ i ] )
            );
        }

        Collections.sort( Arrays.asList( currencyList ), new Comparator<Currency>() {
            @Override
            public int compare( Currency lhs, Currency rhs ) {
                return lhs.getName().compareTo( rhs.getName() );
            }
        });

        mAdapter = new CurrencyAdapter( mActivity, currencyList );

        current = Collections.binarySearch(
                Arrays.asList( currencyList ),
                new Currency( PrefUtils.getTenderCurrency( mActivity ), null ),
                new Comparator<Currency>() {
                    @Override
                    public int compare( Currency lhs, Currency rhs ) {
                        return lhs.getName().compareTo( rhs.getName() );
                    }
                }
        );
    }

    @Override
    public void execute() {
        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                Currency currency = ( Currency ) mAdapter.getItem( item );
                PrefUtils.saveTenderCurrency( mActivity, currency.getName() );
                ( (LauncherActivity) mActivity ).currency( currency.getImg() );
                dialog.dismiss();
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                this.mAdapter,
                current,
                onClick
        );
    }
}
