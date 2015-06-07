package co.yodo.launcher.scanner.ZBarUtils;

import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.helper.DisplayUtils;

import android.graphics.Point;
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

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	/** DEBUG */
	private static final String TAG = CameraPreview.class.getName();

    private int mCameraId;
    private Camera mCamera;
	private SurfaceHolder mHolder;
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

	public CameraPreview(Context context, Camera camera, int cameraId, PreviewCallback previewCb) {
		super( context );
        mCameraId = cameraId;
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

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if( mHolder.getSurface() == null ) {
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        stopCameraPreview();
        showCameraPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if( mCamera != null ) {
            try {
                mPreviewing = true;
                setupCameraParameters();

                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo( mCameraId, info );

                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                int rotation = windowManager.getDefaultDisplay().getRotation();

                int degrees = 0;

                switch( rotation ) {
                    case Surface.ROTATION_0: degrees = 0; break;
                    case Surface.ROTATION_90: degrees = 90; break;
                    case Surface.ROTATION_180: degrees = 180; break;
                    case Surface.ROTATION_270: degrees = 270; break;
                }

                int result;
                if( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
                    result = ( info.orientation + degrees ) % 360;
                    result = ( 360 - result ) % 360;  // compensate the mirror
                } else {  // back-facing
                    result = ( info.orientation - degrees + 360 ) % 360;
                }
                mCamera.setDisplayOrientation( result );

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

    public void setupCameraParameters() {
        Camera.Size optimalSize = getOptimalPreviewSize();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize( optimalSize.width, optimalSize.height );
        mCamera.setParameters( parameters );
    }

    private Camera.Size getOptimalPreviewSize() {
        if( mCamera == null ) {
            return null;
        }

        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        Point screenResolution = DisplayUtils.getScreenResolution( getContext() );
        int w = screenResolution.x;
        int h = screenResolution.y;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        // Try to find an size match aspect ratio and size
        for( Camera.Size size : sizes ) {
            double ratio = (double) size.width / size.height;
            if( Math.abs( ratio - targetRatio ) > ASPECT_TOLERANCE ) continue;
            if( Math.abs(size.height - h ) < minDiff ) {
                optimalSize = size;
                minDiff = Math.abs( size.height - h );
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if( optimalSize == null ) {
            minDiff = Double.MAX_VALUE;
            for( Camera.Size size : sizes ) {
                if( Math.abs( size.height - h ) < minDiff ) {
                    optimalSize = size;
                    minDiff = Math.abs( size.height - h );
                }
            }
        }
        return optimalSize;
    }

    public void setAutoFocus(boolean state) {
        if( mCamera != null && mPreviewing ) {
            if( state == mAutoFocus )
                return;
            mAutoFocus = state;
            if( mAutoFocus ) {
                mCamera.autoFocus( autoFocusCB );
            } else {
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
