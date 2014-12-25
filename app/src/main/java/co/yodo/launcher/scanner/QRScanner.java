package co.yodo.launcher.scanner;

import android.app.Activity;

public abstract class QRScanner {
	/** Activity and Listener */
	protected Activity act;
	protected QRScannerListener listener;
	
	/** Data retrieved */
    protected Integer resultLength;
    protected String resultData;
	
	protected QRScanner(Activity activity) {
		this.act = activity;
	}
	
	public abstract void startScan();
	
	public abstract boolean isScanning();
	
	public abstract void destroy();

	public String getResultData() {
		return this.resultData;
	}

	public Integer getResultLength() {
		return this.resultLength;
	}
	
	public void setListener(QRScannerListener listener) {
		this.listener = listener;
	}
}
