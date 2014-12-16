package yodo.co.yodolauncher.net;

import yodo.co.yodolauncher.helper.AppUtils;

public class ServerRequest {
	/** DEBUG */
	private final static String TAG = ServerRequest.class.getSimpleName();
	
	/** Protocol version used in the request */
	private static final String PROTOCOL_VERSION = "1.1.1";
	
	/** Parameters used for creating an authenticate request */
	private static final String AUTH_REQ            = "0";
	public static final String AUTH_HW_MERCH_SUBREQ = "4";
	
	/** Variable that holds request string separator */
	private static final String	REQ_SEP = ",";
	
	/**
	 * Creates an authentication switch request  
	 * @param pUsrData	Encrypted user's data
	 * @param iAuthReqType Sub-type of the request
	 * @return String Request for getting the authentication
	 */
	public static String createAuthenticationRequest(String pUsrData, int iAuthReqType){
		StringBuilder sAuthenticationRequest = new StringBuilder();
		sAuthenticationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
		sAuthenticationRequest.append( AUTH_REQ ).append( REQ_SEP );
		
		switch( iAuthReqType ) {
			//RT = 0, ST = 4
			case 4: sAuthenticationRequest.append( AUTH_HW_MERCH_SUBREQ ).append( REQ_SEP );
					break;
		}
		sAuthenticationRequest.append( pUsrData );
		
		AppUtils.Logger(TAG, "Authentication Request: " + sAuthenticationRequest.toString());
		return sAuthenticationRequest.toString();
	}
}
