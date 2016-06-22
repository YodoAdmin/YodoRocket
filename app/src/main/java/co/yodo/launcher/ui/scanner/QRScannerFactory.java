package co.yodo.launcher.ui.scanner;

import android.app.Activity;

public class QRScannerFactory {
	public enum SupportedScanners {
		Hardware   ( "Barcode Scanner" ),
		CameraFront( "Camera Front" ),
		CameraBack ( "Camera Back" );

		private String value;
		public static final long length = values().length;

		SupportedScanners( String value ) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	/** QR Scanners */
	private final HardwareScanner hardwareScanner;
	private final ScanditScanner softwareScanner;

	public QRScannerFactory( Activity activity ) throws ClassCastException{
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
	public QRScanner getScanner( SupportedScanners scanner ) {
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