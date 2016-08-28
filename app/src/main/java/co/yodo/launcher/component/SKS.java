package co.yodo.launcher.component;

import java.math.BigDecimal;

/**
 * Created by hei on 18/08/16.
 * contains the structure of the SKS
 */
public class SKS {
    /** Size of the data */
    public static final int SKS_SIZE = 128;

    /** Types of payments */
    public enum PAYMENT {
        YODO,
        CREDIT,
        TRANSIT,
        HEART,
        VISA,
        PAYPAL;

        public static final PAYMENT values[] = values();
    }

    /** QR code separator */
    private static final String QR_SEP = "#";

    /** Header data separator */
    private static final String HDR_SEP = ",";

    /** SKS Data */
    private final String mClient;

    /** SKS Options */
    private final PAYMENT mPayment;
    private final BigDecimal mTip;

    /**
     * Private creator of the SKS
     * @param header The header with the main options
     * @param client The client encrypted data
     */
    private SKS( String header, String client ) {
        final String[] split = header.split( HDR_SEP );

        this.mClient = client;
        this.mPayment = PAYMENT.values[ Integer.valueOf( split[0] ) ];
        this.mTip = new BigDecimal( split[1] ).movePointLeft( 2 );
    }

    public static SKS build( String data ) {
        final String[] split = data.split( QR_SEP );

        final String header = split[0];
        final String client = split[1];

        return ( client.length() == SKS_SIZE ) ?
            new SKS( header, client ) : null;
    }

    /**
     * Gets the user clients data
     * @return A String with the data
     */
    public String getClient() {
        return this.mClient;
    }

    /**
     * Gets the type of payment method
     * @return The enum with type of payment
     */
    public PAYMENT getPaymentMethod() {
        return this.mPayment;
    }

    /**
     * Gets the tip
     * @return A BigDecimal with the tip value %
     */
    public BigDecimal getTip() {
        return this.mTip;
    }
}
