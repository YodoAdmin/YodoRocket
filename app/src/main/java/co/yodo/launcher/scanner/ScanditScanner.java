package co.yodo.launcher.scanner;

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

/**
 * Created by luis on 10/01/16.
 * class that implements a handler for the scandit
 * scanner
 */
public class ScanditScanner extends QRScanner implements OnScanListener {
    /** DEBUG */
    private static final String TAG = ScanditScanner.class.getSimpleName();

    /** The main object for recognizing a displaying barcodes. */
    private BarcodePicker mBarcodePicker;

    /** Your Scandit SDK App key is available via your Scandit SDK web account. */
    public static final String sScanditSdkAppKey = "G4F/1bI6tE+202AsN11AY9vYyZOB7C7tsIu8/xET6FA-";

    /** GUI Controllers */
    private TableRow opPanel;
    private RelativeLayout pvPanel;

    /** Scandit Settings */
    private ScanSettings settings;

    /** Camera Flags */
    private boolean previewing = false;

    /** Instance */
    private static volatile ScanditScanner instance = null;

    private ScanditScanner( Activity activity ) {
        super( activity );
        ScanditLicense.setAppKey( sScanditSdkAppKey );
        // Setup GUI
        opPanel = (TableRow) act.findViewById( R.id.operationsPanel );
        pvPanel = (RelativeLayout) act.findViewById( R.id.previewPanel );
        // Initialize and start the bar code recognition.
        initializeAndStartBarcodeScanning();
    }

    public static ScanditScanner getInstance( Activity activity ) {
         synchronized( ScanditScanner.class ) {
            if( instance == null )
                instance = new ScanditScanner( activity );
        }
        return instance;
    }

    public static void deleteInstance() {
        if( instance != null )
            instance.destroy();
    }

    /**
     * Initializes and starts the bar code scanning.
     */
    public void initializeAndStartBarcodeScanning() {
        settings = ScanSettings.create();
        settings.setSymbologyEnabled( Barcode.SYMBOLOGY_QR, true );
        settings.setCameraFacingPreference( ScanSettings.CAMERA_FACING_BACK );
        BarcodePicker picker = new BarcodePicker( act, settings );
        // Create layout parameters to scale it to match the parent
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT );
        rParams.addRule( RelativeLayout.CENTER_HORIZONTAL );
        // Add the scan view to the root view.
        pvPanel.addView( picker, rParams );
        mBarcodePicker = picker;
        // Register listener, in order to be notified about relevant events
        mBarcodePicker.setOnScanListener( this );
        mBarcodePicker.startScanning( true );
    }

    @Override
    public void startScan() {
        if( !previewing ) {
            //mBarcodePicker.startScanning();
            mBarcodePicker.resumeScanning();

            opPanel.setVisibility( View.GONE );
            pvPanel.setVisibility( View.VISIBLE );

            previewing = true;
        }
    }

    @Override
    public void close() {
        //releaseCamera();
        mBarcodePicker.pauseScanning();

        opPanel.setVisibility( View.VISIBLE );
        pvPanel.setVisibility( View.GONE );

        previewing = false;
    }

    @Override
    public boolean isScanning() {
        return previewing;
    }

    @Override
    public void setFrontFaceCamera( boolean frontFacing ) {
        if( frontFacing )
            settings.setCameraFacingPreference( ScanSettings.CAMERA_FACING_FRONT );
        else
            settings.setCameraFacingPreference( ScanSettings.CAMERA_FACING_BACK );
        mBarcodePicker.applyScanSettings( settings );
    }

    @Override
    public void destroy() {
        releaseCamera();
        instance = null;
    }

    private void releaseCamera() {
        mBarcodePicker.stopScanning();

        opPanel.setVisibility( View.VISIBLE );
        pvPanel.setVisibility( View.GONE );

        previewing = false;
    }

    @Override
    public void didScan( ScanSession session ) {
        for( Barcode code : session.getNewlyRecognizedCodes() ) {
            String trimmed = code.getData().replaceAll( "\\s+", "" );
            listener.onNewData( trimmed );
        }
        handler.post( runnable );
    }

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        public void run() {
            //releaseCamera();
            mBarcodePicker.pauseScanning();

            opPanel.setVisibility( View.VISIBLE );
            pvPanel.setVisibility( View.GONE );

            previewing = false;
        }
    };
}
