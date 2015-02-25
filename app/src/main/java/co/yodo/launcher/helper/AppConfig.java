package co.yodo.launcher.helper;

import co.yodo.launcher.service.RESTService;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag */
    public static final boolean DEBUG = true;

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoLauncherSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

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

    /* The timestamp of the latest currency update.
	 * type -- Integer
	 */
    public static final String SPREF_TIMESTAMP_CURRENCY = "SPTimestampCurrency";

    /* The current beacon.
    * type -- String
    */
    public static final String SPREF_CURRENT_BEACON = "SPCurrentBeacon";

    /* The current password, in case of remember option selected.
    * type -- String
    */
    public static final String SPREF_CURRENT_PASSWORD = "SPCurrentPassword";

    /* The current currency.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_CURRENCY = "SPCurrentCurrency";

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
    public static final String SPREF_ADVERTISING_SERVICE_RUNNING = "SPAdvertisingServiceRunning";

    /**
     * Default values
     * {{ ======================================================================
     */

    /*
	 * Default value position for the language
	 *
	 * Default: position 0 (English)
	 */
    public static final Integer DEFAULT_LANGUAGE = 0;

    /*
	 * Default value position for the currency
	 *
	 * Default: position 1 (Canada Dollar)
	 */
    public static final Integer DEFAULT_CURRENCY = 1;

    /*
	 * Default value position for the scanner
	 *
	 * Default: position 0 (BarcodeScanner)
	 */
    public static final Integer DEFAULT_SCANNER = 0;

    /* Request Code (Activity Result) */
    public static final int REGISTRATION_REQUEST = 1;
    public static final int LAUNCHER_REQUEST     = 2;

    /* Bluetooth Yodo POS name */
    public static final String YODO_POS = "Yodo-Merch-";

    /* Logo URL */
    public static final String LOGO_PATH = RESTService.getRoot() + "/yodo-merch/uploads/img/logo/";

    /* SKS Sizes */
    public static final int SKS_SIZE = 256;
    public static final int ALT_SIZE = 257;
}
