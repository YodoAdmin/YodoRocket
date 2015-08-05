package co.yodo.launcher.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppUtils;

public class HardwareScanner extends QRScanner {
	/** DEBUG */
	private static final String TAG = HardwareScanner.class.getName();
	
	/** GUI Controllers */
	private AlertDialog inputDialog;
	
	/** Instance */
	private static volatile HardwareScanner instance = null;

	private HardwareScanner(Activity activity) {
		super( activity );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( activity );
		final EditText input        = new EditText( activity );
		
		input.setOnKeyListener( new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Prevent adding new line
	            if( event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER )
	            	return true;
	            
				if( event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER ) {
					String scanData = input.getText().toString();
					
					if( scanData != null ) {
                        inputDialog.dismiss();

						AppUtils.Logger(TAG, scanData);
                        AppUtils.hideSoftKeyboard( act );

                        if( listener != null )
                            listener.onNewData( scanData );

                        input.setText( "" );
					}
					return true;
	            }
				return false;
			}
        	    });
		
		builder.setTitle( activity.getString( R.string.barcode_scanner ) );
        builder.setIcon( R.drawable.ic_launcher );
		builder.setView( input );
		builder.setNegativeButton( activity.getString(R.string.cancel), null );

        inputDialog = builder.create();
        inputDialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
	}
	
	public static HardwareScanner getInstance( Activity activity ) {
		synchronized( HardwareScanner.class ) {
			if(instance == null)
				instance = new HardwareScanner( activity );
		}
		return instance;
	}
	
	public static void deleteInstance() {
		if( instance != null )
            instance.destroy();
	}
	
	@Override
	public void startScan() {
        inputDialog.show();
	}

    @Override
    public void close() {
        inputDialog.dismiss();
    }
	
	@Override
	public boolean isScanning() {
		return inputDialog.isShowing();
	}

    @Override
    public void setFrontFaceCamera( boolean frontFacing ) {}

    @Override
	public void destroy() {
        inputDialog.dismiss();
        inputDialog = null;
		instance    = null;
	}
}
