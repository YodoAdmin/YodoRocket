package co.yodo.launcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
	private static final String TAG    = LocationService.class.getName();

    /** The location manager */
	private LocationManager lm;

    /** The Local Broadcast Manager */
	private LocalBroadcastManager lbm;

    /** Intervals for the updates */
    private static final int LOCATION_INTERVAL   = 5000;
    private static final float LOCATION_DISTANCE = 10f;
	
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
        if( lm != null )
		    lm.removeUpdates( this );

        unregisterReceiver( mGpsChangeReceiver );
	}
	
	/**
	 * The method used to obtain the new location.
	 * @param location The new location object.
	 */
	@Override
	public void onLocationChanged(Location location) {
		AppUtils.Logger( TAG, " >> NEW LOCATION SENT" );

		Intent i = new Intent( BroadcastMessage.ACTION_NEW_LOCATION );
		i.putExtra( BroadcastMessage.EXTRA_NEW_LOCATION, location );
		lbm.sendBroadcast( i );
	}
	
	@Override
	public void onProviderDisabled(String provider) {
        AppUtils.Logger( TAG, " >> DISABLED " + provider );

        if( lm != null )
            lm.removeUpdates( this );

        bootstrap();
    }

	@Override
	public void onProviderEnabled(String provider) {
        AppUtils.Logger( TAG, " >> ENABLED " + provider );
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * The bootstrap for the location service
	 */
	private void bootstrap() {
        this.registerReceiver(mGpsChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

		// create a criteria
		Criteria c = new Criteria();
		// force GPS 
		c.setAccuracy( Criteria.ACCURACY_FINE );
        String provider = lm.getBestProvider( c, true );

        // start receiving locations updates
        if(provider != null) {
            AppUtils.Logger( TAG, ">> Provider " + provider );
            lm.requestLocationUpdates( provider, LOCATION_INTERVAL, LOCATION_DISTANCE, this );
        }
		else
            AppUtils.Logger( TAG, ">> Provider NULL" );
	}

    private BroadcastReceiver mGpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( lm != null )
                lm.removeUpdates( LocationService.this );

            bootstrap();
        }
    };
}
