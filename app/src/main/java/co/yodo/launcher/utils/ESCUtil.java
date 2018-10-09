package co.yodo.launcher.utils;

import java.io.UnsupportedEncodingException;

import co.yodo.restapi.network.model.ServerResponse;

/**
 * Created by hei on 17/01/17.
 * Handles the printer commands
 */
public class ESCUtil {
    /** ESC Elements */
    public static final byte ESC = 27;// Escape
    public static final byte FS = 28;// Text delimiter
    public static final byte GS = 29;// Group separator
    public static final byte DLE = 16;// data link escape
    public static final byte EOT = 4;// End of transmission
    public static final byte ENQ = 5;// Enquiry character
    public static final byte SP = 32;// Spaces
    public static final byte HT = 9;// Horizontal list
    public static final byte LF = 10;// Print and wrap (horizontal orientation)
    public static final byte CR = 13;// Home key
    public static final byte FF = 12;// Carriage control (print and return to the standard mode (in page mode))
    public static final byte CAN = 24;// Canceled (cancel print data in page mode)

    /**
     * Initialize the printer
     * @return the bytes of initialization
     */
    public static byte[] init_printer() {
        byte[] result = new byte[2];
        result[0] = ESC;
        result[1] = 64;
        return result;
    }

    /**
     * Wrap
     * @param lineNum  how many line do you want wrap
     */
    private static byte[] nextLine( int lineNum ) {
        byte[] result = new byte[lineNum];
        for( int i = 0; i < lineNum; i++ )
            result[i] = LF;
        return result;
    }

    /**
     * draw a underline（1 pixel width）
     */
    public static byte[] underlineWithOneDotWidthOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 1;
        return result;
    }

    /**
     * draw a underline（2 pixel width）
     */
    public static byte[] underlineWithTwoDotWidthOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 2;
        return result;
    }

    /**
     * cancel draw a underline
     */
    public static byte[] underlineOff() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 0;
        return result;
    }

    /**
     * select bold option
     */
    private static byte[] boldOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 69;
        result[2] = 0xF;
        return result;
    }

    /**
     * cancel bold option
     */
    private static byte[] boldOff() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 69;
        result[2] = 0;
        return result;
    }

    /**
     * Align left
     */
    private static byte[] alignLeft() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 0;
        return result;
    }

    /**
     * Align center
     */
    private static byte[] alignCenter() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 1;
        return result;
    }

    /**
     * Align right
     */
    public static byte[] alignRight() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 2;
        return result;
    }

    /**
     * Horizontal move col columns to the right
     * @param col The column
     */
    public static byte[] set_HT_position( byte col ) {
        byte[] result = new byte[4];
        result[0] = ESC;
        result[1] = 68;
        result[2] = col;
        result[3] = 0;
        return result;
    }
    // ------------------------Font bigger-----------------------------

    /**
     * Font bigger 5 times than normal
     * @param num Size
     */
    private static byte[] fontSizeSetBig( int num ) {
        byte realSize = 0;
        switch( num ) {
            case 1:
                realSize = 0;
                break;
            case 2:
                realSize = 17;
                break;
            case 3:
                realSize = 34;
                break;
            case 4:
                realSize = 51;
                break;
            case 5:
                realSize = 68;
                break;
            case 6:
                realSize = 85;
                break;
            case 7:
                realSize = 102;
                break;
            case 8:
                realSize = 119;
                break;
        }
        byte[] result = new byte[3];
        result[0] = 29;
        result[1] = 33;
        result[2] = realSize;
        return result;
    }

    // ------------------------Font smaller-----------------------------

    /**
     * font smaller
     * @param num
     */
    private static byte[] fontSizeSetSmall( int num ) {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 33;
        return result;
    }

    /**
     * Paper cutting
     */
    public static byte[] feedPaperCutAll() {
        byte[] result = new byte[4];
        result[0] = GS;
        result[1] = 86;
        result[2] = 65;
        result[3] = 0;
        return result;
    }

    /**
     * Paper cutting（the left leave some）
     */
    private static byte[] feedPaperCutPartial() {
        byte[] result = new byte[4];
        result[0] = GS;
        result[1] = 86;
        result[2] = 66;
        result[3] = 0;
        return result;
    }

    public static byte[] byteMerger( byte[] byte_1, byte[] byte_2 ) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy( byte_1, 0, byte_3, 0, byte_1.length );
        System.arraycopy( byte_2, 0, byte_3, byte_1.length, byte_2.length );
        return byte_3;
    }

    private static byte[] byteMerger( byte[][] byteList ) {
        int length = 0;
        for( byte[] aByteList : byteList ) {
            length += aByteList.length;
        }
        byte[] result = new byte[length];

        int index = 0;
        for( byte[] nowByte : byteList ) {
            for( byte aNowByte : nowByte ) {
                result[ index ] = aNowByte;
                index++;
            }
        }

        /*for (int i = 0; i < index; i++) {
            // CommonUtils.LogWuwei("", "result[" + i + "] is " + result[i]);
        }*/
        return result;
    }

    /*public static byte[] parseData( Bitmap bitmap ) {
        try {
            byte[] center = ESCUtil.alignCenter();
            byte[] command = decodeBitmap(bitmap);
            byte[] nextLine = ESCUtil.nextLine( 1 );

            byte[] next4Line = ESCUtil.nextLine( 4 );
            byte[] breakPartial = ESCUtil.feedPaperCutPartial();

            byte[][] cmdBytes = {
                    center, command, nextLine,

                    next4Line, breakPartial
            };

            return ESCUtil.byteMerger( cmdBytes );
        } catch ( NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static byte[] parseData( ServerResponse response, String total, String cashTender, String cashBack, String currency ) {
        try {
            byte[] next2Line = ESCUtil.nextLine( 2 );
            byte[] title = response.getParams().getMerchant().getBytes( "gb2312" );

            next2Line = ESCUtil.nextLine( 2 );

            byte[] priceCashTenderInfo = ( " Cash Tender: " + cashTender + " " + currency ).getBytes( "gb2312" );
            byte[] nextLine = ESCUtil.nextLine( 1 );
            byte[] priceCashBackInfo = ( " Cash Back： " + cashBack ).getBytes( "gb2312" );
            nextLine = ESCUtil.nextLine( 1 );

            byte[] boldOn =  boldOn = ESCUtil.boldOn();
            byte[] fontSize1Big = ESCUtil.fontSizeSetBig( 2 );
            byte[] FocusOrderTotal = ( "Paid: " + total ).getBytes( "gb2312" );
            byte[] boldOff = ESCUtil.boldOff();
            byte[] fontSize1Small = ESCUtil.fontSizeSetSmall( 2 );

            boldOn = ESCUtil.boldOn();
            byte[] fontSize2Big = ESCUtil.fontSizeSetBig( 3 );
            byte[] center = ESCUtil.alignCenter();
            byte[] Focus = ( "AU# " + response.getAuthNumber() ).getBytes( "gb2312" );
            boldOff = ESCUtil.boldOff();
            byte[] fontSize2Small = ESCUtil.fontSizeSetSmall( 3 );

            nextLine = ESCUtil.nextLine( 1 );
            byte[] orderTime = FormatUtils.UTCtoCurrent( response.getParams().getCreated() ).getBytes( "gb2312" );
            byte[] next4Line = ESCUtil.nextLine( 4 );

            byte[] breakPartial = ESCUtil.feedPaperCutPartial();

            byte[][] cmdBytes = {
                    center, fontSize1Big, title, nextLine, next2Line,
                    center, fontSize2Small, priceCashTenderInfo, nextLine,
                    center, fontSize2Small, priceCashBackInfo, next2Line,
                    center, boldOn, fontSize1Big, FocusOrderTotal, boldOff, next2Line,

                    center, boldOn, fontSize2Small, orderTime, boldOff, nextLine,
                    center, boldOn, fontSize1Small, Focus, boldOff,

                    next4Line, breakPartial
            };

            return ESCUtil.byteMerger( cmdBytes );
        } catch( UnsupportedEncodingException | NullPointerException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
