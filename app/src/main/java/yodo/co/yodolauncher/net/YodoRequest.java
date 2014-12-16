package yodo.co.yodolauncher.net;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import yodo.co.yodolauncher.component.Encrypter;
import yodo.co.yodolauncher.data.ServerResponse;
import yodo.co.yodolauncher.helper.AppUtils;

/**
 * Created by luis on 15/12/14.
 */
public class YodoRequest extends ResultReceiver {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

    public interface RESTListener {
        /**
         * Listener for the server responses
         * @param type Type of the request
         * @param response POJO for the response
         */
        public void onResponse(RequestType type, ServerResponse response);
    }

    /** ID for each request */
    public enum RequestType {
        ERROR       ( "00" ), // ERROR
        AUTH_REQUEST( "01" ); // RT=0, ST=4

        private final String name;

        private RequestType(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return ( otherName != null ) && name.equals( otherName );
        }

        public String toString() {
            return name;
        }
    }

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** the external listener to the service */
    private RESTListener externalListener;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    private YodoRequest(Handler handler) {
        super(handler);
        oEncrypter        = new Encrypter();
    }

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static YodoRequest getInstance() {
        if( instance == null )
            instance = new YodoRequest( new Handler() );
        return instance;
    }

    /**
     * Add a listener to the service
     * @param listener
     */
    public void setListener(RESTListener listener) {
        externalListener = listener ;
    }

    /**
     * Remove a listener from the service
     */
    public void removeListener() {
        externalListener = null;
    }

    public void requestAuthentication(Activity activity, String hardwareToken) {
        String sEncryptedUsrData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedUsrData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedUsrData,
                Integer.parseInt( ServerRequest.AUTH_HW_MERCH_SUBREQ )
        );

        Intent intent = new Intent(activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.AUTH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if( resultCode == RESTService.STATUS_FAILED ) {
            externalListener.onResponse( RequestType.ERROR, null );
        } else {
            RequestType action      = (RequestType) resultData.getSerializable( RESTService.ACTION_RESULT );
            ServerResponse response = (ServerResponse) resultData.getSerializable( RESTService.EXTRA_RESULT );
            externalListener.onResponse( action , response );

            AppUtils.Logger(TAG, response.toString());
        }
    }
}
