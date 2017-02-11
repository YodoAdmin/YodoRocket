package co.yodo.launcher.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by hei on 22/06/16.
 * Formats different structures (e.g. date, integer), or
 * cast values to other objects
 */
public class FormatUtils {
    /**
     * Transforms a UTC date to the cellphone date
     * @param date The date in UTC
     * @return the Date in the cellphone time
     */
    static String UTCtoCurrent( String date ) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.US );

        try {
            TimeZone z = c.getTimeZone();
            int offset = z.getRawOffset();
            if( z.inDaylightTime( new Date() ) )
                offset = offset + z.getDSTSavings();
            int offsetHrs = offset / 1000 / 60 / 60;

            c.setTime( sdf.parse( date ) );
            c.add( Calendar.HOUR_OF_DAY, offsetHrs );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sdf.format( c.getTimeInMillis() );
    }

    /**
     * Transforms a UTC date to the cellphone date
     * @return the Date in the cellphone time
     */
    static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.US );
        return sdf.format( new Date() );
    }

    /**
     * Truncates n number of positions (decimal) from a number
     * @param number The number to be truncated
     * @param positions the n positions
     * @return The truncated number as String
     */
    private static String truncateDecimal( String number, int positions ) {
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
