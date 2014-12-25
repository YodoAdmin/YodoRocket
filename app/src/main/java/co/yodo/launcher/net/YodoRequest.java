package co.yodo.launcher.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import java.text.SimpleDateFormat;
import java.util.Locale;

import co.yodo.launcher.R;
import co.yodo.launcher.component.Encrypter;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AppUtils;

/**
 * Created by luis on 15/12/14.
 * Request to the Yodo Server
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
        ERROR_NO_INTERNET ( "-1" ), // ERROR NO INTERNET
        ERROR_GENERAL     ( "00" ), // ERROR GENERAL
        AUTH_REQUEST      ( "01" ), // RT=0, ST=4
        QUERY_BAL_REQUEST ( "02" ), // RT=5, ST=3
        QUERY_LOGO_REQUEST( "03" ), // RT=5, ST=3
        REG_MERCH_REQUEST ( "04 "); // RT=9, ST=1

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

    /** Progress dialog */
    private ProgressDialog progressDialog;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** the external listener to the service */
    private RESTListener externalListener;

    /** User's data separator */
    private static final String	REQ_SEP = ",";

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler Default
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
     * @param listener Listener for the requests to the server
     */
    public void setListener(RESTListener listener) {
        externalListener = listener ;
    }

    public void createProgressDialog(Activity activity) {
        progressDialog = new ProgressDialog( activity );
        progressDialog.setCancelable( false );
        progressDialog.show();
        progressDialog.setContentView( R.layout.custom_progressdialog );
    }

    public void destroyProgressDialog() {
        if( progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void requestAuthentication(Activity activity, String hardwareToken) {
        String sEncryptedMerchData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.AUTH_HW_MERCH_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.AUTH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestHistory(Activity activity, String hardwareToken, String pip) {
        String sEncryptedMerchData, pRequest;
        StringBuilder sBalanceData = new StringBuilder();

        sBalanceData.append( hardwareToken ).append( REQ_SEP );
        sBalanceData.append( pip ).append( REQ_SEP );
        sBalanceData.append( ServerRequest.QUERY_HISTORY_BALANCE );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sBalanceData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BAL_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestLogo(Activity activity, String hardwareToken) {
        String sEncryptedMerchData, pRequest;
        StringBuilder sMerchantLogoData = new StringBuilder();

        sMerchantLogoData.append( hardwareToken ).append(REQ_SEP);
        sMerchantLogoData.append( ServerRequest.QUERY_MERCHANT_LOGO );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(sMerchantLogoData.toString());
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_LOGO_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestMerchantRegistration(Activity activity, String hardwareToken, String token) {
        String sEncryptedMerchData, pRequest;
        StringBuilder sMerchData = new StringBuilder();

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ssZZZZ", Locale.US );
        String timeStamp = dateFormat.format( System.currentTimeMillis() );
        
        sMerchData.append( hardwareToken ).append(REQ_SEP);
        sMerchData.append( token ).append(REQ_SEP);
        sMerchData.append( timeStamp );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sMerchData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedMerchData,
                Integer.parseInt(ServerRequest.REG_MERCH_SUBREQ)
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if( resultCode == RESTService.STATUS_FAILED ) {
            externalListener.onResponse(RequestType.ERROR_GENERAL, null);
        }
        else if( resultCode == RESTService.STATUS_NO_INTERNET ) {
            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
        }
        else {
            RequestType action      = (RequestType) resultData.getSerializable( RESTService.ACTION_RESULT );
            ServerResponse response = (ServerResponse) resultData.getSerializable( RESTService.EXTRA_RESULT );
            externalListener.onResponse( action , response );

            AppUtils.Logger( TAG, response.toString() );
        }
    }
}
