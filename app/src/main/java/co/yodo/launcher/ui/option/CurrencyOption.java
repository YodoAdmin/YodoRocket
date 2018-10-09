package co.yodo.launcher.ui.option;

import android.content.DialogInterface;
import android.widget.ListAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.utils.GuiUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.ui.adapter.CurrencyAdapter;
import co.yodo.launcher.ui.adapter.data.Currency;
import co.yodo.launcher.ui.option.contract.IOption;

/**
 * Created by hei on 22/06/16.
 * Implements the Currency Option of the Launcher
 */
public class CurrencyOption extends IOption {
    /** Elements for the AlertDialog */
    private final String title;
    private final ListAdapter adapter;
    private final int current;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public CurrencyOption(BaseActivity activity) {
        super(activity);

        title = activity.getString(R.string.text_option_currency);
        final String[] currencies = activity.getResources().getStringArray(R.array.currency_array);
        final String[] icons = activity.getResources().getStringArray(R.array.currency_icon_array);

        // Set adapter
        Currency[] currenciesArray = new Currency[currencies.length];
        for (int i = 0; i < currencies.length; ++i) {
            currenciesArray[i] = new Currency(
                    currencies[i],
                    GuiUtils.getDrawableByName(activity, icons[i])
            );
        }

        Collections.sort(Arrays.asList(currenciesArray), new Comparator<Currency>() {
            @Override
            public int compare( Currency lhs, Currency rhs ) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        // Get selected currency
        adapter = new CurrencyAdapter(activity, currenciesArray);
        current = Collections.binarySearch(
                Arrays.asList(currenciesArray),
                new Currency(PrefUtils.getTenderCurrency(activity), null),
                new Comparator<Currency>() {
                    @Override
                    public int compare( Currency lhs, Currency rhs ) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                }
        );
    }

    @Override
    public void execute() {
        DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
                Currency currency = (Currency) adapter.getItem(item);
                PrefUtils.saveTenderCurrency(activity, currency.getName());
                dialog.dismiss();
                activity.updateUI();
            }
        };

        AlertDialogHelper.create(
                activity,
                title,
                adapter,
                current,
                okClick
        );
    }
}
