package co.yodo.launcher.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import co.yodo.launcher.network.model.ServerResponse;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.network.handler.XMLHandler;

/**
 * Created by luis on 15/12/14.
 * Service that makes the requests to the server
 */
public class RESTService extends IntentService {
    /** DEBUG */
    private static final String TAG = RESTService.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Switch server IP address */
    //private static final String IP 	         = "http://50.56.180.133";  // Production
    private static final String IP 			 = "http://198.101.209.120";  // Development
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** It is the ID of the application (the package) used for the extras */
    private static final String PARAMS_APPID = "yodo.co.yodolauncher.net.";

    /** Params keys for the data */
    public static final String EXTRA_PARAMS          = PARAMS_APPID + "EXTRA_PARAMS";
    public static final String EXTRA_RESULT_RECEIVER = PARAMS_APPID + "EXTRA_RESULT_RECEIVER";

    /** Param keys for the result */
    public static final String ACTION_RESULT = PARAMS_APPID + "ACTION_RESULT";
    public static final String EXTRA_RESULT  = PARAMS_APPID + "EXTRA_RESULT";

    /** Status code */
    public static final int STATUS_NO_INTERNET = -1;
    public static final int STATUS_FAILED      = 0;
    public static final int STATUS_SUCCESS     = 1;

    /** Timeout */
    private final static int TIMEOUT = 10000;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RESTService() {
        super( TAG );
        // get the context
        ac = RESTService.this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if( extras == null || !extras.containsKey( EXTRA_RESULT_RECEIVER ) ) {
            AppUtils.Logger( TAG, "You did not pass extras or data with the Intent." );
            return;
        }

        Serializable action     = extras.getSerializable( ACTION_RESULT );
        String pRequest         = extras.getString( EXTRA_PARAMS );
        ResultReceiver receiver = extras.getParcelable( EXTRA_RESULT_RECEIVER );

        if( receiver == null ) {
            AppUtils.Logger( TAG, "You did not pass the receiver with the Intent." );
            return;
        }

        try {
            // Handling XML
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            // Send URL to parse XML Tags
            URLConnection sourceUrl = new URL( IP + YODO_ADDRESS + pRequest ).openConnection();
            sourceUrl.setConnectTimeout( TIMEOUT );
            sourceUrl.setReadTimeout( TIMEOUT );

            // Create handler to handle XML Tags ( extends DefaultHandler )
            xr.setContentHandler( new XMLHandler() );
            xr.parse( new InputSource( sourceUrl.getInputStream() ) );
        } catch( SocketTimeoutException | SocketException e) {
            AppUtils.Logger( TAG, "Timeout Exception = " + e );
            receiver.send( STATUS_NO_INTERNET, null );
            return;
        } catch( Exception e ) {
            AppUtils.Logger( TAG, "XML Parsing Exception = " + e );
            receiver.send( STATUS_FAILED, null );
            return;
        }

        ServerResponse response = XMLHandler.response;

        AppUtils.Logger( TAG, response.toString() );

        Bundle resultData = new Bundle();
        resultData.putSerializable( ACTION_RESULT, action );
        resultData.putSerializable( EXTRA_RESULT, response );
        receiver.send( STATUS_SUCCESS, resultData );
    }

    public static String getRoot() {
        return IP;
    }

    public static String getSwitch() {
        if( IP.equals( "http://50.56.180.133" ) )
            return "P";
        return "D";
    }
}
