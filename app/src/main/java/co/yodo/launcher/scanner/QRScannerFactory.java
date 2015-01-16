package co.yodo.launcher.scanner;

import android.app.Activity;

public class QRScannerFactory {
	public enum SupportedScanners {
		Hardware   ( "Barcode Scanner" ),
		ZBarScanner1( "Camera Front" ),
        ZBarScanner2( "Camera Back" );
		
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
			
			case ZBarScanner1:
				qrscanner = ZBarScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( true );
			break;

            case ZBarScanner2:
                qrscanner = ZBarScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( false );
            break;
		}

        if( qrscanner != null && activity instanceof QRScannerListener )
            qrscanner.setListener( (QRScannerListener) activity );

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
