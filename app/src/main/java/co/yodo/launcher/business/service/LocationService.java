package co.yodo.launcher.business.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.utils.SystemUtils;

/**
 * Service to obtain the location of the device, this service in particular
 * uses the Android Location package, the implementation of the Android
 * Location API (it does not use Google Play).
 *
 * @author Luis Talavera
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /** Service tag */
    private static final String TAG = LocationService.class.getSimpleName();

    /** Time difference threshold */
    private static final int TIME_DIFFERENCE_THRESHOLD = 60 * 1000; // 1 minute(s)

    /** Location updates intervals in sec */
    private static final int UPDATE_INTERVAL = 30 * 1000; // 30 second(s)
    private static final int FASTEST_INTERVAL = 20 * 1000; // 20 second(s)
    private static final float DISPLACEMENT = 10.0F;     // 10 meter(s)

    /** Accuracy requirements */
    private static final int BAD_ACCURACY = 100; // 100 meters

    /** Google API object */
    private GoogleApiClient googleApiClient;

    /** Location request object */
    private LocationRequest locationRequest;

    /** Last location saved */
    private Location lastRegisteredLocation;

    /** Service handler for background process */
    private ServiceHandler handler;

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // start the service using the background handler
        Looper looper = thread.getLooper();
        handler = new ServiceHandler(looper);
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        // The service ID can be used to identify the service
        Message message = handler.obtainMessage();
        message.arg1 = startId;
        message.arg2 = flags;
        handler.sendMessage(message);

        // if the service is killed by Android, service starts again
        return START_STICKY;
    }

    /**
     * When the service get destroyed by Android or manually.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Disconnect from the Google API
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        locationRequest = new LocationRequest()
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
            .setSmallestDisplacement(DISPLACEMENT)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Decide if new location is better than older by following some basic criteria.
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    private boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, the new location is better
        if (oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time
        long timeDelta = newLocation.getTime() - oldLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DIFFERENCE_THRESHOLD;
        boolean isSignificantlyOlder = timeDelta < -TIME_DIFFERENCE_THRESHOLD;
        boolean isNewer = timeDelta > 0;

        // If it's been more than $TIME_DIFFERENCE_THRESHOLD since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        }
        // If the new location is more than $TIME_DIFFERENCE_THRESHOLD older, it must be worse
        else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - oldLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > BAD_ACCURACY; // 100 meters

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), oldLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if(isMoreAccurate) {
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }

        return provider1.equals(provider2);
    }

    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            PrefUtils.saveLocating(this, false);
            stopSelf();
            return;
        }

        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this
        );
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopSelf();
    }

    /**
     * The method used to obtain the new location.
     * @param location The new location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        // Wait until we get a good enough location
        if (isBetterLocation(lastRegisteredLocation, location)) {
            // Post the Location object for subscribers
            EventBus.getDefault().postSticky(location);

            // Save the location as last registered
            lastRegisteredLocation = location;
        }
    }

    /**
     * Request the necessary permissions for the location
     * @param activity The activity that needs the location permission
     * @param requestCode The code to request the permission
     * @param resultCode The code to respond to the activity - REQUEST_CODE_LOCATION_SERVICES
     */
    public static void setup(Activity activity, int requestCode, int resultCode) {
        if (PrefUtils.isLegacy(activity)) {
            PrefUtils.saveLocating(activity, false);
            return;
        }

        boolean locationPermission = SystemUtils.requestPermission(
                activity,
                R.string.text_permission_location,
                Manifest.permission.ACCESS_FINE_LOCATION,
                requestCode
        );

        // We have permission, it is time to see if location is enabled, if not just request
        if (locationPermission && PrefUtils.isLocating(activity)) {
            enable(activity, resultCode);
        }
    }

    /**
     * Asks the user to enable the location services
     * @param activity The activity that requests to enable location
     * @param resultCode The code to respond to the activity - REQUEST_CODE_LOCATION_SERVICES
     */
    public static void enable(final Activity activity, final int resultCode) {
        if (SystemUtils.isLocationEnabled(activity)) {
            // Start the location service
            Intent iLoc = new Intent(activity, LocationService.class);
            if (!SystemUtils.isMyServiceRunning(activity, LocationService.class.getName())) {
                activity.startService(iLoc);
            }
        } else {
            // If location not enabled, then request
            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivityForResult(intent, resultCode);
                }
            };

            DialogInterface.OnClickListener onCancel = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    PrefUtils.saveLocating(activity, false);
                }
            };

            AlertDialogHelper.create(
                    activity,
                    R.string.text_enable_gps,
                    onClick,
                    onCancel
            );
        }
    }

    /**
     * Stops the service if running
     * @param context The application context
     */
    public static void stop(Context context) {
        // Stop location service while app is in background
        if (SystemUtils.isMyServiceRunning(context, LocationService.class.getName())) {
            Intent iLoc = new Intent(context, LocationService.class);
            context.stopService(iLoc);
        }
    }

    /** Object responsible for handling the service actions */
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            googleApiClient = new GoogleApiClient.Builder(LocationService.this)
                    .addConnectionCallbacks(LocationService.this)
                    .addOnConnectionFailedListener(LocationService.this)
                    .addApi(LocationServices.API)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
    }
}
