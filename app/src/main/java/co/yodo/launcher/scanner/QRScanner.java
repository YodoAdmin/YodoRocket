package co.yodo.launcher.scanner;

import android.app.Activity;

public abstract class QRScanner {
	/** Activity and Listener */
	protected Activity act;
	protected QRScannerListener listener;
	
	protected QRScanner(Activity activity) {
		this.act = activity;
	}
	
	public abstract void startScan();

    public abstract void close();
	
	public abstract boolean isScanning();
	
	public abstract void destroy();
	
	public void setListener(QRScannerListener listener) {
		this.listener = listener;
	}
}
