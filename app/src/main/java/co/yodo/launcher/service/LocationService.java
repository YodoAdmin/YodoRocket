package co.yodo.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import co.yodo.launcher.helper.AppUtils;

/**
 * Service to obtain the location of the device, this service in particular
 * uses the Android Location package, the implementation of the Android
 * Location API (it does not use Google Play).
 *
 * @author Luis Talavera
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = LocationService.class.getSimpleName();

    /** Google API object */
    private GoogleApiClient mGoogleApiClient;

    /** Location request object */
    private LocationRequest mLocationRequest;

    /** Last location saved */
    private Location lastRegisteredLocation;

    /** Time difference threshold */
    private static final int TIME_DIFFERENCE_THRESHOLD = 60 * 1000; // 1 minute(s)

    /** Location updates intervals in sec */
    private static final int UPDATE_INTERVAL  = 30 * 1000; // 30 second(s)
    private static final int FASTEST_INTERVAL = 20 * 1000; // 20 second(s)
    private static final float DISPLACEMENT   = 10.0F;     // 10 meter(s)

    /** Accuracy requirements */
    private static final int BAD_ACCURACY = 100; // 100 meters

    @Override
    public void onCreate() {
        AppUtils.Logger( TAG, ">> Created" );
        super.onCreate();
    }

    /**
     * It gets called when the service is started.
     *
     * @param i The intent received.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return Using START_STICKY the service will run again if got killed by
     * the service.
     */
    @Override
    public int onStartCommand( Intent i, int flags, int startId ) {
        // Configurations
        bootstrap();
        // if the service is killed by Android, service starts again
        return START_STICKY;
    }

    /**
     * When the service get destroyed by Android or manually.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.Logger( TAG, ">> Destroyed" );
        // Disconnect from the Google API
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    /**
     * The bootstrap for the location service
     */
    private void bootstrap() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();

        if( !mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() )
            mGoogleApiClient.connect();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL );
        mLocationRequest.setFastestInterval( FASTEST_INTERVAL );
        mLocationRequest.setSmallestDisplacement( DISPLACEMENT );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
    }

    /**
     * Decide if new location is better than older by following some basic criteria.
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    private boolean isBetterLocation( Location oldLocation, Location newLocation ) {
        // If there is no old location, the new location is better
        if( oldLocation == null )
            return true;

        // Check if new location is newer in time
        long timeDelta = newLocation.getTime() - oldLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DIFFERENCE_THRESHOLD;
        boolean isSignificantlyOlder = timeDelta < -TIME_DIFFERENCE_THRESHOLD;
        boolean isNewer = timeDelta > 0;

        // If it's been more than $TIME_DIFFERENCE_THRESHOLD since the current location, use the new location
        // because the user has likely moved
        if( isSignificantlyNewer )
            return true;
            // If the new location is more than $TIME_DIFFERENCE_THRESHOLD older, it must be worse
        else if( isSignificantlyOlder )
            return false;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) ( newLocation.getAccuracy() - oldLocation.getAccuracy() );
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > BAD_ACCURACY; // 100 meters

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider( newLocation.getProvider(), oldLocation.getProvider() );

        // Determine location quality using a combination of timeliness and accuracy
        if( isMoreAccurate )
            return true;
        else if( isNewer && !isLessAccurate )
            return true;
        else if( isNewer && !isSignificantlyLessAccurate && isFromSameProvider )
            return true;

        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider( String provider1, String provider2 ) {
        if( provider1 == null )
            return provider2 == null;
        return provider1.equals( provider2 );
    }

    @Override
    public void onConnected( Bundle bundle ) {
        createLocationRequest();
        //lastTimestamp = null;
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );
    }

    @Override
    public void onConnectionSuspended( int i ) {
        AppUtils.Logger( TAG, "GoogleApiClient connection has been suspended" );
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult connectionResult ) {
        AppUtils.Logger( TAG, "GoogleApiClient connection has failed" );
        stopSelf();
    }

    /**
     * The method used to obtain the new location.
     * @param location The new location object.
     */
    @Override
    public void onLocationChanged( Location location ) {
        // Wait until we get a good enough location
        if( isBetterLocation( lastRegisteredLocation, location ) ) {
            // Post the Location object for subscribers
            EventBus.getDefault().postSticky( location );
            // Save the location as last registered
            lastRegisteredLocation = location;
            // Print location for debugging
            AppUtils.Logger( TAG, location.toString() );
        }
    }
}
