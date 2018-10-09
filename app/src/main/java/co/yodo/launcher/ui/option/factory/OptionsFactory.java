package co.yodo.launcher.ui.option.factory;

import java.util.HashMap;

import co.yodo.launcher.business.manager.PromotionManager;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.option.AboutOption;
import co.yodo.launcher.ui.option.BalanceOption;
import co.yodo.launcher.ui.option.CurrencyOption;
import co.yodo.launcher.ui.option.DiscountOption;
import co.yodo.launcher.ui.option.contract.IOption;

/**
 * Created by hei on 23/04/17.
 * Builds the different options
 */
public final class OptionsFactory {
    /** Mandatory activity and messenger */
    private final BaseActivity activity;

    /** Elements for the request */
    private final PromotionManager promotionManager;

    /** Contain the created options */
    private final HashMap<Option, IOption> options;

    /** Options enumerate */
    public enum Option {
        ABOUT,
        BALANCE,
        CURRENCY,
        DISCOUNT;
    }

    public OptionsFactory(BaseActivity activity, PromotionManager promotionManager) {
        this.activity = activity;
        this.promotionManager = promotionManager;
        this.options = new HashMap<>();
    }

    /**
     * Gets an option
     * @param option The option key
     * @return The option to be executed
     */
    public IOption getOption(Option option) {
        // Get the instance
        if (options.containsKey(option)) {
            return options.get(option);
        }

        // Create depending in the option
        IOption instance;
        switch (option) {
            case ABOUT:
                instance = new AboutOption(activity);
                break;

            case BALANCE:
                instance = new BalanceOption(activity, promotionManager);
                break;

            case CURRENCY:
                instance = new CurrencyOption(activity);
                break;

            case DISCOUNT:
                instance = new DiscountOption(activity, promotionManager);
                break;

            default:
                return null;
        }

        options.put(option, instance);
        return instance;
    }
}
