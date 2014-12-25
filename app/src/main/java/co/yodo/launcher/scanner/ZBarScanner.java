package co.yodo.launcher.scanner;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

/** Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.scanner.ZBarUtils.CameraPreview;

public class ZBarScanner extends QRScanner {
	/** DEBUG */
	private static final String TAG = ZBarScanner.class.getName();

    /** Camera */
	private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    
    /** GUI Controllers */
    private ImageScanner scanner;
    private FrameLayout preview;
	private LinearLayout opPanel;

    private boolean previewing = false;
    
    static {
        System.loadLibrary( "iconv" );
    } 
    
    /** Instance */
    private static volatile ZBarScanner instance = null;

	private ZBarScanner(Activity activity) {
		super( activity );
		
		autoFocusHandler = new Handler();

        // Instance barcode scanner 
        scanner = new ImageScanner();
        scanner.setConfig( 0, Config.X_DENSITY, 3 );
        scanner.setConfig( 0, Config.Y_DENSITY, 3 );
        
        preview = (FrameLayout) act.findViewById( R.id.cameraPreview );
        opPanel = (LinearLayout) act.findViewById( R.id.operationsPanel );
	}
	
	public static ZBarScanner getInstance(Activity activity) {
		synchronized(ZBarScanner.class) {
			if(instance == null)
				instance = new ZBarScanner(activity);
		}
		return instance;
	}
	
	public static ZBarScanner getInstance() {
		return instance;
	}

	@Override
	public void startScan() {
		if( !previewing ) {
			mCamera = getCameraInstance();
			mPreview = new CameraPreview( this.act, mCamera, previewCb, autoFocusCB );
			preview.addView( mPreview );
			
			opPanel.setVisibility( View.GONE );
			preview.setVisibility( View.VISIBLE );
			
			mCamera.setPreviewCallback( previewCb );
			mCamera.startPreview();
			
			/*try {
				mCamera.autoFocus(autoFocusCB);
	        } catch(Exception e) {
	        	Utils.logger(TAG, "Error starting camera preview: " + e.getMessage());
	        }*/
			
			previewing = true;
		}
	}
	
	@Override
	public boolean isScanning() {
		return previewing;
	}

	@Override
	public void destroy() {
		releaseCamera();
		instance = null;
	}
	
	/** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
    	Camera c = null;
        try {
            c = openFrontFacingCamera();
        } catch( Exception e ) {
            AppUtils.Logger( TAG, "Exception = " + e );
        }
        return c;
    }

    private void releaseCamera() {
    	opPanel.setVisibility( View.VISIBLE );
    	preview.setVisibility( View.GONE );
    	
        if( mCamera != null ) {
        	mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback( null );
            mCamera.release();
            mCamera = null;
        }
        
        if(mPreview != null) {
        	preview.removeView( mPreview );
        	mPreview = null;
        }
        
        previewing = false;
    }

    private Runnable doAutoFocus = new Runnable() {
    	@Override
    	public void run() {
    		if(previewing)
    			mCamera.autoFocus( autoFocusCB );
    	}
    };

    PreviewCallback previewCb = new PreviewCallback() {
    	@Override
    	public void onPreviewFrame(byte[] data, Camera camera) {
    		Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();

            Image barcode = new Image( size.width, size.height, "Y800" );
            barcode.setData( data );

            int result = scanner.scanImage( barcode );
                
            if( result != 0 ) {
            	previewing = false;
                mCamera.setPreviewCallback( null );
                mCamera.stopPreview();
                    
                SymbolSet syms = scanner.getResults();
                for( Symbol sym : syms ) {
                    AppUtils.Logger( TAG, sym.getData() );
        			
        			if( listener != null )
        				listener.onNewData( resultData );
                }
                releaseCamera();
            }
    	}
    };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
    	@Override
    	public void onAutoFocus(boolean success, Camera camera) {
    		autoFocusHandler.postDelayed( doAutoFocus, 1000 );
        }
    };
    
    /**
     * tries to open the front camera
     * @return Camera
     */
    private static Camera openFrontFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        
        for( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
                try {
                    cam = Camera.open( camIdx );
                } catch( RuntimeException e ) {
                	AppUtils.Logger(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        
        if( cam == null )
        	cam = Camera.open();

        return cam;
    }
}
