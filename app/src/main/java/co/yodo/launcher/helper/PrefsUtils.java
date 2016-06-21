package co.yodo.launcher.helper;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;

import co.yodo.launcher.component.AES;
import co.yodo.launcher.component.YodoHandler;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class PrefsUtils {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = PrefsUtils.class.getSimpleName();

    /**
     * A simple check to see if a string is a valid number before inserting
     * into the shared preferences.
     *
     * @param s The number to be checked.
     * @return true  It is a number.
     *         false It is not a number.
     */
    public static Boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
        }
        catch( NumberFormatException e ) {
            return false;
        }
        return true;
    }

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig( Context c ) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    /**
     * Generates the mobile hardware identifier either
     * from the Phone (IMEI) or the Bluetooth (MAC)
     * @param c The Context of the Android system.
     */
    public static String generateHardwareToken( Context c ) {
        String HARDWARE_TOKEN = null;

        TelephonyManager telephonyManager  = (TelephonyManager) c.getSystemService( Context.TELEPHONY_SERVICE );
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if( telephonyManager != null ) {
            String tempMAC = telephonyManager.getDeviceId();
            if( tempMAC != null )
                HARDWARE_TOKEN = tempMAC.replace( "/", "" );
        }

        if( HARDWARE_TOKEN == null && mBluetoothAdapter != null ) {
            if( mBluetoothAdapter.isEnabled() ) {
                String tempMAC = mBluetoothAdapter.getAddress();
                HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
            }
        }

        return HARDWARE_TOKEN;
    }

    public static Boolean saveHardwareToken( Context c, String hardwareToken ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_HARDWARE_TOKEN, hardwareToken );
        return writer.commit();
    }

    public static String getHardwareToken( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        String token = config.getString( AppConfig.SPREF_HARDWARE_TOKEN, "" );
        return ( token.equals( "" ) ) ? null : token;
    }

    /**
     * Sets if the device is legacy or not
     * @param c The Android application context
     * @param legacy True or false, (if legacy or not)
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean setLegacy( Context c, Boolean legacy ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_LEGACY, legacy );
        return writer.commit();
    }

    /**
     * Returns if the device is leagacy (doesn't support Google Service)
     * @param c The Android application context
     * @return True if legacy
     *         False if not
     */
    public static Boolean isLegacy( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_LEGACY, false );
    }

    /**
     * Returns if the device is leagacy (doesn't support Google Service)
     * @param c The Android application context
     * @return True if legacy
     *         False if not
     */
    public static Boolean isAdvertising( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, false );
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
    public static Boolean isLoggedIn( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_LOGIN_STATE, false );
    }

    /**
     * It saves the logo url to the preferences.
     * @param c The Context of the Android system.
     * @param s The logo url.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveLogoUrl( Context c, String s ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_CURRENT_LOGO, s );
        return writer.commit();
    }

    /**
     * It gets the logo url.
     * @param c The Context of the Android system.
     * @return int It returns the beacon name.
     */
    public static String getLogoUrl( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_LOGO, "" );
    }

    /**
     * It gets the language position.
     * @param c The Context of the Android system.
     * @return int It returns the language position.
     */
    public static String getLanguage( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_LANGUAGE, AppConfig.DEFAULT_LANGUAGE );
    }

    /**
     * It saves the currency array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The currency position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveCurrency( Context c, int n ) {
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
    public static int getCurrency( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        int position = config.getInt( AppConfig.SPREF_CURRENT_CURRENCY, AppConfig.DEFAULT_CURRENCY );
        if( position < 0 ) {
            position = AppConfig.DEFAULT_CURRENCY;
            saveCurrency( c, position );
        }
        return position;
    }

    /**
     * It saves the merchant currency to the preferences.
     * @param c The Context of the Android system.
     * @param n The currency of the merchant.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveMerchantCurrency( Context c, String n ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_MERCHANT_CURRENCY, n );
        return writer.commit();
    }

    /**
     * It gets the merchant currency.
     * @param c The Context of the Android system.
     * @return int It returns the currency.
     */
    public static String getMerchantCurrency( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_MERCHANT_CURRENCY, null );
    }

    /**
     * It saves the scanner array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The scanner position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveScanner( Context c, int n ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putInt( AppConfig.SPREF_CURRENT_SCANNER, n );
        return writer.commit();
    }

    /**
     * It gets the scanner position.
     * @param c The Context of the Android system.
     * @return int It returns the scanner position.
     */
    public static int getScanner( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_SCANNER, AppConfig.DEFAULT_SCANNER );
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
     * It gets the current tip (%).
     * @param c The Context of the Android system.
     * @return int It returns the tip percentage.
     */
    public static String getCurrentTip( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_TIP, AppConfig.DEFAULT_TIP );
    }

    /**
     * It saves the discount for the payments to the preferences.
     * @param c The Context of the Android system.
     * @param n The scanner position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveDiscount( Context c, String n ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putString( AppConfig.SPREF_DISCOUNT, n );
        return writer.commit();
    }

    /**
     * It gets the current tip (%).
     * @param c The Context of the Android system.
     * @return int It returns the tip percentage.
     */
    public static String getDiscount( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_DISCOUNT, AppConfig.DEFAULT_DISCOUNT );
    }

    /**
     * It saves the password to the preferences.
     * @param c The Context of the Android system.
     * @param s The password.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean savePassword( Context c, String s ) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();

        if( s != null ) {
            try {
                String encryptPip = AES.encrypt( s );
                writer.putString( AppConfig.SPREF_CURRENT_PASSWORD, encryptPip );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            writer.remove( AppConfig.SPREF_CURRENT_PASSWORD );
        }

        return writer.commit();
    }

    /**
     * It gets the password if saved.
     * @param c The Context of the Android system.
     * @return int It returns the password or nul.
     */
    public static String getPassword( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        String password = config.getString( AppConfig.SPREF_CURRENT_PASSWORD, null );

        if( password != null ) {
            try {
                password = AES.decrypt( password );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return password;
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
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_FIRST_LOGIN, true );
    }

    public static int getCurrentBackground( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_BACKGROUND, -0x1 );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( int messageType, YodoHandler handlerMessages, String title, String message ) {
        Message msg = new Message();
        msg.what = messageType;

        Bundle bundle = new Bundle();
        bundle.putString( YodoHandler.CODE, title );
        bundle.putString( YodoHandler.MESSAGE, message );
        msg.setData( bundle );

        handlerMessages.sendMessage( msg );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( YodoHandler handlerMessages, String title, String message ) {
        sendMessage( YodoHandler.SERVER_ERROR, handlerMessages, title, message );
    }


}
