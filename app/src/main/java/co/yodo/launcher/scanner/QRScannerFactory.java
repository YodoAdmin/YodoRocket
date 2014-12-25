package co.yodo.launcher.scanner;

import android.app.Activity;

public class QRScannerFactory {
	public enum SupportedScanners {
		Hardware   ( "Barcode Scanner" ),
		ZBarScanner( "Camera" );
		
		private String value;
		
		SupportedScanners(String value) {
			this.value = value;
		}
		
		@Override 
		public String toString() {
		    return value;
		}
	}
	
	public static QRScanner getInstance(Activity activity, SupportedScanners scanner) {
		QRScanner qrscanner = null;

		switch( scanner ) {
			case Hardware:
				qrscanner = HardwareScanner.getInstance( activity );
			break;
			
			case ZBarScanner:
				qrscanner = ZBarScanner.getInstance( activity );
			break;
		}
		return qrscanner;
	}
	
	public static void destroy() {
        QRScanner qrscanner = HardwareScanner.getInstance();
		
		if( qrscanner != null )
			qrscanner.destroy();
		
		qrscanner = ZBarScanner.getInstance();
		
		if( qrscanner != null )
			qrscanner.destroy();
	}
}
