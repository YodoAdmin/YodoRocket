package co.yodo.launcher.ui.scanner;

import android.app.Activity;

import co.yodo.launcher.R;

public class QRScannerFactory {
	/** SKS Sizes */
	public static final int SKS_SIZE = 256;
	public static final int ALT_SIZE = 257;

	public enum SupportedScanner {
		Hardware    ( R.string.name_scanner_hardware ),
		CameraFront ( R.string.name_scanner_software_front ),
		CameraBack  ( R.string.name_scanner_software_back );

		private int value;
		public static final long length = values().length;

		SupportedScanner( int value ) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/** QR Scanners */
	private final HardwareScanner hardwareScanner;
	private final ScanditScanner softwareScanner;

	public QRScannerFactory( Activity activity ) throws ClassCastException {
		if( activity instanceof QRScanner.QRScannerListener ) {
			hardwareScanner = new HardwareScanner( activity );
			softwareScanner = new ScanditScanner( activity );
			hardwareScanner.setListener( (QRScanner.QRScannerListener) activity );
			softwareScanner.setListener( (QRScanner.QRScannerListener) activity );
		} else {
			throw new ClassCastException( activity.getLocalClassName() + " does not implement the QRScannerListener" );
		}
	}

	/**
	 * Gets the requested scanner
	 * @param scanner Could be hardware, front and back software
	 * @return The QR scanner
	 */
	public QRScanner getScanner( SupportedScanner scanner ) {
		switch( scanner ) {
			case CameraFront:
				softwareScanner.setFrontFaceCamera( true );
				return softwareScanner;

			case CameraBack:
				softwareScanner.setFrontFaceCamera( false );
				return softwareScanner;

			default:
				return hardwareScanner;
		}
	}

	/**
	 * Release the resources from both scanners
	 */
	public void destroy() {
		hardwareScanner.destroy();
		softwareScanner.destroy();
	}
}