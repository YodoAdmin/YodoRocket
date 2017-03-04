package co.yodo.launcher.helper;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

    /** Values used for the images */
    private static final String hexStr = "0123456789ABCDEF";
    private static final String[] binaryArray = { "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111" };

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

    public static byte[] parseData( Bitmap bitmap ) {
        try {
            byte[] center = ESCUtil.alignCenter();
            byte[] command = ESCUtil.decodeBitmap( bitmap );
            byte[] next4Line = ESCUtil.nextLine( 4 );
            byte[] breakPartial = ESCUtil.feedPaperCutPartial();

            byte[][] cmdBytes = {
                    next4Line, center, command,
                    next4Line, breakPartial
            };

            return ESCUtil.byteMerger( cmdBytes );
        } catch ( NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    private static byte[] decodeBitmap( Bitmap bmp ){
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<>(); //binaryString list
        StringBuffer sb;


        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<>();
        commandList.add(commandHexString+widthHexString+heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    private static List<String> binaryListToHexStringList( List<String> list ) {
        List<String> hexList = new ArrayList<>();
        for (String binaryStr : list) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    private static String myBinaryStrToHexString( String binaryStr ) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    private static byte[] hexList2Byte( List<String> list ) {
        List<byte[]> commandList = new ArrayList<>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        return sysCopy(commandList);
    }

    private static byte[] hexStringToBytes( String hexString ) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte[] sysCopy( List<byte[]> srcArrays ) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
