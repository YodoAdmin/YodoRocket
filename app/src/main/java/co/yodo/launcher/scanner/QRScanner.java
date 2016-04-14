package co.yodo.launcher.scanner;

import android.app.Activity;

/**
 * Defines the base structure for any
 * scanner implementation and its listener
 */
public abstract class QRScanner {
	public interface QRScannerListener {
		/**
		 * Listener for the data of the scanner
		 * @param data String data received
		 */
		void onNewData( String data );
	}

	/** Activity (GUI) and Listener (data) */
	protected Activity act;
	protected QRScannerListener listener;

	/**
	 * Constructor that uses the activity for any
	 * initialization
	 * @param activity The activity that provides the views
	 */
	protected QRScanner( Activity activity ) {
		this.act = activity;
	}

	/**
	 * Starts the scan with the preview of the camera
	 */
	public abstract void startScan();

	/**
	 * Stops the scan and closes the preview
	 */
	public abstract void stopScan();

	/**
	 * Returns the current state of the scanner
	 * @return true if it is scanning
	 * 		   false if it is not scanning
	 */
	public abstract boolean isScanning();

	/**
	 * Sets the camera that is going to be used
	 * @param frontFacing true for front, false for back
	 */
	public abstract void setFrontFaceCamera( boolean frontFacing );

	/**
	 * Sets the listener for the scan results
	 * @param listener The class that will receive the updates
	 */
	public void setListener( QRScannerListener listener ) {
		this.listener = listener;
	}

	/**
	 * It should release all the resources that
	 * the scanner is using
	 */
	public void destroy() {
		act = null;
		listener = null;
	}
}