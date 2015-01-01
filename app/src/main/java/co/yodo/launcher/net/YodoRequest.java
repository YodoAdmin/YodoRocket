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
import co.yodo.launcher.component.TransparentProgressDialog;
import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.service.RESTService;

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
        QUERY_DAY_REQUEST ( "03" ), // RT=5, ST=3
        QUERY_LOGO_REQUEST( "04" ), // RT=5, ST=3
        REG_MERCH_REQUEST ( "05 "), // RT=9, ST=1
        EXCH_MERCH_REQUEST( "06" ),	// RT=1, ST=1
        ALT_MERCH_REQUEST ( "07" );	// RT=7

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

    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Progress dialog */
    private ProgressDialog progressDialog;
    private TransparentProgressDialog transProgressDialog;

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
        super( handler );
        oEncrypter = new Encrypter();
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

    public void createProgressDialog(Activity activity, ProgressDialogType type) {
        switch( type ) {
            case NORMAL:
                progressDialog = new ProgressDialog( activity );
                progressDialog.setCancelable( false );
                progressDialog.show();
                progressDialog.setContentView( R.layout.custom_progressdialog );
                break;

            case TRANSPARENT:
                transProgressDialog = new TransparentProgressDialog( activity, R.drawable.spinner );
                transProgressDialog.show();
                break;
        }
    }

    public void destroyProgressDialog() {
        if( progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if( transProgressDialog != null ) {
            transProgressDialog.dismiss();
            transProgressDialog = null;
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

    public void requestDailyHistory(Activity activity, String hardwareToken, String pip) {
        String sEncryptedMerchData, pRequest;
        StringBuilder sBalanceData = new StringBuilder();

        sBalanceData.append( hardwareToken ).append( REQ_SEP );
        sBalanceData.append( pip ).append( REQ_SEP );
        sBalanceData.append( ServerRequest.QUERY_TODAY_BALANCE );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sBalanceData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_DAY_REQUEST );
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

    public void requestRegistration(Activity activity, String hardwareToken, String token) {
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
                Integer.parseInt( ServerRequest.REG_MERCH_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestExchange(Activity activity, String hardwareToken, String client,
                                String totalPurchase, String cashTender, String cashBack,
                                double latitude, double longitude, String currency) {
        String sEncryptedMerchData, sEncryptedExchangeUsrData, pRequest;
        StringBuilder sEncryptedUsrData = new StringBuilder();
        StringBuilder sExchangeUsrData = new StringBuilder();

        /// Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        sEncryptedUsrData.append( sEncryptedMerchData ).append( REQ_SEP );
        sEncryptedUsrData.append( client ).append( REQ_SEP );

        sExchangeUsrData.append( latitude ).append( REQ_SEP );
        sExchangeUsrData.append( longitude ).append(REQ_SEP);
        sExchangeUsrData.append( totalPurchase ).append(REQ_SEP);
        sExchangeUsrData.append( cashTender ).append( REQ_SEP );
        sExchangeUsrData.append( cashBack ).append(REQ_SEP);
        sExchangeUsrData.append( currency );

        oEncrypter.setsUnEncryptedString( sExchangeUsrData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedExchangeUsrData = oEncrypter.bytesToHex();
        sEncryptedUsrData.append( sEncryptedExchangeUsrData );

        pRequest = ServerRequest.createExchangeRequest(
                sEncryptedUsrData.toString(),
                Integer.parseInt(ServerRequest.REG_MERCH_SUBREQ)
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.ALT_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestAlternate(Activity activity, String hardwareToken, String client,
                                String totalPurchase, String cashTender, String cashBack,
                                double latitude, double longitude, String currency) {
        String sEncryptedMerchData, sEncryptedExchangeUsrData, pRequest;
        StringBuilder sEncryptedUsrData = new StringBuilder();
        StringBuilder sExchangeUsrData = new StringBuilder();

        // Get the client data and account type
        String clientData  = client.substring( 0, client.length() - 1 );
        String accountType = client.substring( client.length() - 1 );

        if( !AppUtils.isNumber( accountType ) ) {
            instance.send( RESTService.STATUS_FAILED, null );
            return;
        }

        /// Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        sEncryptedUsrData.append( sEncryptedMerchData ).append( REQ_SEP );
        sEncryptedUsrData.append( clientData ).append( REQ_SEP );

        sExchangeUsrData.append( latitude ).append( REQ_SEP );
        sExchangeUsrData.append( longitude ).append(REQ_SEP);
        sExchangeUsrData.append( totalPurchase ).append(REQ_SEP);
        sExchangeUsrData.append( cashTender ).append( REQ_SEP );
        sExchangeUsrData.append( cashBack ).append(REQ_SEP);
        sExchangeUsrData.append( currency );

        oEncrypter.setsUnEncryptedString( sExchangeUsrData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedExchangeUsrData = oEncrypter.bytesToHex();
        sEncryptedUsrData.append( sEncryptedExchangeUsrData );

        pRequest = ServerRequest.createAlternateRequest(
                sEncryptedUsrData.toString(),
                Integer.parseInt( accountType )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.EXCH_MERCH_REQUEST );
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
