package co.yodo.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import co.yodo.launcher.broadcastreceiver.BroadcastMessage;
import co.yodo.launcher.helper.AppUtils;

/**
 * Service to obtain the location of the device, this service in particular
 * uses the Android Location package, the implementation of the Android
 * Location API (it does not use Google Play).
 * 
 * @author Luis Talavera
 */
public class LocationService extends Service implements LocationListener {
	/** DEBUG */
	private static final String TAG = LocationService.class.getSimpleName();

    /** The location manager */
	private LocationManager lm;

    /** The Local Broadcast Manager */
	private LocalBroadcastManager lbm;

    /** Last known location */
    private Location lastKnownLocation;

    /** GPS rate, since it consumes more battery than network */
    private static final int GPS_RATE = 4;

    /** Intervals for the updates */
    private static final long LOCATION_INTERVAL = 30 * 1000; // 30 seconds
    private static final long LOCATION_DISTANCE = 10;

    /** Time difference threshold set for one minute */
    private static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;
	
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
	public int onStartCommand(Intent i, int flags, int startId) {
		// get the context
        Context ac = LocationService.this;
		// get local broadcast 
		lbm = LocalBroadcastManager.getInstance( ac );
		// get location manager
		lm = (LocationManager) getSystemService( LOCATION_SERVICE );
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
		// remove the listener
		lm.removeUpdates( this );
	}
	
	/**
	 * The method used to obtain the new location.
	 * @param location The new location object.
	 */
	@Override
	public void onLocationChanged(Location location) {
        if( isBetterLocation( lastKnownLocation, location ) ) {
            AppUtils.Logger( TAG, " >> NEW LOCATION SENT: " + location.toString() );
            Intent i = new Intent( BroadcastMessage.ACTION_NEW_LOCATION );
            i.putExtra( BroadcastMessage.EXTRA_NEW_LOCATION, location );
            lbm.sendBroadcast( i );

            // save the location as last registered
            lastKnownLocation = location;
        }
	}
	
	@Override
	public void onProviderDisabled(String provider) {
        AppUtils.Logger(TAG, "provider disabled: " + provider);
    }

	@Override
	public void onProviderEnabled(String provider) {
        AppUtils.Logger(TAG, "provider enabled: " + provider);

        // If it's a provider we care about
        if( provider.equals( LocationManager.GPS_PROVIDER ) ) {
            lm.requestLocationUpdates( provider,
                    LOCATION_INTERVAL * GPS_RATE,
                    LOCATION_DISTANCE,
                    this );
        }
        else if( provider.equals( LocationManager.NETWORK_PROVIDER ) ) {
            lm.requestLocationUpdates( provider,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    this );
        }
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusAsString = "Available";
        if( status == LocationProvider.OUT_OF_SERVICE )
            statusAsString = "Out of service";
        else if( status == LocationProvider.TEMPORARILY_UNAVAILABLE )
            statusAsString = "Temporarily Unavailable";

        AppUtils.Logger( TAG, provider + " provider status has changed: [" + statusAsString + "]" );
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * The bootstrap for the location service
	 */
	private void bootstrap() {
        String provider = null;

        if( lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            provider = LocationManager.GPS_PROVIDER;
            lm.requestLocationUpdates( provider,
                    LOCATION_INTERVAL * GPS_RATE,
                    LOCATION_DISTANCE,
                    this );

            AppUtils.Logger( TAG, "GPS Location provider has been started" );
        }

        // 4x faster refreshing rate since this provider doesn't consume much battery.
        if( lm.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
            provider = LocationManager.NETWORK_PROVIDER;
            lm.requestLocationUpdates( provider,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    this );

            AppUtils.Logger(TAG, "NETWORK Location provider has been started");
        }

        if( provider == null )
            AppUtils.Logger(TAG, "No providers available");
	}

    /**
     * Decide if new location is better than older by following some basic criteria.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    private boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, the new location is better
        if( oldLocation == null )
            return true;

        // Check if new location is newer in time
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();
        // Check if new location more accurate. Accuracy is radius in meters, so less is better
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();

        if( isMoreAccurate && isNewer )
            return true;
        else if( isMoreAccurate ) {
            // More accurate but not newer can lead to bad fix because of user movement
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // Threshold for the maximum tolerance of time difference
            if( timeDifference > -TIME_DIFFERENCE_THRESHOLD )
                return true;
        }
        return false;
    }
}
