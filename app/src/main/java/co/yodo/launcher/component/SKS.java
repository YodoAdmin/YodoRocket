package co.yodo.launcher.component;

import java.math.BigDecimal;

/**
 * Created by hei on 18/08/16.
 * contains the structure of the SKS
 */
public class SKS {
    /** Size of the data */
    private static final int SKS_SIZE = 128;
    private static final int ALT_SIZE = 129;

    /** Types of payments */
    public enum PAYMENT {
        YODO,
        CREDIT,
        TRANSIT,
        HEART,
        VISA,
        PAYPAL,
        STATIC;

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
        if( header != null ) {
            final String[] split = header.split( HDR_SEP );

            this.mClient = client;
            this.mPayment = PAYMENT.values[ Integer.valueOf( split[ 0 ] ) ];
            this.mTip = new BigDecimal( split[ 1 ] ).movePointLeft( 2 );
        } else {
            String tempClient;
            PAYMENT tempPayment;
            if( client.length() == ALT_SIZE ) {
                tempClient = client.substring( 0, client.length() - 1 );
                tempPayment = PAYMENT.values[ Integer.valueOf( client.substring( client.length() - 1 ) ) ];
            } else {
                tempClient = client;
                tempPayment = PAYMENT.YODO;
            }

            this.mClient = tempClient;
            this.mPayment = tempPayment;
            this.mTip = BigDecimal.ZERO;
        }
    }

    public static SKS build( String data ) {
        try {
            final String[] split = data.split( QR_SEP );
            // Support for old SKS
            if( split.length == 1 ) {
                final String client = split[ 0 ];
                final int length = client.length();
                if( length == SKS_SIZE || length == ALT_SIZE )
                    return new SKS( null, client );
            }
            // New SKS with header
            else if( split.length == 2 ) {
                final String header = split[ 0 ];
                final String client = split[ 1 ];

                return new SKS( header, client );
            }
            // There is a problem with the SKS
            else {
                throw new ArrayIndexOutOfBoundsException( "SKS with too many parameters" );
            }
        } catch( ArrayIndexOutOfBoundsException ex ) {
            ex.printStackTrace();
            return null;
        }
        return null;
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
