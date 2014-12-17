package yodo.co.yodolauncher.net;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import yodo.co.yodolauncher.data.ServerResponse;
import yodo.co.yodolauncher.helper.AppUtils;

/**
 * Created by luis on 15/12/14.
 */
public class RESTService extends IntentService {
    /** DEBUG */
    private static final String TAG = RESTService.class.getSimpleName();

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
    public static final int STATUS_FAILED  = 0;
    public static final int STATUS_SUCCESS = 1;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RESTService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if( extras == null || !extras.containsKey( EXTRA_RESULT_RECEIVER ) ) {
            AppUtils.Logger(TAG, "You did not pass extras or data with the Intent.");
            return;
        }

        Serializable action     = extras.getSerializable( ACTION_RESULT );
        String pRequest         = extras.getString( EXTRA_PARAMS );
        ResultReceiver receiver = extras.getParcelable( EXTRA_RESULT_RECEIVER );

        try {
            // Handling XML
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            // Send URL to parse XML Tags
            URL sourceUrl = new URL( IP + YODO_ADDRESS + pRequest );

            // Create handler to handle XML Tags ( extends DefaultHandler )
            xr.setContentHandler( new XMLHandler() );
            xr.parse( new InputSource( sourceUrl.openStream() ) );
        } catch(Exception e) {
            AppUtils.Logger( TAG, "XML Parsing Exception = " + e );
        }

        ServerResponse responseEntity = XMLHandler.response;
        if( responseEntity != null ) {
            Bundle resultData = new Bundle();
            resultData.putSerializable( ACTION_RESULT, action );
            resultData.putSerializable( EXTRA_RESULT, responseEntity );
            receiver.send( STATUS_SUCCESS, resultData );
        }
        else {
            receiver.send( STATUS_FAILED, null );
        }
    }

    public static String getRoot() {
        return IP;
    }
}
