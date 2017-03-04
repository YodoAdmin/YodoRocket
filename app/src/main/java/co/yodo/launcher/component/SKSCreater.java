package co.yodo.launcher.component;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import co.yodo.restapi.helper.SystemUtils;

/**
 * This class is used for create Yodo SKS by using 
 * ZXing's qr library. 
 * @author Sirinut Thangthumachit (Zui), zui@yodo.mobi
 */
public class SKSCreater {
	/** DEBUG */
	@SuppressWarnings( "unused" )
	private final static String TAG = SKSCreater.class.getSimpleName();

	/** Codes for black and white */
	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	/** QR code separator */
	private static final String QR_SEP = "#";

	/** Max size of the QR code supported by the thermal printer */
	private static final int MAX_SIZE = 255;

	/**
	 * Encodes the user data as a SKS to build a Bitmap
	 * @param code The user encrypted data
	 * @return A bitmap with the SKS code (QR)
	 */
	public static Bitmap createSKS( String header, String code ) {
		Bitmap bitmap = null;

		try {
			final String SKS = header + QR_SEP + code;
			SystemUtils.iLogger( TAG, SKS + " - " + code.length() );
			BitMatrix qrMatrix = new MultiFormatWriter().encode(
					SKS,
					BarcodeFormat.QR_CODE,
					MAX_SIZE, MAX_SIZE,
					null
			);

			int width = qrMatrix.getWidth();
			int height = qrMatrix.getHeight();
			int[] pixels = new int[width * height];

			// All are 0, or black, by default
			for( int y = 0; y < height; y++ ) {
				int offset = y * width;
				for( int x = 0; x < width; x++ )
					pixels[offset + x] = qrMatrix.get( x, y ) ? BLACK : WHITE;
			}
			
			bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
			bitmap.setPixels( pixels, 0, width, 0, 0, width, height );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return bitmap;
	}
}