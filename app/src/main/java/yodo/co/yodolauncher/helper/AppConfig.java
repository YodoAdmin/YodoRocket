package yodo.co.yodolauncher.helper;

/**
 * Created by luis on 15/12/14.
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

    /* The current language.
	 * type -- Integer
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* The current currency.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_CURRENCY = "SPCurrentCurrency";

    /* The current beacon.
    * type -- String
    */
    public static final String SPREF_CURRENT_BEACON = "SPCurrentBeacon";

    /* Advertising service status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Service is running
	 * false -- Service not running
	 */
    public static final String SPREF_ADVERTISING_SERVICE_RUNNING = "SPAdvertisingServiceRunning";

    /**
     * }} ======================================================================
     */

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
	 * Default: position 0 (MexicanPeso)
	 */
    public static final Integer DEFAULT_CURRENCY = 0;

    /* Bluetooth Yodo POS name */
    public static final String YODO_POS = "Yodo-Merch-";
}
