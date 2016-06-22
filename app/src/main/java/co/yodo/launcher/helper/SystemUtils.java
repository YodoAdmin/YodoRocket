package co.yodo.launcher.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import co.yodo.launcher.ui.notification.AlertDialogHelper;

/**
 * Created by hei on 20/06/16.
 * Any system requirement like permissions,
 * google services or logger
 */
public class SystemUtils {
    /**
     * Verify if a service is running
     * @param c The Context of the Android system.
     * @param serviceName The name of the service.
     * @return Boolean true if is running otherwise false
     */
    public static boolean isMyServiceRunning( Context c, String serviceName) {
        ActivityManager manager = (ActivityManager) c.getSystemService( Context.ACTIVITY_SERVICE );
        for( ActivityManager.RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) )  {
            if( serviceName.equals( service.service.getClassName() ) )
                return true;
        }
        return false;
    }

    /**
     * Method to verify google play services on the device
     * @param activity The activity that
     * @param code The code for the activity result
     * */
    public static boolean isGooglePlayServicesAvailable( Activity activity, int code ) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable( activity );
        if( resultCode != ConnectionResult.SUCCESS ) {
            if( googleAPI.isUserResolvableError( resultCode ) ) {
                googleAPI.getErrorDialog( activity, resultCode, code ).show();
            } else {
                PrefUtils.setLegacy( activity, true );
            }
            return false;
        }
        return true;
    }

    /**
     * Verify if the device has GPS
     * @param c The Context of the Android system.
     * @return Boolean true if it has GPS
     */
    public static boolean hasLocationService( Context c ) {
        LocationManager locManager = (LocationManager) c.getSystemService( Context.LOCATION_SERVICE );
        return locManager.getProvider( LocationManager.GPS_PROVIDER ) != null;
    }

    /**
     * Verify if the location services are enabled (any provider)
     * @param c The Context of the Android system.
     * @return Boolean true if is running otherwise false
     */
    public static boolean isLocationEnabled( Context c ) {
        LocationManager lm = (LocationManager) c.getSystemService( Context.LOCATION_SERVICE );
        String provider    = lm.getBestProvider( new Criteria(), true );
        return ( ( !provider.isEmpty() ) && !LocationManager.PASSIVE_PROVIDER.equals( provider ) );
    }

    /**
     * Requests a permission for the use of a phone's characteristic (e.g. Camera, Phone info, etc)
     * @param ac The application context
     * @param message A message to request the permission
     * @param permission The permission
     * @param requestCode The request code for the result
     * @return If the permission was already allowed or not
     */
    public static boolean requestPermission( final Activity ac, final int message, final String permission, final int requestCode ) {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission( ac, permission );
        if( permissionCheck != PackageManager.PERMISSION_GRANTED ) {
            if( ActivityCompat.shouldShowRequestPermissionRationale( ac, permission ) ) {
                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        ActivityCompat.requestPermissions(
                                ac,
                                new String[]{permission},
                                requestCode
                        );
                    }
                };

                AlertDialogHelper.showAlertDialog(
                        ac,
                        message,
                        onClick
                );
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions( ac, new String[]{permission}, requestCode );
            }
            return false;
        }
        return true;
    }

    /**
     * Logger for Android
     * @param TAG The String of the TAG for the log
     * @param text The text to print on the log
     */
    public static void Logger( String TAG, String text ) {
        if( AppConfig.DEBUG ) {
            if( text == null )
                Log.e( TAG, "Null Text" );
            else
                Log.e( TAG, text );
        }
    }
}
