package yodo.co.yodolauncher.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by luis on 15/12/14.
 */
public class AppUtils {
    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig(Context c) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    /**
     * It saves the status of login.
     * @param c The Context of the Android system.
     * @param flag The status of the login.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveLoginStatus(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_LOGIN_STATE, flag );
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
        return config.getBoolean( AppConfig.SPREF_LOGIN_STATE, false );
    }

    /**
     * It saves the language array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The language position on the array.
     * @return true  If it was saved.
     *         false It it was not saved.
     */
    public static Boolean saveLanguage(Context c, int n) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putInt( AppConfig.SPREF_CURRENT_LANGUAGE, n );
        return writer.commit();
    }

    /**
     * It gets the language position.
     * @param c The Context of the Android system.
     * @return int It returns the language position.
     */
    public static int getLanguage(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_LANGUAGE, AppConfig.DEFAULT_LANGUAGE );
    }

    /**
     * It saves the currency array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The currency position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveCurrency(Context c, int n) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putInt( AppConfig.SPREF_CURRENT_CURRENCY, n );
        return writer.commit();
    }

    /**
     * It gets the currency position.
     * @param c The Context of the Android system.
     * @return int It returns the currency position.
     */
    public static int getCurrency(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_CURRENCY, AppConfig.DEFAULT_CURRENCY );
    }

    /**
     * It saves the beacon name to the preferences.
     * @param c The Context of the Android system.
     * @param s The beacon name.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveBeaconName(Context c, String s) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_CURRENT_BEACON, s );
        return writer.commit();
    }

    /**
     * It gets the beacon name.
     * @param c The Context of the Android system.
     * @return int It returns the beacon name.
     */
    public static String getBeaconName(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_BEACON, "" );
    }

    /**
     * It saves if it is the first login.
     * @param c The Context of the Android system.
     * @param flag If it is the first login or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveFirstLogin(Context c, Boolean flag) {
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
    public static Boolean isFirstLogin(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean( AppConfig.SPREF_FIRST_LOGIN, true );
    }

    /**
     * It saves the state of the advertising service.
     * @param c The Context of the Android system.
     * @param flag If it is running or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveAdvertisingServiceRunning(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_ADVERTISING_SERVICE_RUNNING, flag );
        return writer.commit();
    }

    /**
     * It gets the status of the advertising service.
     * @param c The Context of the Android system.
     * @return true  Advertising service is on.
     *         false Advertising service is off.
     */
    public static Boolean isAdvertisingServiceRunning(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE_RUNNING, false );
    }

    /**
     * Gets the mobile hardware identifier
     * @param c The Context of the Android system.
     */
    public static String getHardwareToken(Context c) {
        String HARDWARE_TOKEN = null;

        TelephonyManager telephonyManager  = (TelephonyManager) c.getSystemService( Context.TELEPHONY_SERVICE );
        //BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        WifiManager wifiManager            = (WifiManager) c.getSystemService( Context.WIFI_SERVICE );

        if( telephonyManager != null ) {
            HARDWARE_TOKEN = telephonyManager.getDeviceId();
        }

        /*if(HARDWARE_TOKEN == null && mBluetoothAdapter != null) {
            if(mBluetoothAdapter.isEnabled()) {
                String tempMAC = mBluetoothAdapter.getAddress();
                HARDWARE_TOKEN = tempMAC.replaceAll(":", "");
            }
        }*/

		if( HARDWARE_TOKEN == null && wifiManager != null ) {
			if( wifiManager.isWifiEnabled() ) {
				WifiInfo wifiInf = wifiManager.getConnectionInfo();
				String tempMAC = wifiInf.getMacAddress();
				HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
			}
		}

        return HARDWARE_TOKEN;
    }

    public static Drawable getDrawableByName(Context c, String name) {
        Resources resources = c.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable", c.getPackageName());
        Drawable image = resources.getDrawable(resourceId);
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        return image;
    }

    /**
     * Logger for Android
     * @param TAG The String of the TAG for the log
     * @param text The text to print on the log
     */
    public static void Logger(String TAG, String text) {
        if( AppConfig.DEBUG ) {
            if( text == null )
                Log.e( TAG, "Null Text" );
            else
                Log.e( TAG, text );
        }
    }
}
