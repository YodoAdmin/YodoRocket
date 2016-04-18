package co.yodo.launcher.net;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import co.yodo.launcher.data.ServerResponse;

/**
 * Created by luis on 15/12/14.
 * Handler for the XML responses
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
    private static final String PARAMS_ELEM       = "params";
    private static final String LOGO_ELEM         = "logo_url";
    private static final String DEBIT_ELEM        = "MerchantDebitWTCost";
    private static final String CREDIT_ELEM       = "MerchantCreditWTCost";
    private static final String CURRENCY_ELEM     = "DefaultCurrency";
    private static final String SETTLEMENT_ELEM   = "Settlement";
    private static final String EQUIPMENT_ELEM    = "Equipments";
    private static final String LEASE_ELEM        = "Lease";
    private static final String TOTAL_LEASE_ELEM  = "TotalLease";
    private static final String ACCOUNT_ELEM      = "account";
    private static final String PURCHASE_ELEM     = "purchase_price";
    private static final String AMOUNT_DELTA_ELEM = "amount_delta";

    /** Parser Elements */
    private Boolean currentElement = false;
    private String currentValue = null;

    /** Server Response POJO */
    public static ServerResponse response = null;

    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
        currentElement = true;

        if(localName.equalsIgnoreCase(ROOT_ELEMENT)) {
            /** Start */
            response = new ServerResponse();
        }
    }

    /** Called when tag closing ( ex:- <name>AndroidPeople</name>
     * -- </name> )*/
    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException {
        currentElement = false;

        /** set value */
        if( localName.equalsIgnoreCase( CODE_ELEM ) ) {
            response.setCode( currentValue );
        }
        else if( localName.equalsIgnoreCase( AUTH_NUM_ELEM ) ) {
            response.setAuthNumber( currentValue );
        }
        else if( localName.equalsIgnoreCase( MESSAGE_ELEM ) ) {
            response.setMessage( currentValue );
        }
        else if( localName.equalsIgnoreCase( TIME_ELEM ) ) {
            response.setRTime( Long.valueOf( currentValue ) );
        }

        /** Params */
        else if( localName.equalsIgnoreCase( PARAMS_ELEM ) ) {
            response.addParam( ServerResponse.PARAMS, currentValue );
        }
        else if( localName.equalsIgnoreCase( LOGO_ELEM ) ) {
            response.addParam( ServerResponse.LOGO, currentValue );
        }
        else if( localName.equalsIgnoreCase( DEBIT_ELEM ) ) {
            response.addParam( ServerResponse.DEBIT, currentValue );
        }
        else if( localName.equalsIgnoreCase( CREDIT_ELEM ) ) {
            response.addParam( ServerResponse.CREDIT, currentValue );
        }
        else if( localName.equalsIgnoreCase( SETTLEMENT_ELEM ) ) {
            response.addParam( ServerResponse.SETTLEMENT, currentValue );
        }
        else if( localName.equalsIgnoreCase( CURRENCY_ELEM ) ) {
            response.addParam( ServerResponse.CURRENCY, currentValue );
        }
        else if( localName.equalsIgnoreCase( EQUIPMENT_ELEM ) ) {
            response.addParam( ServerResponse.EQUIPMENT, currentValue );
        }
        else if( localName.equalsIgnoreCase( LEASE_ELEM ) ) {
            response.addParam( ServerResponse.LEASE, currentValue );
        }
        else if( localName.equalsIgnoreCase( TOTAL_LEASE_ELEM ) ) {
            response.addParam( ServerResponse.TOTAL_LEASE, currentValue );
        }
        else if( localName.equalsIgnoreCase( ACCOUNT_ELEM ) ) {
            response.addParam( ServerResponse.ACCOUNT, currentValue );
        }
        else if( localName.equalsIgnoreCase( PURCHASE_ELEM ) ) {
            response.addParam( ServerResponse.PURCHASE, currentValue );
        }
        else if( localName.equalsIgnoreCase( AMOUNT_DELTA_ELEM ) ) {
            response.addParam( ServerResponse.AMOUNT_DELTA, currentValue );
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
