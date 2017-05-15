package co.yodo.launcher.ui.scanner;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableRow;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.recognition.Barcode;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.scanner.contract.QRScanner;

/**
 * Created by luis on 10/01/16.
 * class that implements a handler for the scandit
 * scanner
 */
public class ScanditScanner extends QRScanner implements OnScanListener {
    /** The main object for recognizing a displaying barcodes. */
    private BarcodePicker barcodePicker;

    /** Your Scandit SDK App key is available via your Scandit SDK web account. */
    private static final String sScanditSdkAppKey = "fKiwAnaUTbGsN9Us2fDIIyGYwxHaS3gwbOs21jWzSfU";

    /** GUI Controllers */
    private TableRow opPanel;
    private RelativeLayout pvPanel;

    /** Scandit Settings */
    private ScanSettings settings;

    /** Camera Flags */
    private boolean previewing = false;

    /** Handler used after the scan */
    private Handler handler = new Handler();

    public ScanditScanner(Activity activity) {
        super(activity);

        // Set the key
        ScanditLicense.setAppKey(sScanditSdkAppKey);

        // Setup GUI
        opPanel = (TableRow) act.findViewById(R.id.trOperationsPanel);
        pvPanel = (RelativeLayout) act.findViewById(R.id.previewPanel);

        // Initialize and start the bar code recognition.
        initializeBarcodeScanning();
    }

    /**
     * Initializes the bar code scanning with the basic
     * settings
     */
    private void initializeBarcodeScanning() {
        settings = ScanSettings.create();
        settings.setSymbologyEnabled(Barcode.SYMBOLOGY_QR, true);
        settings.setCameraFacingPreference(ScanSettings.CAMERA_FACING_BACK);
        BarcodePicker picker = new BarcodePicker(act, settings);

        // Create layout parameters to scale it to match the parent
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        // Add the scan view to the root view.
        pvPanel.addView(picker, rParams);
        barcodePicker = picker;

        // Register listener, in order to be notified about relevant events
        barcodePicker.setOnScanListener(this);
    }

    @Override
    public void setFrontFaceCamera(boolean frontFacing) {
        if (frontFacing) {
            settings.setCameraFacingPreference(ScanSettings.CAMERA_FACING_FRONT);
        } else {
            settings.setCameraFacingPreference(ScanSettings.CAMERA_FACING_BACK);
        }
        barcodePicker.applyScanSettings(settings);
    }

    @Override
    public void startScan() {
        if (!previewing) {
            barcodePicker.startScanning();

            opPanel.setVisibility(View.GONE);
            pvPanel.setVisibility(View.VISIBLE);

            previewing = true;
        }
    }

    @Override
    public void stopScan() {
        releaseCamera();
    }

    @Override
    public boolean isScanning() {
        return previewing;
    }

    /**
     * Release the camera resources
     */
    private void releaseCamera() {
        barcodePicker.stopScanning();

        opPanel.setVisibility(View.VISIBLE);
        pvPanel.setVisibility(View.GONE);

        previewing = false;
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            barcodePicker.pauseScanning();

            opPanel.setVisibility(View.VISIBLE);
            pvPanel.setVisibility(View.GONE);

            previewing = false;
        }
    };

    @Override
    public void didScan( ScanSession session ) throws NullPointerException {
        for (Barcode code : session.getAllRecognizedCodes()) {
            String trimmed = code.getData().replaceAll("\\s+", "");
            listener.onScanResult(trimmed);
        }

        handler.post(runnable);
    }

    @Override
    public void destroy() {
        super.destroy();

        // Stops the camera
        releaseCamera();

        // Set to null all the variables
        barcodePicker = null;
        opPanel = null;
        pvPanel = null;
        settings = null;
    }
}