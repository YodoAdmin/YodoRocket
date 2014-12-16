package yodo.co.yodolauncher.data;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    /** ID for authorized responses */
    public static final String AUTHORIZED		       = "AU00";
    public static final String AUTHORIZED_REGISTRATION = "AU01";
    public static final String AUTHORIZED_ALTERNATE    = "AU69";
    public static final String AUTHORIZED_TRANSFER     = "AU88";

    /** ID for error responses */
    public static final String ERROR_INTERNET      = "ERIN";
    public static final String ERROR_FAILED        = "ER00";
    public static final String ERROR_MAX_LIM       = "ER13";
    public static final String ERROR_INSUFF_FUNDS  = "ER25";
    public static final String ERROR_INCORRECT_PIP = "ER22";

	private String code;
	private String authNumber;
	private String message;
	private long rtime;

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
	
	@Override
	public String toString() {
		return new StringBuffer(" Code : ").append(this.code).append("\n")
				.append(" AuthNumber : ").append(this.authNumber).append("\n")
				.append(" Message : ").append(this.message).append("\n")
				.append(" Time : ").append(this.rtime).toString();
	}
}
