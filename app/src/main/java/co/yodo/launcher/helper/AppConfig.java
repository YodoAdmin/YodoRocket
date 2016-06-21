package co.yodo.launcher.helper;

import co.yodo.restapi.network.YodoRequest;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag: to print the logs in console */
    public static final boolean DEBUG = true;

    /** Sets the log flag for the restapi */
    static {
        co.yodo.restapi.helper.AppConfig.DEBUG = true;
    }

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoRocketSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Hardware token for the account
     * type -- String
     */
    public static final String SPREF_HARDWARE_TOKEN = "SPHardwareToken";

    /* Device state: legacy or not.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- The device is legacy (doesn't have google services)
	 * false -- The device supports google services
	 */
    public static final String SPREF_LEGACY = "SPLegacy";

    /* Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Application registered and authorized
	 * false -- Application not registered or authorized
	 */
    public static final String SPREF_LOGIN_STATE = "SPLoginState";

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    public static final String SPREF_FIRST_LOGIN = "SPFirstLogin";

    /* The current logo url.
    * type -- String
    */
    public static final String SPREF_CURRENT_LOGO = "SPCurrentLogo";

    /* The current language.
	 * type -- Integer
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* The current beacon.
    * type -- String
    */
    public static final String SPREF_CURRENT_BEACON = "SPCurrentBeacon";

    /* The current discount to the purchase.
    * type -- String
    */
    public static final String SPREF_DISCOUNT = "SPDiscount";

    /* The current tip to the purchase.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_TIP = "SPCurrentTip";

    /* The current password, in case of remember option selected.
    * type -- String
    */
    public static final String SPREF_CURRENT_PASSWORD = "SPCurrentPassword";

    /* The current currency.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_CURRENCY = "SPCurrentCurrency";

    /* The current currency.
    * type -- Integer
    */
    public static final String SPREF_MERCHANT_CURRENCY = "SPMerchantCurrency";

    /* The current scanner position.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_SCANNER = "SPCurrentScanner";

    /* Advertising service status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Service is running
	 * false -- Service not running
	 */
    public static final String SPREF_ADVERTISING_SERVICE = "SPAdvertisingService";

    /* The current background of the Rocket.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_BACKGROUND = "SPBackgroundColor";

    /**
     * Default values
     * {{ ======================================================================
     */

    /*
	 * Default value position for the language
	 *
	 * Default: en (English)
	 */
    public static final String DEFAULT_LANGUAGE = "en";

    /*
	 * Default value position for the currency
	 *
	 * Default: position 1 (Canada Dollar)
	 */
    public static final Integer DEFAULT_CURRENCY = 1;

    /*
	 * Default value for the discount (%)
	 */
    public static final String DEFAULT_DISCOUNT = "0";

    /*
	 * Default value for the tip (%)
	 */
    public static final String DEFAULT_TIP = "0";

    /*
	 * Default value position for the scanner
	 *
	 * Default: position 0 (BarcodeScanner)
	 */
    public static final Integer DEFAULT_SCANNER = 0;

    /* Currency of the URL where we are getting the rates */
    public static final String URL_CURRENCY  = "EUR";

    /* Logo URL */
    public static final String LOGO_PATH = YodoRequest.getRoot() + "/yodo-merch/uploads/img/logo/";

    /* SKS Sizes */
    public static final int SKS_SIZE = 256;
    public static final int ALT_SIZE = 257;
}
