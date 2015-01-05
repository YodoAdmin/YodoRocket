package co.yodo.launcher.scanner.ZBarUtils;

import co.yodo.launcher.component.ToastMaster;
import co.yodo.launcher.helper.AppUtils;

import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	/** DEBUG */
	private static final String TAG = CameraPreview.class.getName();
		
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;

    private Handler mAutoFocusHandler;
    private boolean mPreviewing = true;
    private boolean mAutoFocus = true;
    private boolean mSurfaceCreated = false;

    public CameraPreview(Context context) {
        super( context );
    }
    public CameraPreview(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

	public CameraPreview(Context context, Camera camera, PreviewCallback previewCb) {
		super( context );
		mCamera = camera;
		previewCallback = previewCb;
        mAutoFocusHandler = new Handler();

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback( this );
	}
    
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
        mSurfaceCreated = false;
        stopCameraPreview();
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if( mHolder.getSurface() == null ) {
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        stopCameraPreview();
        showCameraPreview();
    }

    public void showCameraPreview() {
        if( mCamera != null ) {
            try {
                mPreviewing = true;

                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                int rotation = windowManager.getDefaultDisplay().getRotation();

                switch( rotation ) {
                    case Surface.ROTATION_180:
                        mCamera.setDisplayOrientation( 180 );
                        break;
                }

                mCamera.setPreviewDisplay( mHolder );
                mCamera.setPreviewCallback( previewCallback );
                mCamera.startPreview();

                if( mAutoFocus ) {
                    if( mSurfaceCreated ) { // check if surface created before using autofocus
                        mCamera.autoFocus( autoFocusCB );
                    } else {
                        scheduleAutoFocus(); // wait 1 sec and then do check again
                    }
                }
            } catch( Exception e ) {
                AppUtils.Logger( TAG, e.getMessage() );
            }
        }
    }
    public void stopCameraPreview() {
        if( mCamera != null ) {
            try {
                mPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();
            } catch( Exception e ) {
                AppUtils.Logger( TAG, e.toString() );
            }
        }
    }

    public void setAutoFocus(boolean state) {
        if( mCamera != null && mPreviewing ) {
            if( state == mAutoFocus ) {
                return;
            }

            mAutoFocus = state;
            if( mAutoFocus ) {
                if (mSurfaceCreated) { // check if surface created before using autofocus
                    AppUtils.Logger( TAG, "Starting autofocus" );
                    mCamera.autoFocus( autoFocusCB );
                } else {
                    scheduleAutoFocus(); // wait 1 sec and then do check again
                }
            } else {
                AppUtils.Logger( TAG, "Cancelling autofocus" );
                mCamera.cancelAutoFocus();
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        @Override
        public void run() {
        if( mCamera != null && mPreviewing && mAutoFocus && mSurfaceCreated )
            mCamera.autoFocus( autoFocusCB );
        }
    };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            scheduleAutoFocus();
        }
    };

    private void scheduleAutoFocus() {
        mAutoFocusHandler.postDelayed( doAutoFocus, 1000 );
    }
}
