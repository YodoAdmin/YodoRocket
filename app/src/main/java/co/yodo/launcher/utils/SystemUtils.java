package co.yodo.launcher.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;
import timber.log.Timber;

/**
 * Created by hei on 20/06/16.
 * Any system requirement like permissions,
 * google services or logger
 */
public class SystemUtils {
    /** Folder to store the resources */
    private static final String FOLDER = "Yodo";
    public static final String RESOURCES_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER;

    private SystemUtils() {}

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
            if( resultCode != ConnectionResult.SERVICE_INVALID && googleAPI.isUserResolvableError( resultCode ) ) {
                googleAPI.getErrorDialog( activity, resultCode, code ).show();
            } else {
                Timber.w("This device does not support Google Services (" + resultCode + ")");
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
    public static boolean hasLocationService(Context c) {
        try {
            LocationManager locManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            return locManager.getProvider(LocationManager.GPS_PROVIDER) != null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify if the location services are enabled (any provider)
     * @param c The Context of the Android system.
     * @return Boolean true if is running otherwise false
     */
    public static boolean isLocationEnabled(Context c) {
        LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(new Criteria(), true);
        return ((!provider.isEmpty()) && !LocationManager.PASSIVE_PROVIDER.equals(provider));
    }

    /**
     * Requests a permission for the use of a phone's characteristic (e.g. Camera, Phone info, etc)
     * @param ac The application context
     * @param message A message to request the permission
     * @param permission The permission
     * @param requestCode The request code for the result
     * @return If the permission was already allowed or not
     */
    public static boolean requestPermission( final Activity ac, final int message, final String permission, final Integer requestCode ) {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission( ac, permission );
        if( permissionCheck != PackageManager.PERMISSION_GRANTED && requestCode == null ) {
            return false;
        }

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

                AlertDialogHelper.create(
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
     * Plays a sound of error
     * @param c The Context of the Android system.
     * @param type The kind of sound 0 - error and 1 - successful
     */
    public static void startSound(Context c, int type) {
        MediaPlayer mp = null;

        switch( type ) {
            case AppConfig.ERROR:
                mp = MediaPlayer.create( c, R.raw.error );
                break;

            case AppConfig.SUCCESSFUL:
                mp = MediaPlayer.create( c, R.raw.successful );
                break;
        }

        if( mp != null ) {
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mp.start();
        }
    }

    /**
     * Copy assets to the storage
     * @param context The application context
     * @return The file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static File getCacheSplash(Context context) {
        File outDir = new File(SystemUtils.RESOURCES_PATH);
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        File outFile = new File(outDir, "splash.png");
        if (!outFile.exists()) {
            try {
                InputStream in = context.getAssets().open("splash.png");
                FileOutputStream out = new FileOutputStream(outFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }

                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outFile;
    }

    /**
     * Saves the merchant logo into the storage
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveMerchantLogo(Context context) {
        final String url = PrefUtils.getLogoUrl(context);
        if (url != null) {
            Picasso.with(context)
                    .load(url)
                    .into(getTarget());
        }
    }

    /**
     * Target to save the file
     * @return The target object
     */
    private static Target getTarget(){
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void run() {
                        File outDir = new File(RESOURCES_PATH);
                        if (!outDir.exists()) {
                            outDir.mkdir();
                        }

                        File outFile = new File(outDir, "logo.png");
                        if (!outFile.exists()) {
                            try {
                                outFile.createNewFile();
                                FileOutputStream ostream = new FileOutputStream(outFile);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed() {
            }
        };
    }
}
