package co.yodo.launcher.manager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;

import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;

/**
 * Created by hei on 15/06/16.
 * Handles the Nearby advertising
 */
public class PromotionManager {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = PromotionManager.class.getSimpleName();

    /** The Activity object */
    private AppCompatActivity ac;

    /** Implements all the GoogleApi callbacks */
    private IPromotionListener mActivity;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient mGoogleApiClient;

    /** The {@link android.os.Message} object used to broadcast information about the device to nearby devices. */
    private Message mActiveMessage;

    /** Sets the time in seconds for a published message or a subscription to live */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds( Strategy.TTL_SECONDS_MAX ).build();

    /** Listener for the advertise */
    public interface IPromotionListener extends
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    }

    public PromotionManager( IPromotionListener activity ) {
        if( !( activity instanceof AppCompatActivity ) )
            throw new ExceptionInInitializerError( "The class has to be an activity" );

        this.ac = (AppCompatActivity) activity;
        this.mActivity = activity;
    }

    /**
     * Starts the GoogleClient for Nearby
     */
    public void startService() {
        if( PrefUtils.isLegacy( ac ) )
            return;

        // Connect to the service
        mGoogleApiClient = new GoogleApiClient.Builder( ac )
                .addApi( Nearby.MESSAGES_API )
                .addConnectionCallbacks( mActivity )
                .enableAutoManage( ac, mActivity )
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Publishes device information to nearby devices. If not successful, attempts to resolve any
     * error related to Nearby permissions by displaying an opt-in dialog. Registers a callback
     * that updates the UI when the publication expires.
     */
    public void publish() {
        if( isNotAvailable() )
            return;

        SystemUtils.Logger( TAG, "trying to publish" );
        String message = PrefUtils.getBeaconName( ac );
        mActiveMessage = new Message( message.getBytes() );

        PublishOptions options = new PublishOptions.Builder()
                .setStrategy( PUB_SUB_STRATEGY )
                .setCallback( new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        SystemUtils.Logger( TAG, "no longer publishing" );
                        publish();
                    }
                }).build();

        Nearby.Messages.publish( mGoogleApiClient, mActiveMessage, options )
                .setResultCallback( new ResultCallback<Status>() {
                    @Override
                    public void onResult( @NonNull Status status ) {
                        if( status.isSuccess() ) {
                            SystemUtils.Logger( TAG, "published successfully" );
                        } else {
                            SystemUtils.Logger( TAG, "could not publish" );
                        }
                    }
                });
    }

    /**
     * Stops publishing device information to nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by displaying an
     * opt-in dialog.
     */
    public void unpublish() {
        if( isNotAvailable() )
            return;

        SystemUtils.Logger( TAG, "trying to unpublish" );
        Nearby.Messages.unpublish( mGoogleApiClient, mActiveMessage )
                .setResultCallback( new ResultCallback<Status>() {
                    @Override
                    public void onResult( @NonNull Status status ) {
                        if( status.isSuccess() ) {
                            SystemUtils.Logger( TAG, "unpublished successfully" );
                        } else {
                            SystemUtils.Logger( TAG, "could not unpublish" );
                        }
                    }
                });
    }

    /**
     * Verifies that the GoogleApiClient is ready
     * @return true if it is ready, otherwise false
     */
    private boolean isNotAvailable() {
        return mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
    }
}
