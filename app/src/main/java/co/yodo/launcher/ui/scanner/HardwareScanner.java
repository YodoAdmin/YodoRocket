package co.yodo.launcher.ui.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.scanner.contract.QRScanner;
import co.yodo.launcher.utils.GuiUtils;

public class HardwareScanner extends QRScanner {
	/** GUI Controllers */
	private AlertDialog inputDialog;

	public HardwareScanner(Activity activity) {
		super(activity);

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final EditText input = new EditText(activity);

		input.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Prevent adding new line
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					return true;
				}

				// Detect final of the scan
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
					inputDialog.dismiss();
					GuiUtils.hideSoftKeyboard(act);

					// Sets the data
					final String scanData = input.getText().toString();
					listener.onScanResult(scanData);

					// Delete the information
					input.setText("");
					return true;
				}

				return false;
			}
		});

		builder.setTitle(activity.getString(R.string.text_scanner));
		builder.setIcon(R.mipmap.ic_launcher);
		builder.setView(input);
		builder.setNegativeButton(activity.getString(R.string.text_cancel), null);

		inputDialog = builder.create();

		Window window = inputDialog.getWindow();
		if (window != null) {
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}

	@Override
	public void setFrontFaceCamera(boolean frontFacing) {}

	@Override
	public void startScan() {
		inputDialog.show();
	}

	@Override
	public void stopScan() {
		inputDialog.dismiss();
	}

	@Override
	public boolean isScanning() {
		return inputDialog.isShowing();
	}

	@Override
	public void destroy() {
		super.destroy();

		// Set to null all the variables
		inputDialog.dismiss();
		inputDialog = null;
	}
}