package co.yodo.launcher.business.manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;

import co.yodo.launcher.utils.PrefUtils;
import timber.log.Timber;

/**
 * Created by hei on 15/06/16.
 * Handles the Nearby advertising
 */
public class PromotionManager {
    /** The Activity object */
    private AppCompatActivity context;

    /** Implements all the GoogleApi callbacks */
    private IPromotionListener activity;

    /** Provides an entry point for Google Play services. */
    private GoogleApiClient googleApiClient;

    /** The {@link android.os.Message} object used to broadcast information about the device to nearby devices. */
    private Message activeMessage;

    /** Sets the time in seconds for a published message or a subscription to live */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(Strategy.TTL_SECONDS_MAX).build();

    /** Listener for the advertise */
    public interface IPromotionListener extends
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    }

    public PromotionManager(IPromotionListener activity) {
        if(!(activity instanceof AppCompatActivity)) {
            throw new ExceptionInInitializerError("The class has to be an activity");
        }

        this.context = (AppCompatActivity) activity;
        this.activity = activity;
    }

    /**
     * Starts the GoogleClient for Nearby
     */
    public void startService() {
        if(PrefUtils.isLegacy(context)) {
            return;
        }

        // Connect to the service
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(activity)
                .enableAutoManage(context, activity)
                .build();
        googleApiClient.connect();
    }

    /**
     * Publishes device information to nearby devices. If not successful, attempts to resolve any
     * error related to Nearby permissions by displaying an opt-in dialog. Registers a callback
     * that updates the UI when the publication expires.
     */
    public void publish() {
        if (isNotAvailable()) {
            return;
        }

        Timber.i( "trying to publish" );
        String message = PrefUtils.getBeaconName(context);
        activeMessage = new Message(message.getBytes());

        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Timber.i("no longer publishing");
                        publish();
                    }
                }).build();

        Nearby.Messages.publish(googleApiClient, activeMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Timber.i("published successfully");
                        } else {
                            Timber.i("could not publish");
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
        if (isNotAvailable()) {
            return;
        }

        Timber.i( "trying to unpublish" );
        Nearby.Messages.unpublish(googleApiClient, activeMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Timber.i("unpublished successfully");
                        } else {
                            Timber.i("could not unpublish");
                        }
                    }
                });
    }

    /**
     * Verifies that the GoogleApiClient is ready
     * @return true if it is ready, otherwise false
     */
    private boolean isNotAvailable() {
        return googleApiClient == null || !googleApiClient.isConnected() || googleApiClient.isConnecting();
    }
}
