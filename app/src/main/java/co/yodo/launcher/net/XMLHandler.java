package co.yodo.launcher.net;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import co.yodo.launcher.data.ServerResponse;

/**
 * Created by luis on 15/12/14.
 */
public class XMLHandler extends DefaultHandler {
    /** XML root element */
    private static final String ROOT_ELEMENT = "Yodoresponse";

    /** XML sub root element */
    private static final String CODE_ELEM     = "code";
    private static final String AUTH_NUM_ELEM = "authNumber";
    private static final String MESSAGE_ELEM  = "message";
    private static final String TIME_ELEM     = "rtime";

    /** Param elements */
    private static final String LOGO_ELEM = "logo_url";

    /** Parser Elements */
    private Boolean currentElement = false;
    private String currentValue = null;

    /** Server Response POJO */
    public static ServerResponse response = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = true;

        if(localName.equalsIgnoreCase(ROOT_ELEMENT)) {
            /** Start */
            response = new ServerResponse();
        }
    }

    /** Called when tag closing ( ex:- <name>AndroidPeople</name>
     * -- </name> )*/
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = false;

        /** set value */
        if(localName.equalsIgnoreCase(CODE_ELEM)) {
            response.setCode(currentValue);
        }
        else if(localName.equalsIgnoreCase(AUTH_NUM_ELEM)) {
            response.setAuthNumber(currentValue);
        }
        else if(localName.equalsIgnoreCase(MESSAGE_ELEM)) {
            response.setMessage(currentValue);
        }
        else if(localName.equalsIgnoreCase(TIME_ELEM)) {
            response.setRTime(Long.valueOf(currentValue));
        }

        /** Params */
        else if(localName.equalsIgnoreCase(LOGO_ELEM)) {
            response.addParam( ServerResponse.LOGO, currentValue );
        }
    }

    /** Called to get tag characters ( ex:- <name>AndroidPeople</name>
     * -- to get AndroidPeople Character ) */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if( currentElement ) {
            currentValue = new String( ch, start, length );
            currentElement = false;
        }
    }
}
