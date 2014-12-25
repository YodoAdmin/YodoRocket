package co.yodo.launcher.scanner.ZBarUtils;

import java.io.IOException;

import co.yodo.launcher.helper.AppUtils;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	/** DEBUG */
	private static final String TAG = CameraPreview.class.getName();
		
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;

	public CameraPreview(Context context, Camera camera, PreviewCallback previewCb, AutoFocusCallback autoFocusCb) {
		super( context );
		mCamera = camera;
		previewCallback = previewCb;
		autoFocusCallback = autoFocusCb;
		
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback( this );
		
		// deprecated setting, but required on Android versions prior to 3.0
		//mHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
	}
    
    public void surfaceCreated(SurfaceHolder holder) {
    	//mCamera.autoFocus(autoFocusCallback);
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay( holder );
        } catch(IOException e) {
        	AppUtils.Logger( TAG, "Error setting camera preview: " + e.getMessage() );
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or reformatting it.
         */
        if( mHolder.getSurface() == null ) {
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch( Exception e ){
          // ignore: tried to stop a non-existent preview
        }

        try {
            // Hard code camera surface rotation 90 degs to match Activity view in portrait
            //mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay( mHolder );
            mCamera.setPreviewCallback( previewCallback );
            mCamera.startPreview();
            mCamera.autoFocus( autoFocusCallback );
        } catch(Exception e) {
        	AppUtils.Logger( TAG, "Error starting camera preview: " + e.getMessage() );
        }
    }
}
