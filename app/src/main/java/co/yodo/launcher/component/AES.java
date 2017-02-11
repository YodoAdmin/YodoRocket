package co.yodo.launcher.component;

import android.annotation.SuppressLint;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import co.yodo.restapi.helper.CryptUtils;

public class AES {	 
	/** Key instance */
	private static final String KEY_INSTANCE = "AES";
	private static final int KEY_SIZE = 128;

	/** Cipher instance used for encryption */
	private static final String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";

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
	 
	 private static String bytesToHex( byte[] data ) {
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
	 
	 private static byte[] hexToBytes( String str ) {
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

	/**
	 * Decrypts a hex encrypted string
	 * @param hexText The encrypted string in hex
	 * @param key       The key used to encrypt the data
	 * @return String   The original message
	 */
	public static String decrypt( String hexText, SecretKeySpec key ) {
		final byte[] encryptedData = hexToBytes( hexText );
		String unencryptedData = null;

		try {
			Cipher cipher = Cipher.getInstance( CIPHER_INSTANCE );
			cipher.init( Cipher.DECRYPT_MODE, key, generateIV( key ) );
			unencryptedData = new String( cipher.doFinal( encryptedData ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return unencryptedData;
	}

	/**
	 * Generates the IV (salt) from the key
	 * @param key              The key used to encrypt
	 * @return IvParameterSpec The IV (salt)
	 */
	private static IvParameterSpec generateIV( SecretKeySpec key ) {
		return new IvParameterSpec( key.getEncoded() );
	}
}
