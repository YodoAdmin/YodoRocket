/**
 * 
 */
package co.yodo.launcher.component;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import co.yodo.launcher.service.RESTService;

/**
 * @author renatomarroquin
 *
 */
public class Encrypter {

	/**
	 * Public key generated with: openssl rsa -in 11.private.pem -pubout -outform DER -out 11.public.der
	 * This key is created using the private key generated using openssl in unix environments
	*/
    private static String PUBLIC_KEY;

    static {
        if( RESTService.getSwitch().equals( "D" ) )
            PUBLIC_KEY = "YodoKey/Dev/12.public.der";
        else
            PUBLIC_KEY = "YodoKey/Prod/12.public.der";
    }
	
	/**
	 * Cipher instance used for encryption
	 */
	private static String CIPHER_INSTANCE = "RSA/ECB/PKCS1Padding";
	
	/**
	 * Public key instance
	 */
	private static String KEY_INSTANCE = "RSA";
	
	/**
	 * Contains string to be encrypted
	 */
	private String sUnEncryptedString;
	
	/**
	 * Contains encrypted data
	 */
	private byte cipherData[];
	
	/**
	 * Function that opens the public key and returns the java object that contains it
	 * @param parent		Parent activity of SKSCreater
	 * @return				The public key specified in $keyFileName
	 */
	static PublicKey readKeyFromFile(Activity parent){
		AssetManager as;
		InputStream inFile;
		byte[] encodedKey;
		PublicKey pkPublic = null;
		
		try {
			as = parent.getResources().getAssets();   
			inFile = as.open(PUBLIC_KEY);
			encodedKey = new byte[inFile.available()];
			inFile.read(encodedKey);
			inFile.close();
			
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory kf = KeyFactory.getInstance(KEY_INSTANCE);
			pkPublic = kf.generatePublic(publicKeySpec);
			    
		} catch(Exception e){
			Log.e(parent.getClass().toString(), "Error Reading Public Key - SKSCreater");
		}
		
		return pkPublic;
	}
	/**
	 * Encrypts a string and returns a byte array containing the encrypted string
	 * @return Byte array containing the encrypted string
	 */
	public void rsaEncrypt(Activity parent) {
		PublicKey pubKey = readKeyFromFile(parent);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_INSTANCE);
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			this.cipherData = cipher.doFinal(this.sUnEncryptedString.getBytes());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			Log.e(parent.getClass().toString() , "Error Encrypting string - SKSCreater");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * Receives an encrypted byte array and returns a string of
	 * hexadecimal numbers that represents it
	 * @return String of hexadecimal number
	 */
	public String bytesToHex(){
		StringBuffer hexCrypt = new StringBuffer();
		for(int i = 0; i < this.cipherData.length; i++ ) {
				int int_value = (int)this.cipherData[i];
				
				if(int_value < 0)
					int_value = int_value + 256;
				String hexNum = Integer.toHexString(int_value);
				if(hexNum.length() == 1) {
					hexCrypt.append("0"+ hexNum);
				} else {
					hexCrypt.append(hexNum);
				}
		}
		return hexCrypt.toString();
	}
	
	/**
	 * @return the sUnEncryptedString
	 */
	public String getsUnEncryptedString() {
		return sUnEncryptedString;
	}

	/**
	 * @param sUnEncryptedString the sUnEncryptedString to set
	 */
	public void setsUnEncryptedString(String sUnEncryptedString) {
		this.sUnEncryptedString = sUnEncryptedString;
	}

	/**
	 * @return the sEncryptedString
	 */
	public String getsEncryptedString() {
		return new String(this.cipherData);
	}

	public void setCipherForHex() {
		this.cipherData = this.sUnEncryptedString.getBytes();
	}
}
