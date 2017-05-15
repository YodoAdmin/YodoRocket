package co.yodo.launcher.business.component;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Window;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

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

	/**
	 * Encodes the user data as a SKS to build a Bitmap
	 * @param parent The activity that is creating the SKS
	 * @param code The user encrypted data
	 * @return A bitmap with the SKS code (QR)
	 */
	public static Bitmap createSKS( Activity parent, String header, String code ) {
		Bitmap bitmap = null;

		try {
			//SystemUtils.iLogger( TAG, header + QR_SEP + code + " - " + code.length() );

			Integer QR_SIZE = getSKSSize( parent );
			BitMatrix qrMatrix = new MultiFormatWriter().encode(
					header + QR_SEP + code,
					BarcodeFormat.QR_CODE,
					QR_SIZE, QR_SIZE,
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

	/**
	 * Gets the SKS size for the screen
	 * @param activity The Context of the Android system (as activity)
	 * @return int The size
	 */
	private static int getSKSSize( Activity activity ) {
		int screenLayout = activity.getResources().getConfiguration().screenLayout;
		screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

		Rect displayRectangle = new Rect();
		Window window = activity.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame( displayRectangle );
		int size, currentOrientation = activity.getResources().getConfiguration().orientation;

		if( currentOrientation == Configuration.ORIENTATION_LANDSCAPE )
			size = displayRectangle.height();
		else
			size = displayRectangle.width();

		switch( screenLayout ) {
			case Configuration.SCREENLAYOUT_SIZE_SMALL:
				return (int)( size * 0.7f );

			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
				return (int)( size * 0.7f );

			case Configuration.SCREENLAYOUT_SIZE_LARGE:
				return (int)( size * 0.4f );

			case Configuration.SCREENLAYOUT_SIZE_XLARGE:
				return (int)( size * 0.3f );

			default:
				return 300;
		}
	}
}