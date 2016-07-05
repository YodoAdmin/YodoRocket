package co.yodo.launcher.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by hei on 22/06/16.
 * Formats different structures (e.g. date, integer), or
 * cast values to other objects
 */
public class FormatUtils {
    /**
     * Truncates n number of positions (decimal) from a number
     * @param number The number to be truncated
     * @param positions the n positions
     * @return The truncated number as String
     */
    public static String truncateDecimal( String number, int positions ) {
        BigDecimal value  = new BigDecimal( number );
        return value.setScale( positions, RoundingMode.DOWN ).toString();
    }

    /**
     * Truncates 2 number of positions (decimal) from a number
     * @param number The number to be truncated
     * @return The truncated number as String
     */
    public static String truncateDecimal( String number ) {
        return truncateDecimal( number, 2 );
    }

    /**
     * Replace a null string with an empty one
     * @param input The original String
     * @return The string or "" if null
     */
    public static String replaceNull( String input ) {
        return input == null ? "" : input;
    }
}
