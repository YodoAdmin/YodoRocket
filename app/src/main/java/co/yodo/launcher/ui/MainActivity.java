package co.yodo.launcher.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.business.component.Intents;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.utils.ErrorUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.utils.SystemUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.AuthMerchDeviceRequest;
import co.yodo.restapi.network.requests.QueryCurrencyRequest;
import sunmi.ds.DSKernel;
import sunmi.ds.callback.IConnectionCallback;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;

public class MainActivity extends BaseActivity {
    /** The application context */
    @Inject
    Context context;

    /** Sunmi */
    @Inject
    DSKernel sDSKernel;

    /** Code for the error dialog */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    /** Request Code (Activity Results) */
    private static final int ACTIVITY_REGISTRATION_REQUEST = 1;
    private static final int ACTIVITY_LAUNCHER_REQUEST = 2;

    /** Bundle in case of external call */
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupGUI();
        updateData();
    }

    @Override
    protected void setupGUI() {
        // Inject
        YodoApplication.getComponent().inject(this);
    }

    @Override
    protected void updateData() {
        // Handle external requests
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Intents.ACTION.equals(action)) {
                bundle = intent.getExtras();
            }
        }

        // Get the main system booleans
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );
        boolean isLegacy = PrefUtils.isLegacy(context);

        // Verify Google Play Services
        if (hasServices || isLegacy) {
            // If has services, then it is not legacy
            if (hasServices) {
                PrefUtils.setLegacy(context, false);
            }

            setupPermissions();
        }
    }

    /**
     * Gets the result from the activities called with the function startActivityForResult
     * @param requestCode The code that indicates which activity was started with startActivityForResult
     * @param resultCode If the result was ok or not
     * @param data The result intent sent by the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                    // Google play services installed, start the app again
                    PrefUtils.setLegacy(context, false);
                    Intent intent = new Intent(context, MainActivity.class);
                    if (bundle != null) intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    break;

                case ACTIVITY_REGISTRATION_REQUEST:
                    // Get the merchant currency
                    syncCurrencies();
                    break;

                case ACTIVITY_LAUNCHER_REQUEST:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
            switch (requestCode) {
                case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                    // Denied to install, restart in legacy mode
                    PrefUtils.setLegacy(context, true);
                    Intent intent = new Intent(context, MainActivity.class);
                    if (bundle != null) intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        } else if (resultCode == RESULT_FIRST_USER) {
            Intent intent = new Intent(context, RocketActivity.class);
            if (bundle != null) intent.putExtras(bundle);
            startActivityForResult(intent, ACTIVITY_LAUNCHER_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    authenticateUser();
                } else {
                    // Permission Denied
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
                MainActivity.this,
                R.string.text_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if (phoneStatePermission) {
            authenticateUser();
        }
    }

    /**
     * Gets the permission to generate a hardware token
     * and authenticates the user
     */
    private void authenticateUser() {
        // Get the main user booleans
        boolean isLoggedIn = PrefUtils.isLoggedIn(context);
        boolean hasMerchCurr = PrefUtils.getMerchantCurrency(context) != null;
        boolean hasTenderCurr = PrefUtils.getTenderCurrency(context) != null;

        if (isLoggedIn && hasMerchCurr && hasTenderCurr) {
            Intent intent = new Intent(context, RocketActivity.class);
            if (bundle != null) intent.putExtras(bundle);
            startActivityForResult(intent, ACTIVITY_LAUNCHER_REQUEST);
        } else {
            YodoApi.execute(
                    new AuthMerchDeviceRequest(),
                    new RequestCallback() {
                        @Override
                        public void onPrepare() {
                        }

                        @Override
                        public void onResponse(ServerResponse response) {
                            final String code = response.getCode();
                            switch (code) {
                                case ServerResponse.AUTHORIZED:
                                    syncCurrencies();
                                    break;

                                case ServerResponse.ERROR_FAILED:
                                    PrefUtils.setLoggedIn(context, false);
                                    Intent intent = new Intent(context, RegistrationActivity.class);
                                    if (bundle != null) intent.putExtras(bundle);
                                    startActivityForResult(intent, ACTIVITY_REGISTRATION_REQUEST);
                                    break;

                                default:
                                    handleServerError(R.string.error_server);
                                    break;
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            handleApiError(throwable);
                        }
                    }
            );
        }
    }

    /**
     * Request the merchant currency to the server
     */
    private void syncCurrencies() {
        YodoApi.execute(
                new QueryCurrencyRequest(),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String code = response.getCode();
                        if (code.equals(ServerResponse.AUTHORIZED)) {
                            // Set currencies
                            String currency = response.getParams().getCurrency();
                            final boolean savedMCurr = PrefUtils.saveMerchantCurrency(context, currency);
                            final boolean savedTCurr = PrefUtils.saveTenderCurrency(context, currency);

                            if (savedMCurr && savedTCurr) {
                                // Start the app
                                PrefUtils.setLoggedIn(context, true);
                                Intent intent = new Intent(context, RocketActivity.class);
                                if (bundle != null) intent.putExtras(bundle);
                                startActivityForResult(intent, ACTIVITY_LAUNCHER_REQUEST);
                            } else {
                                handleServerError(R.string.error_currency);
                            }
                        } else {
                            handleServerError(R.string.error_server);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        handleApiError(throwable);
                    }
                }
        );
    }

    /**
     * Takes an action over an error from the server
     * @param error The resource message
     */
    private void handleServerError(int error) {
        ErrorUtils.handleError(
                MainActivity.this,
                error,
                true
        );
    }

    /**
     * Takes an action over the errors in the requests
     * @param throwable The exception
     */
    private void handleApiError(Throwable throwable) {
        ErrorUtils.handleApiError(
                MainActivity.this,
                throwable,
                true
        );
    }
}
