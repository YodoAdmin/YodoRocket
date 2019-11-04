package co.yodo.launcher.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.yodo.launcher.BuildConfig;
import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.helper.ProgressDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.utils.ErrorUtils;
import co.yodo.launcher.utils.GuiUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.RegMerchDeviceRequest;

public class RegistrationActivity extends BaseActivity {
    /** The application context */
    @Inject
    Context context;

    /** GUI Controllers */
    @BindView(R.id.tietActivationCode)
    TextInputEditText tietActivationCode;

    @BindView(R.id.tvVersion)
    TextView tvVersion;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setupGUI();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupGUI() {
        // Injection
        ButterKnife.bind(this);
        YodoApplication.getComponent().inject(this);

        // Setup the toolbar
        GuiUtils.setActionBar(this);
    }

    @Override
    protected void updateData() {
        if (PrefUtils.isLoggedIn(context)) {
            finish();
        }
        final String message = String.format(Locale.getDefault(), "%s %s/%s",
                getString(R.string.text_version),
                BuildConfig.VERSION_NAME,
                YodoApi.getAlias()
        );
        tvVersion.setText(message);
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    @OnClick(R.id.acbRegistration)
    public void register(View v) {
        // Get the token
        String token = tietActivationCode.getText().toString();
        if (token.isEmpty()) {
            ErrorUtils.handleFieldError(
                    context,
                    tietActivationCode,
                    R.string.error_required_field
            );
        } else {
            progressManager.create(
                    RegistrationActivity.this,
                    ProgressDialogHelper.ProgressDialogType.NORMAL
            );

            YodoApi.execute(
                    new RegMerchDeviceRequest(token),
                    new RequestCallback() {
                        @Override
                        public void onPrepare() {
                        }

                        @Override
                        public void onResponse(ServerResponse response) {
                            progressManager.destroy();
                            final String code = response.getCode();
                            switch (code) {
                                case ServerResponse.AUTHORIZED_REGISTRATION:
                                    setResult(RESULT_OK);
                                    finish();
                                    break;

                                case ServerResponse.ERROR_DUP_AUTH:
                                    ErrorUtils.handleError(
                                            RegistrationActivity.this,
                                            R.string.error_20,
                                            false
                                    );
                                    break;

                                default:
                                    ErrorUtils.handleError(
                                            RegistrationActivity.this,
                                            R.string.error_server,
                                            false
                                    );
                                    break;
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            progressManager.destroy();
                            ErrorUtils.handleApiError(
                                    RegistrationActivity.this,
                                    throwable,
                                    false
                            );
                        }
                    }
            );
        }
    }

    /**
     * Restarts the application to authenticate the user
     * @param v The view of the button, not used
     */
    public void restart(View v) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
