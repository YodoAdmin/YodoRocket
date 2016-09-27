package co.yodo.launcher.component;

import android.annotation.SuppressLint;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {	 
	 /** Public key instance */
	 private static String KEY_INSTANCE = "AES";

    /** Encrypt Key AES */
    public static final String seed = "FEDCBA98765432100123456789ABCDEF";
	 
	 public static String encrypt(String cleartext) throws Exception {
		 byte[] rawKey = getRawKey( seed.getBytes() );
		 byte[] result = encrypt( rawKey, cleartext.getBytes() );
		 return bytesToHex( result );
	 }
    
	 public static String decrypt( String encrypted ) throws Exception {
		 byte[] rawKey = getRawKey( seed.getBytes() );
		 byte[] enc = hexToBytes( encrypted );
		 byte[] result = decrypt( rawKey, enc );
		 return new String( result );
	 }

	 private static byte[] getRawKey( byte[] seed ) throws Exception {
		 MessageDigest md = MessageDigest.getInstance( "MD5" );
		 byte[] md5Bytes = md.digest( seed ); // 128 Bit = 16 byte SecretKey
		 SecretKeySpec skey = new SecretKeySpec( md5Bytes, "AES" );
         return skey.getEncoded();
	 }

	 @SuppressLint("TrulyRandom")
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		 SecretKeySpec skeySpec = new SecretKeySpec( raw, KEY_INSTANCE );
		 Cipher cipher = Cipher.getInstance( KEY_INSTANCE );
		 cipher.init( Cipher.ENCRYPT_MODE, skeySpec );
         return cipher.doFinal( clear );
	 }

	 private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		 SecretKeySpec skeySpec = new SecretKeySpec( raw, KEY_INSTANCE );
		 Cipher cipher = Cipher.getInstance( KEY_INSTANCE );
		 cipher.init( Cipher.DECRYPT_MODE, skeySpec );
         return cipher.doFinal( encrypted );
	 }
	 
	 public static String bytesToHex(byte[] data) {
		 if( data == null )
			 return null;

		 String str = "";
         for( byte aData : data ) {
             if ( ( aData & 0xFF ) < 16 )
                 str = str + "0" + Integer.toHexString( aData & 0xFF );
             else
                 str = str + Integer.toHexString( aData & 0xFF );
         }
		 return str;
	 }
	 
	 public static byte[] hexToBytes(String str) {
		 if( str == null ) {
			 return null;
		 } else if( str.length() < 2 ) {
			 return null;
		 } else {
			 int len = str.length() / 2;
			 byte[] buffer = new byte[ len ];
			 for( int i = 0; i < len; i++ ) {
				 buffer[ i ] = (byte) Integer.parseInt( str.substring( i * 2, i * 2 + 2 ), 16 );
			 }
			 return buffer;
		 }
	 }
}
