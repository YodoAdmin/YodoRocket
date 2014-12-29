package co.yodo.launcher.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class ServerResponse implements Serializable {
    /** ID for authorized responses */
    public static final String AUTHORIZED		       = "AU00";
    public static final String AUTHORIZED_REGISTRATION = "AU01";
    public static final String AUTHORIZED_ALTERNATE    = "AU69";
    public static final String AUTHORIZED_TRANSFER     = "AU88";

    /** ID for error responses */
    public static final String ERROR_FAILED        = "ER00";
    public static final String ERROR_MAX_LIM       = "ER13";
    public static final String ERROR_DUP_AUTH      = "ER20";
    public static final String ERROR_INCORRECT_PIP = "ER22";
    public static final String ERROR_INSUFF_FUNDS  = "ER25";

    /** Param keys */
    public static final String PARAMS      = "params";
    public static final String LOGO        = "logo";
    public static final String CREDIT      = "credit";
    public static final String DEBIT       = "debit";
    public static final String SETTLEMENT  = "settlement";
    public static final String EQUIPMENT   = "equipments";
    public static final String LEASE       = "lease";
    public static final String TOTAL_LEASE = "totalLease";

	private String code;
	private String authNumber;
	private String message;
	private long rtime;
    private HashMap<String, String> params;

    public ServerResponse() {
        params = new HashMap<>();
    }

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public void setAuthNumber(String authNumber) {
		this.authNumber = authNumber;
	}

	public String getAuthNumber() {
		return this.authNumber;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setRTime(long rtime) {
		this.rtime = rtime;
	}

	public long getRTime() {
		return this.rtime;
	}

    public void addParam(String key, String value) {
        params.put( key, value );
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public HashMap<String, String> getParams() { return params; }
	
	@Override
	public String toString() {
		return "\nCode : " + this.code + "\n" +
               " AuthNumber : " + this.authNumber + "\n" +
               " Message : " + this.message + "\n" +
               " Time : " + this.rtime + "\n" +
               " Params : " + Arrays.asList(this.params);
	}
}