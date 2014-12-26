package co.yodo.launcher.net;

import co.yodo.launcher.helper.AppUtils;

public class ServerRequest {
	/** DEBUG */
	private final static String TAG = ServerRequest.class.getSimpleName();
	
	/** Protocol version used in the request */
	private static final String PROTOCOL_VERSION = "1.1.1";
	
	/** Parameters used for creating an authenticate request */
	private static final String AUTH_REQ            = "0";
	public static final String AUTH_HW_MERCH_SUBREQ = "4";

    /** Parameters used for creating an exchange request */
    private static final String EXCH_REQ         = "1";
    public static final String EXCH_MERCH_SUBREQ = "1";

    /** Parameters used for creating a balance request */
    private static final String QUERY_REQ          = "4";
    public static final String QUERY_BAL_TP_SUBREQ = "2";
    public static final String QUERY_ACC_SUBREQ    = "3";

    /** Parameters used for creating a registration request */
    private static final String REG_REQ         = "9";
    public static final String REG_MERCH_SUBREQ = "1";

    /** Query Records */
    public static final int QUERY_HISTORY_BALANCE = 10;
    public static final int QUERY_TODAY_BALANCE   = 12;
    public static final int QUERY_MERCHANT_LOGO   = 14;
	
	/** Variable that holds request string separator */
	private static final String	REQ_SEP = ",";
	
	/**
	 * Creates an authentication switch request  
	 * @param pUsrData	Encrypted user's data
	 * @param iAuthReqType Sub-type of the request
	 * @return String Request for getting the authentication
	 */
	public static String createAuthenticationRequest(String pUsrData, int iAuthReqType) {
		StringBuilder sAuthenticationRequest = new StringBuilder();
		sAuthenticationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
		sAuthenticationRequest.append( AUTH_REQ ).append( REQ_SEP );
		
		switch( iAuthReqType ) {
			//RT = 0, ST = 4
			case 4: sAuthenticationRequest.append( AUTH_HW_MERCH_SUBREQ ).append( REQ_SEP );
					break;
		}
		sAuthenticationRequest.append( pUsrData );
		
		AppUtils.Logger( TAG, "Authentication Request: " + sAuthenticationRequest.toString() );
		return sAuthenticationRequest.toString();
	}

    /**
     * Creates a query request
     * @param pUsrData	Encrypted user's data
     * @param iQueryReqType Sub-type of the request
     * @return String Request for getting the balance
     */
    public static String createQueryRequest(String pUsrData, int iQueryReqType) {
        StringBuilder sQueryRequest = new StringBuilder();
        sQueryRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sQueryRequest.append( QUERY_REQ ).append( REQ_SEP );

        switch( iQueryReqType ) {
            //RT = 4, ST = 2
            case 2: sQueryRequest.append( QUERY_BAL_TP_SUBREQ ).append( REQ_SEP );
                break;

            //RT = 4, ST = 3
            case 3: sQueryRequest.append( QUERY_ACC_SUBREQ ).append( REQ_SEP );
                break;
        }
        sQueryRequest.append( pUsrData );

        AppUtils.Logger( TAG, "Third Party Balance Request: " + sQueryRequest.toString() );
        return sQueryRequest.toString();
    }

    /**
     * Creates an registration switch request
     * @param pUsrData	Encrypted user's data
     * @param iRegReqType Sub-type of the request
     * @return String Request for getting the registration code
     */
    public static String createRegistrationRequest(String pUsrData, int iRegReqType) {
        StringBuilder sRegistrationRequest = new StringBuilder();
        sRegistrationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sRegistrationRequest.append( REG_REQ ).append( REQ_SEP );

        switch( iRegReqType ) {
            //RT = 9, ST = 1
            case 1: sRegistrationRequest.append( REG_MERCH_SUBREQ ).append( REQ_SEP );
                break;
        }
        sRegistrationRequest.append(pUsrData);

        AppUtils.Logger( TAG, "Registration Request: " + sRegistrationRequest.toString() );
        return sRegistrationRequest.toString();
    }

    /**
     * Creates an exchange switch request
     * @param pUsrData	Encrypted user's data
     * @param iExchReqType Sub-type of the request
     * @return	String	Request for getting the Exchange
     */
    public static String createExchangeRequest(String pUsrData, int iExchReqType){
        StringBuilder sExchangeRequest = new StringBuilder();
        sExchangeRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sExchangeRequest.append( EXCH_REQ ).append( REQ_SEP );

        switch( iExchReqType ) {
            //RT = 1, ST = 1
            case 1: sExchangeRequest.append( EXCH_MERCH_SUBREQ ).append( REQ_SEP );
                break;
        }
        sExchangeRequest.append( pUsrData );

        AppUtils.Logger( TAG, "Exchange Request: " + sExchangeRequest.toString() );
        return sExchangeRequest.toString();
    }
}
