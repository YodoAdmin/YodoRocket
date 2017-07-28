package co.yodo.launcher.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.business.component.Intents;
import co.yodo.launcher.business.component.SKS;
import co.yodo.launcher.business.component.ToastMaster;
import co.yodo.launcher.business.manager.PromotionManager;
import co.yodo.launcher.business.service.LocationService;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.helper.ProgressDialogHelper;
import co.yodo.launcher.ui.adapter.ScannerAdapter;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.dialog.PopupImpl;
import co.yodo.launcher.ui.option.factory.OptionsFactory;
import co.yodo.launcher.ui.scanner.contract.QRScanner;
import co.yodo.launcher.ui.scanner.factory.QRScannerFactory;
import co.yodo.launcher.utils.AppConfig;
import co.yodo.launcher.utils.BluetoothUtil;
import co.yodo.launcher.utils.ESCUtil;
import co.yodo.launcher.utils.ErrorUtils;
import co.yodo.launcher.utils.FormatUtils;
import co.yodo.launcher.utils.GuiUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.utils.SystemUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.AltExchangeRequest;
import co.yodo.restapi.network.requests.CurrenciesRequest;
import co.yodo.restapi.network.requests.ExchRetailRequest;
import co.yodo.restapi.network.requests.QueryLogoRequest;
import sunmi.ds.DSKernel;
import sunmi.ds.callback.IConnectionCallback;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;
import timber.log.Timber;

public class RocketActivity extends BaseActivity implements PromotionManager.IPromotionListener,
        QRScanner.QRScannerListener {
    /** The context object */
    @Inject
    Context context;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    /** Sunmi */
    private DSKernel sDSKernel;

    /** GUI controllers */
    @BindView(R.id.splActivityLauncher)
    SlidingPaneLayout splActivityLauncher;

    @BindView(R.id.llRoot)
    LinearLayout llRoot;

    @BindView(R.id.nivCompanyLogo)
    ImageView nivCompanyLogo;

    @BindView(R.id.tvTotal)
    TextView tvTotal;

    @BindView(R.id.tvCashtender)
    TextView tvCashtender;

    @BindView(R.id.tvCashback)
    TextView tvCashback;

    @BindView(R.id.pgBalance)
    ProgressBar pgBalance;

    @BindView(R.id.tvBalance)
    TextView tvBalance;

    @BindView(R.id.sScannerSelector)
    Spinner sScannerSelector;

    @BindView(R.id.ivYodoGear)
    ImageView ivYodoGear;

    /** Zero value */
    private static String zero;

    /** Popup for tenders and discount*/
    private PopupImpl popupDiscount;
    private PopupImpl popupTender;

    /** Selected Text View (total, cashTender, cashBack) */
    private TextView tvSelected;

    /** Handle all the options of the Rocket */
    private OptionsFactory optsFactory;

    /** Current Scanners */
    private QRScannerFactory scannerFactory;
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager promotionManager;
    private boolean isPublishing = false;

    /** Location */
    private Location location = new Location("flp");

    /** Code for the error dialog */
    private static final int REQUEST_CODE_LOCATION_SERVICES = 0;
    private static final int REQUEST_SETTINGS = 1;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    /** External data */
    private Bundle externBundle;
    private boolean promptResponse = true;
    private String ex_tip;

    /** If it should print the receipt */
    private boolean isPrinting = false;

    private IConnectionCallback connCallback = new IConnectionCallback() {
        @Override
        public void onDisConnect() {
        }

        @Override
        public void onConnected(final ConnState state) {
        }
    };

    private IReceiveCallback receiveCallback = new IReceiveCallback() {
        @Override
        public void onReceiveFile(DSFile arg0) {
        }

        @Override
        public void onReceiveFiles(DSFiles dsFiles) {
        }

        @Override
        public void onReceiveData(DSData data) {
        }

        @Override
        public void onReceiveCMD(DSData arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rocket);

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();

        // register to event bus
        EventBus.getDefault().register(this);

        // Setup the required permissions
        LocationService.setup(
                this,
                PERMISSIONS_REQUEST_LOCATION,
                REQUEST_CODE_LOCATION_SERVICES
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start the scanner if necessary
        if (currentScanner != null && isScanning) {
            isScanning = false;
            currentScanner.startScan();
        }

        /*if (sDSKernel != null) {
            sDSKernel.addConnCallback(connCallback);
            sDSKernel.addReceiveCallback(receiveCallback);
        }
*/
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the scanner while app is not focus (only if scanning)
        if (currentScanner != null && currentScanner.isScanning()) {
            isScanning = true;
            currentScanner.stopScan();
        }

      /*  if (sDSKernel != null) {
            sDSKernel.removeConnCallback(connCallback);
            sDSKernel.removeReceiveCallback(receiveCallback);
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister from event bus
        EventBus.getDefault().unregister(this);

        // Stops location if running
        LocationService.stop(this);
    }

    @Override
    public void onBackPressed() {
        // Send image to secondary screen
        GuiUtils.sendImageContentToSecondaryScreen(sDSKernel, context);

        // If we are scanning, first close the camera
        if (currentScanner != null && currentScanner.isScanning()) {
            currentScanner.stopScan();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void setupGUI() {
        // Injection
        ButterKnife.bind(this);
        YodoApplication.getComponent().inject(this);

        boolean writePermission = SystemUtils.requestPermission(
                RocketActivity.this,
                R.string.text_permission_write_external_storage,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        );

        if (writePermission) {
            // Initialize the Sunmi SDK
            initSDK();
        }

        // Gets the zero value
        zero = getString(R.string.text_zero);

        // Load the popups
        popupDiscount = new PopupImpl(this, tvTotal);
        popupTender = new PopupImpl(this, tvCashtender);

        // Listeners for the inputs
        tvTotal.addTextChangedListener(onChange);
        tvCashtender.addTextChangedListener(onChange);
        tvCashback.addTextChangedListener(onChange);

        // Sliding Panel Configurations
        splActivityLauncher.setParallaxDistance(30);

        // Sets up the spinner for the cameras
        ScannerAdapter.initializeScannerSpinner(this, sScannerSelector);

        // creates the factory for the scanners
        scannerFactory = new QRScannerFactory(this);

        // Setup promotion manager start it
        promotionManager = new PromotionManager(this);
        promotionManager.startService();

        // Options
        optsFactory = new OptionsFactory(this, promotionManager);

        // Load the company logo
        loadAndSaveLogo(PrefUtils.getLogoUrl(context));
        YodoApi.execute(
                new QueryLogoRequest(),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String code = response.getCode();
                        if (code.equals(ServerResponse.AUTHORIZED)) {
                            String logoName = response.getParams().getLogo();
                            if (logoName != null) {
                                loadAndSaveLogo(AppConfig.LOGO_PATH + logoName);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                    }
                }
        );

        // Reset all the values
        reset(null);

        // If it is the first login, setData the navigation panel
        if (PrefUtils.isFirstLogin(context)) {
            splActivityLauncher.openPane();
            PrefUtils.saveFirstLogin(context, false);
        }
    }

    @Override
    protected void updateData() {
        // Handle external Requests
        externBundle = getIntent().getExtras();
        if (externBundle != null) {
            BigDecimal total = new BigDecimal(externBundle.getString(Intents.TOTAL, "0.00"));
            BigDecimal cashTender = new BigDecimal(externBundle.getString(Intents.CASH_TENDER, "0.00"));
            BigDecimal cashBack = new BigDecimal(externBundle.getString(Intents.CASH_BACK, "0.00"));
            final String currency = externBundle.getString(Intents.TENDER_CURRENCY);

            // Checks values and set
            GuiUtils.validateAndSetAmount(total, tvTotal);
            GuiUtils.validateAndSetAmount(cashTender, tvCashtender);
            GuiUtils.validateAndSetAmount(cashBack, tvCashback);
            PrefUtils.saveTenderCurrency(context, currency);

            // Setup the inputs
            select(tvCashtender);
            tvTotal.setOnClickListener(null);

            // Handle the prompt of the response message from the server
            promptResponse = externBundle.getBoolean(Intents.PROMPT_RESPONSE, true);
        }
    }

    @Override
    public void updateUI() {
        // Setup currencies
        GuiUtils.setTenderCurrencyIcon(context, tvCashtender);
        GuiUtils.setQuickKeys(this);

        // Setup ic_discount
        GuiUtils.setViewIcon(context, tvTotal,
                (PrefUtils.getDiscount(context) > 0) ? R.mipmap.ic_discount : null
        );

        // Setup Background
        llRoot.setBackgroundColor(PrefUtils.getCurrentBackground(context));

        // Setup the layout mode
        if (PrefUtils.isPortraitMode(context)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        // Get the current balance
        requestBalance();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.i("GoogleApiClient connected");
        if (PrefUtils.isAdvertising(context)) {
            promotionManager.publish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.i("GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Timber.i("Connection to GoogleApiClient failed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    showCamera();
                }
                break;

            case PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    LocationService.enable(this, REQUEST_CODE_LOCATION_SERVICES);
                } else {
                    // Permission Denied
                    PrefUtils.saveLocating(context, false);
                }
                break;

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    initSDK();
                    SystemUtils.saveMerchantLogo(context);
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if (!SystemUtils.isLocationEnabled(context)) {
                    PrefUtils.saveLocating(context, false);
                }
                break;

            case REQUEST_SETTINGS:
                if (resultCode == RESULT_FIRST_USER) {
                    setResult(RESULT_FIRST_USER);
                    finish();
                }
                break;
        }
    }

    @SuppressWarnings( "unused" ) // it receives events from the Location Service
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLocationEvent(Location location) {
        // Remove Sticky Event
        EventBus.getDefault().removeStickyEvent(Location.class);

        // Process the Event
        this.location = location;
    }

    /**
     * Sets the currency for a transaction
     * @param v View, used to get the title
     */
    @SuppressWarnings("ConstantConditions")
    public void currency(View v) {
        splActivityLauncher.closePane();
        optsFactory.getOption(OptionsFactory.Option.CURRENCY).execute();
    }

    /**
     * Sets a ic_discount for all the transactions
     * @param v The view is not used
     */
    @SuppressWarnings("ConstantConditions")
    public void discount(View v) {
        splActivityLauncher.closePane();
        optsFactory.getOption(OptionsFactory.Option.DISCOUNT).execute();
    }

    /**
     * Opens the settings activity
     * @param v View, not used
     */
    public void settings(View v) {
        splActivityLauncher.closePane();
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    @SuppressWarnings("ConstantConditions")
    public void balance(View v) {
        splActivityLauncher.closePane();
        optsFactory.getOption(OptionsFactory.Option.BALANCE).execute();
    }

    /**
     * Shows some basic information about the POS
     * @param v View, not used
     */
    @SuppressWarnings("ConstantConditions")
    public void about(View v) {
        splActivityLauncher.closePane();
        optsFactory.getOption(OptionsFactory.Option.ABOUT).execute();
    }

    /**
     * Logout the user
     * @param v View, not used
     */
    public void logout(View v) {
        PrefUtils.setLoggedIn(context, false);
        PrefUtils.saveLogoUrl(context, null);
        finish();
    }

    /**
     * Change the selected view (purchase, cash tender, or cash back)
     * @param v The selected view
     */
    public void select(View v) {
        if (tvSelected != null) {
            tvSelected.setBackgroundResource(R.drawable.bg_show_field);
        }

        tvSelected = (TextView) v;
        tvSelected.setBackgroundResource(R.drawable.bg_selected_field);
    }

    /**
     * Resets the values to 0.00
     * @param v The View, not used
     */
    public void reset(View v) {
        // Set total to 0, only if not opened from external app
        if (externBundle == null) {
            tvTotal.setText(zero);
            select(tvTotal);
        } else {
            select(tvCashtender);
        }

        // Set values to 0
        tvCashtender.setText(zero);
        tvCashback.setText(zero);

        // Set merchant currency (default) to tender
        PrefUtils.saveTenderCurrency(context, PrefUtils.getMerchantCurrency(context));
        updateUI();
    }

    /**
     * Handle numeric button clicked
     * @param v View, used to get the number
     */
    @SuppressWarnings("unused")
    public void value(View v) {
        final String value = ((Button) v).getText().toString();

        // This button is not working value_.
        if (value.equals(getString(R.string.value__))) {
            return;
        }

        for (int i = 0; i < value.length(); ++i) {
            final String current = tvSelected.getText().toString();
            BigDecimal temp = new BigDecimal(current + value);
            final String tvNewValue = temp.multiply(BigDecimal.TEN).setScale(2, RoundingMode.DOWN).toString();
            tvSelected.setText(tvNewValue);
        }
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    @SuppressWarnings("unused")
    public void add(View v) {
        final String amount  = ((Button)v).getText().toString();
        final String current = tvSelected.getText().toString();

        if (amount.equals(getString(R.string.coins_0))) {
            tvSelected.setText(zero);
        } else {
            BigDecimal value = new BigDecimal(current).add(new BigDecimal(amount));
            final String tvNewValue = value.setScale(2, RoundingMode.DOWN).toString();
            tvSelected.setText(tvNewValue);
        }
    }

    /**
     * Copies the value of the purchase to the tender TextView
     * @param v, The view, not used
     */
    public void purchaseToTender(View v) {
        tvCashtender.setText(tvTotal.getText());
    }

    /**
     * Handles on back pressed
     * @param v View, not used
     */
    public void back(View v) {
        onBackPressed();
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void makePayment(View v) {
        final String total = tvTotal.getText().toString();
        final String cashTender = tvCashtender.getText().toString();
        final String cashBack = tvCashback.getText().toString();
        final String balance = tvBalance.getText().toString();
        final String currency = PrefUtils.getTenderCurrency(context);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        if(total.equals(zero) && cashTender.equals(zero) && cashBack.equals(zero)) {
            ErrorUtils.handleError(this, R.string.error_payment, false);
        }
        else if (PrefUtils.isCashPaymentAllowed(context) && balance.equals(zero)) {
            progressManager.create(
                    RocketActivity.this,
                    ProgressDialogHelper.ProgressDialogType.TRANSPARENT
            );

            // Verifies if it should print the receipt
            isPrinting = PrefUtils.isPrintingCash(context);
            YodoApi.execute(new AltExchangeRequest("0", "X", // Type 0, any client => need to be improved
                            total, cashTender, cashBack, latitude, longitude, currency
                    ), callback
            );
        }
        else {
            boolean cameraPermission = SystemUtils.requestPermission(
                    RocketActivity.this,
                    R.string.text_permission_camera,
                    Manifest.permission.CAMERA,
                    PERMISSIONS_REQUEST_CAMERA
            );

            if (cameraPermission) {
                showCamera();
            }
        }
    }

    /**
     * Request the permission for the camera
     */
    private void showCamera() {
        // Rotate the yodo year icon
        GuiUtils.rotateImage(ivYodoGear);

        // Process the scanner
        if (currentScanner != null && currentScanner.isScanning()) {
            currentScanner.stopScan();
        } else {
            currentScanner = scannerFactory.getScanner(
                    (QRScannerFactory.SupportedScanner) sScannerSelector.getSelectedItem()
            );
            currentScanner.startScan();
        }
    }

    @Override
    public void onScanResult(String data) {
        // Get subtotal with ic_discount
        BigDecimal ic_discount = new BigDecimal(PrefUtils.getDiscount(context)).movePointLeft(2);
        BigDecimal subTotal = new BigDecimal(tvTotal.getText().toString()).multiply(
                BigDecimal.ONE.subtract(ic_discount)
        );

        final String cashtender = tvCashtender.getText().toString();
        final String cashback   = tvCashback.getText().toString();
        final String currency = PrefUtils.getTenderCurrency(context);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        SKS code = SKS.build(data);
        if (code == null) {
            SystemUtils.startSound(context, AppConfig.ERROR);
            ToastMaster.makeText(this, R.string.exchange_error, Toast.LENGTH_LONG).show();
        } else {
            final String client = code.getClient();
            final SKS.PAYMENT method = code.getPaymentMethod();

            ex_tip = subTotal.multiply(
                    code.getTip()
            ).setScale(2, RoundingMode.DOWN).toString();

            final String total = subTotal.multiply(
                    BigDecimal.ONE.add(code.getTip())
            ).setScale(2, RoundingMode.DOWN).toString();

            progressManager.create(
                    this,
                    ProgressDialogHelper.ProgressDialogType.TRANSPARENT
            );

            switch (method) {
                case YODO:
                    // Verifies if it should print the receipt
                    isPrinting = PrefUtils.isPrintingYodo(context);
                    YodoApi.execute(
                            new ExchRetailRequest(client,
                                    total, cashtender, cashback, latitude, longitude, currency
                            ),
                            callback
                    );
                    break;

                case STATIC:
                case HEART:
                    // Verifies if it should print the receipt
                    if (method.equals(SKS.PAYMENT.HEART)) {
                        isPrinting = PrefUtils.isPrintingYodo(context);
                    } else {
                        isPrinting = PrefUtils.isPrintingStatic(context);
                    }

                    YodoApi.execute(
                            new AltExchangeRequest(String.valueOf(method.ordinal()), client,
                                    total, cashtender, cashback, latitude, longitude, currency
                            ),
                            callback
                    );
                    break;

                default:
                    progressManager.destroy();
                    ErrorUtils.handleError(this, R.string.error_sks, false);
                    break;
            }
        }
    }

    /**
     * Init the sunmi second screen SDK
     */
    private void initSDK() {
        sDSKernel = DSKernel.newInstance();
        sDSKernel.init(this, connCallback);
        sDSKernel.addReceiveCallback(receiveCallback);
    }

    /**
     * Loads the logo in UI and saves the path
     * @param url The string with the path
     */
    private void loadAndSaveLogo(String url) {
        if (url != null) {
            PrefUtils.saveLogoUrl(context, url);
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_loading)
                    .error(R.mipmap.ic_no_image)
                    .into(nivCompanyLogo);

            if (sDSKernel != null) {
                SystemUtils.saveMerchantLogo(context);
            }
        }
    }

    /**
     * Gets the used currencies (merchant and tender) and requests their rates from the
     * server or cache
     */
    private void requestBalance() {
        // Balance GUI
        tvBalance.setVisibility(View.GONE);
        pgBalance.setVisibility(View.VISIBLE);

        // Loads the popups
        popupTender.load();

        // Get currencies
        String merchant = PrefUtils.getMerchantCurrency(context);
        String tender = PrefUtils.getTenderCurrency(context);
        YodoApi.execute(
                new CurrenciesRequest(merchant, tender),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String sMerchRate  = response.getParams().getMerchRate();
                        final String sTenderRate = response.getParams().getTenderRate();

                        if (sMerchRate != null && sTenderRate != null) {
                            // Get the rates in BigDecimals
                            BigDecimal merchRate  = new BigDecimal(sMerchRate);
                            BigDecimal tenderRate = new BigDecimal(sTenderRate);

                            // Get the values of the money TextViews
                            BigDecimal total      = new BigDecimal(tvTotal.getText().toString());
                            BigDecimal cashtender = new BigDecimal(tvCashtender.getText().toString());
                            BigDecimal cashback   = new BigDecimal(tvCashback.getText().toString());

                            // Get the tender in merchant currency
                            BigDecimal merchTender = cashtender.multiply(
                                    merchRate.divide(tenderRate, 4, RoundingMode.DOWN)
                            );
                            popupTender.setData(merchTender.toString());

                            // Get subtotal with ic_discount
                            BigDecimal discount = new BigDecimal(PrefUtils.getDiscount(context)).movePointLeft(2);
                            BigDecimal subTotal = total.multiply(
                                    BigDecimal.ONE.subtract(discount)
                            );
                            popupDiscount.setData(subTotal.toString());

                            // Only calculate cashBack when cash payments are allowed
                            if (PrefUtils.isCashPaymentAllowed(context)) {
                                if (tvSelected != null && tvSelected != tvCashback) {
                                    cashback = merchTender.subtract(subTotal);
                                    tvCashback.removeTextChangedListener(onChange);
                                    if (cashback.compareTo(BigDecimal.ZERO) == -1) {
                                        cashback = BigDecimal.ZERO;
                                    }
                                    tvCashback.setText(FormatUtils.truncateDecimal(cashback.toString()));
                                    tvCashback.addTextChangedListener(onChange);
                                }
                            }

                            // Get balance
                            BigDecimal balance = merchTender.subtract(
                                    subTotal.add(cashback)
                            );

                            tvBalance.setText(FormatUtils.truncateDecimal(balance.toString()));
                            tvBalance.setVisibility(View.VISIBLE);
                            pgBalance.setVisibility(View.GONE);

                            // Update sunmi secondary screen
                            updateSecondaryScreen(FormatUtils.truncateDecimal(
                                    merchTender.toString()
                            ));
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        ErrorUtils.handleApiError(RocketActivity.this, error, false);
                    }
                }
        );
    }

    private final TextWatcher onChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable editable) {
            requestBalance();
        }
    };

    private final RequestCallback callback = new RequestCallback() {
        @Override
        public void onPrepare() {
            if (PrefUtils.isAdvertising(context)) {
                isPublishing = true;
                promotionManager.unpublish();
            }
        }

        @Override
        public void onResponse(ServerResponse response) {
            progressManager.destroy();

            // If it was publishing before the request
            if (isPublishing) {
                isPublishing = false;
                promotionManager.publish();
            }

            final String code = response.getCode();
            switch (code) {
                case ServerResponse.AUTHORIZED:
                    final String ex_code = response.getCode();
                    final String ex_authbumber = response.getAuthNumber();
                    final String ex_message = response.getMessage();
                    final String ex_account = response.getParams().getAccount();
                    final String ex_purchase = response.getParams().getPurchase();
                    final String ex_delta = response.getParams().getAmountDelta();

                    final String message = getString(R.string.exchange_auth) + " " + ex_authbumber + "\n" +
                            getString(R.string.exchange_message) + " " + ex_message;

                    DialogInterface.OnClickListener onClick;

                    if (externBundle != null) {
                        final Intent data = new Intent();
                        data.putExtra(Intents.RESULT_CODE, ex_code);
                        data.putExtra(Intents.RESULT_AUTH, ex_authbumber);
                        data.putExtra(Intents.RESULT_MSG, ex_message);
                        data.putExtra(Intents.RESULT_ACC, ex_account);
                        data.putExtra(Intents.RESULT_PUR, ex_purchase);
                        data.putExtra(Intents.RESULT_TIP, ex_tip);
                        data.putExtra(Intents.RESULT_DEL, ex_delta);
                        setResult(RESULT_OK, data);

                        onClick = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                finish();
                            }
                        };
                    } else {
                        onClick = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reset(null);
                            }
                        };
                    }

                    // Verify if there is a printer
                    if (BluetoothUtil.getDevice() != null && isPrinting) {
                        // Get the cash values
                        final String total = tvTotal.getText().toString();
                        final String cashTender = tvCashtender.getText().toString();
                        final String cashBack = tvCashback.getText().toString();
                        final String currency = PrefUtils.getTenderCurrency(context);

                        // Print data
                        BluetoothUtil.printData(
                                ESCUtil.parseData(response, total, cashTender, cashBack, currency)
                        );
                    }

                    if (promptResponse) {
                        AlertDialogHelper.create(RocketActivity.this, message, onClick);
                    } else {
                        GuiUtils.sendImageContentToSecondaryScreen(sDSKernel, context);
                        finish();
                    }
                    break;

                case ServerResponse.ERROR_DUP_AUTH:
                    ErrorUtils.handleError(RocketActivity.this, R.string.error_20, false);
                    break;

                default:
                    ErrorUtils.handleError(RocketActivity.this, R.string.error_server, false);
                    break;
            }
        }

        @Override
        public void onError(Throwable error) {
            progressManager.destroy();
            ErrorUtils.handleApiError(RocketActivity.this, error, false);

            // If it was publishing before the request
            if (isPublishing) {
                isPublishing = false;
                promotionManager.publish();
            }
        }
    };

    private void updateSecondaryScreen(String cashTender) {
        final String currency = PrefUtils.getMerchantCurrency(context);
        final String purchase = tvTotal.getText().toString();
        final String cashBack = tvCashback.getText().toString();
        final String balance = tvBalance.getText().toString();
        final String format = "%s %s\n";

        GuiUtils.sendTextContentToSecondaryScreen(sDSKernel,
                currency + "\n" +
                String.format(format, getString(R.string.text_total), purchase) +
                String.format(format, getString(R.string.text_cash_tender ), cashTender) +
                String.format(format, getString(R.string.text_cash_back), cashBack) +
                String.format(format, getString(R.string.text_balance), balance)
        );
    }
}
