package co.yodo.launcher.scanner;

import android.app.Activity;

public class QRScannerFactory {
	public enum SupportedScanners {
		Hardware   ( "Barcode Scanner" ),
		CameraFront( "Camera Front" ),
        CameraBack ( "Camera Back" );
		
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
			
			case CameraFront:
				//qrscanner = ZBarScanner.getInstance( activity );
                //qrscanner = ZxingScanner.getInstance( activity );
				qrscanner = ScanditScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( true );
			break;

            case CameraBack:
                //qrscanner = ZBarScanner.getInstance( activity );
                //qrscanner = ZxingScanner.getInstance( activity );
				qrscanner = ScanditScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( false );
            break;
		}

        if( qrscanner != null && activity instanceof QRScannerListener )
            qrscanner.setListener( (QRScannerListener) activity );

		return qrscanner;
	}
	
	public static void destroy() {
        HardwareScanner.deleteInstance();
		//ZxingScanner.deleteInstance();
		ScanditScanner.deleteInstance();
	}
}
