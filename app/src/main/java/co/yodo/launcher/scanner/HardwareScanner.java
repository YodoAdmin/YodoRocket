package co.yodo.launcher.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppUtils;

public class HardwareScanner extends QRScanner {
	/** DEBUG */
	private static final String TAG = HardwareScanner.class.getName();
	
	/** GUI Controllers */
	private AlertDialog alertDialog;
	
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
					String data = input.getText().toString();
					
					if( data != null ) {
						alertDialog.dismiss();

						AppUtils.Logger(TAG, data);
                        AppUtils.hideSoftKeyboard( act );

                        if( listener != null )
                            listener.onNewData( data );
					}
					return true;
	            }
				return false;
			}
	    });
		
		builder.setTitle( activity.getString( R.string.barcode_scanner ) );
		builder.setView( input );
		builder.setNegativeButton( activity.getString(R.string.cancel), null);
		
		alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		alertDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
		        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
        });
	}
	
	public static HardwareScanner getInstance(Activity activity) {
		synchronized(HardwareScanner.class) {
			if(instance == null)
				instance = new HardwareScanner(activity);
		}
		return instance;
	}
	
	public static HardwareScanner getInstance() {
		return instance;
	}
	
	@Override
	public void startScan() {
		alertDialog.show();
	}
	
	@Override
	public boolean isScanning() {
		return false;
	}

	@Override
	public void destroy() {
		instance = null;
	}
}
