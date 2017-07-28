package co.yodo.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;

import co.yodo.launcher.R;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class PrefUtils {
    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig(Context c) {
        return c.getSharedPreferences(AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Sets if the device is legacy or not
     * @param c The Android application context
     * @param legacy True or false, (if legacy or not)
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean setLegacy(Context c, Boolean legacy) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean(AppConfig.SPREF_LEGACY, legacy);
        return writer.commit();
    }

    /**
     * Returns if the device is leagacy (doesn't support Google Service)
     * @param c The Android application context
     * @return True if legacy
     *         False if not
     */
    public static Boolean isLegacy(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean(AppConfig.SPREF_LEGACY, false);
    }

    /**
     * It saves the status of login.
     * @param c The Context of the Android system.
     * @param flag The status of the login.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean setLoggedIn(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean(AppConfig.SPREF_LOGIN_STATE, flag);
        return writer.commit();
    }

    /**
     * It gets the status of the login.
     * @param c The Context of the Android system.
     * @return true  It is logged in.
     *         false It is not logged in.
     */
    public static Boolean isLoggedIn(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean(AppConfig.SPREF_LOGIN_STATE, false);
    }

    /**
     * It gets the currency for a specific key.
     * @param c The Context of the Android system.
     * @param key The key of the currency
     * @return String It returns the currency name.
     */
    private static String getCurrency(Context c, String key) {
        SharedPreferences config = getSPrefConfig(c);

        try {
            // Looks for any problem in previous preferences
            final String currency = config.getString(key, null);
            final String[] currencies = c.getResources().getStringArray(R.array.currency_array);
            return (currency == null || !Arrays.asList(currencies).contains(currency)) ? null : currency;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * It saves the currency to the preferences.
     * @param c The Context of the Android system.
     * @param key The key of the currency
     * @param currency The currency name
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    private static Boolean saveCurrency(Context c, String key, String currency) {
        SharedPreferences.Editor writer = getSPrefConfig(c).edit();

        // Supported currencies
        final String[] currencies = c.getResources().getStringArray(R.array.currency_array);
        if (Arrays.asList(currencies).contains(currency)) {
            writer.putString(key, currency);
            return writer.commit();
        }

        return false;
    }

    /**
     * It saves the currency array position to the preferences.
     * @param c The Context of the Android system.
     * @param currency The currency name
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveTenderCurrency(Context c, String currency) {
        return saveCurrency(c, AppConfig.SPREF_CURRENT_CURRENCY, currency);
    }

    /**
     * It gets the currency position.
     * @param c The Context of the Android system.
     * @return int It returns the currency name.
     */
    public static String getTenderCurrency(Context c) {
        return getCurrency(c, AppConfig.SPREF_CURRENT_CURRENCY);
    }

    /**
     * It saves the merchant currency to the preferences.
     * @param c The Context of the Android system.
     * @param currency The currency of the merchant.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveMerchantCurrency(Context c, String currency) {
        return saveCurrency(c, AppConfig.SPREF_MERCHANT_CURRENCY, currency);
    }

    /**
     * It gets the merchant currency.
     * @param c The Context of the Android system.
     * @return int It returns the currency.
     */
    public static String getMerchantCurrency(Context c) {
        return getCurrency(c, AppConfig.SPREF_MERCHANT_CURRENCY);
    }

    /**
     * It gets the state of cash payments
     * @param c The Context of the Android system.
     * @return Boolean If cash payments are allowed
     */
    public static Boolean isCashPaymentAllowed(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean(AppConfig.SPREF_PAYMENT_CASH, false);
    }

    /**
     * Returns if the device is advertising
     * @param c The Android application context
     * @return True if it is advertising
     *         False if it is not
     */
    public static Boolean isAdvertising(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, false );
    }

    /**
     * Returns if the device is getting the location
     * @param c The Android application context
     * @return True if it is getting the location
     *         False if it is not
     */
    public static Boolean isLocating( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_LOCATION_SERVICE, false );
    }

    /**
     * Save the current state of the location service
     * @param c The Android application context
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveLocating( Context c, Boolean flag ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_LOCATION_SERVICE, flag );
        return writer.commit();
    }

    /**
     * It saves the logo url to the preferences.
     * @param c The Context of the Android system.
     * @param s The logo url.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveLogoUrl(Context c, String s) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();
        writer.putString(AppConfig.SPREF_CURRENT_LOGO, s);
        return writer.commit();
    }

    /**
     * It gets the logo url.
     * @param c The Context of the Android system.
     * @return int It returns the beacon name.
     */
    public static String getLogoUrl(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getString(AppConfig.SPREF_CURRENT_LOGO, null);
    }

    /**
     * It gets the language position.
     * @param c The Context of the Android system.
     * @return int It returns the language position.
     */
    static String getLanguage( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_LANGUAGE, AppConfig.DEFAULT_LANGUAGE );
    }

    /**
     * It saves the scanner array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The scanner position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveScanner(Context c, int n) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();
        writer.putInt(AppConfig.SPREF_CURRENT_SCANNER, n);
        return writer.commit();
    }

    /**
     * It gets the scanner position.
     * @param c The Context of the Android system.
     * @return int It returns the scanner position.
     */
    public static int getScanner(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getInt(AppConfig.SPREF_CURRENT_SCANNER, AppConfig.DEFAULT_SCANNER);
    }

    /**
     * It gets the beacon name.
     * @param c The Context of the Android system.
     * @return int It returns the beacon name.
     */
    public static String getBeaconName( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_BEACON, "" );
    }

    /**
     * It saves the ic_discount for the payments to the preferences.
     * @param c The Context of the Android system.
     * @param n The scanner position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveDiscount(Context c, String n) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();

        if (n.isEmpty()) {
            writer.remove(AppConfig.SPREF_DISCOUNT);
        } else {
            writer.putString(AppConfig.SPREF_DISCOUNT, n);
        }

        return writer.commit();
    }

    /**
     * It gets the current tip (%).
     * @param c The Context of the Android system.
     * @return int It returns the tip percentage.
     */
    public static Double getDiscount(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        String discount = config.getString(AppConfig.SPREF_DISCOUNT, AppConfig.DEFAULT_DISCOUNT);
        return Double.parseDouble(discount);
    }

    /**
     * It saves if it is the first login.
     * @param c The Context of the Android system.
     * @param flag If it is the first login or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveFirstLogin( Context c, Boolean flag ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_FIRST_LOGIN, flag );
        return writer.commit();
    }

    /**
     * It gets if it is the first login.
     * @param c The Context of the Android system.
     * @return true  It is logged in.
     *         false It is not logged in.
     */
    public static Boolean isFirstLogin( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_FIRST_LOGIN, true );
    }

    /**
     * It gets the layout mode.
     * @param c The Context of the Android system.
     * @return int It returns the layout mode.
     */
    public static Boolean isPortraitMode( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_PORTRAIT_MODE, false );
    }

    public static int getCurrentBackground( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_BACKGROUND, -0x1 );
    }

    /**
     * If the rocket should print receipts after a yodo transaction
     * @param c The Context of the Android system
     * @return boolean True or false
     */
    public static boolean isPrintingYodo( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_YODO_RECEIPTS, false );
    }

    /**
     * If the rocket should print receipts after a static transaction
     * @param c The Context of the Android system
     * @return boolean True or false
     */
    public static boolean isPrintingStatic( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_STATIC_RECEIPTS, false );
    }

    /**
     * If the rocket should print receipts after a cash transaction
     * @param c The Context of the Android system
     * @return boolean True or false
     */
    public static boolean isPrintingCash( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_CASH_RECEIPTS, false );
    }

    /**
     * Gets the task id for the splash image
     * @param c The application context
     * @param taskId The id of the task for the image
     */
    static void setSplashImageId(Context c, long taskId) {
        SharedPreferences config = getSPrefConfig(c);
        SharedPreferences.Editor writer = config.edit();
        writer.putLong(AppConfig.SPREF_SPLASH_IMAGE, taskId);
        writer.apply();
    }

    /**
     * Gets the task id of the splash image
     * @param c The application context
     * @return The id as long
     */
    static long getSplashImageId(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getLong(AppConfig.SPREF_SPLASH_IMAGE, -1L);
    }
}
