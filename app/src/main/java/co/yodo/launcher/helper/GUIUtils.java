package co.yodo.launcher.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.util.ExceptionToResourceMapping;

import java.util.Arrays;
import java.util.Locale;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.LauncherActivity;

/**
 * Created by hei on 20/06/16.
 * Utils used for the interface
 */
public class GUIUtils {
    /**
     * Get the drawable based on the name
     * @param c The Context of the Android system.
     * @param name The name of the drawable
     * @return The drawable
     */
    public static Drawable getDrawableByName( Context c, String name ) throws Resources.NotFoundException {
        Resources resources = c.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable", c.getPackageName());
        Drawable image = ContextCompat.getDrawable( c, resourceId );
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        return image;
    }

    /**
     * Show or hide the password depending on the checkbox
     * @param state The checkbox
     * @param password The EditText for the password
     */
    public static void showPassword( CheckBox state, EditText password) {
        if( state.isChecked() )
            password.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
        else
            password.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        password.setTypeface( Typeface.MONOSPACE );
    }

    /**
     * Hides the soft keyboard
     * @param a The activity where the keyboard is open
     */
    public static void hideSoftKeyboard(Activity a) {
        View v = a.getCurrentFocus();
        if( v != null ) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( v.getWindowToken(), 0 );
        }
    }

    /**
     * Plays a sound of error
     * @param c The Context of the Android system.
     */
    public static void errorSound(Context c) {
        MediaPlayer mp = MediaPlayer.create( c, R.raw.error );
        mp.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }

    public static void setLanguage( Context ac ) {
        final String language = PrefUtils.getLanguage( ac );
        if( language != null ) {
            Locale appLoc = new Locale( language );

            Resources res = ac.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();

            Locale.setDefault( appLoc );
            Configuration config = new Configuration( res.getConfiguration() );
            config.locale = appLoc;

            res.updateConfiguration( config, dm );
        }
    }

    /**
     * Rotates an image by 360 in 1 second
     * @param image The image to rotate
     */
    public static void rotateImage(View image) {
        RotateAnimation rotateAnimation1 = new RotateAnimation( 0, 90,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f );
        rotateAnimation1.setInterpolator( new LinearInterpolator() );
        rotateAnimation1.setDuration( 500 );
        rotateAnimation1.setRepeatCount( 0 );

        image.startAnimation( rotateAnimation1 );
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     */
    public static boolean setViewIcon( Context c, TextView v, Integer resource ) {
        // Clean an icon from the view
        if( resource == null ) {
            v.setCompoundDrawables( null, null, null, null );
            return true;
        }
        // Try to set an icon to a view
        Drawable icon = ContextCompat.getDrawable( c, resource );
        if( icon != null ) {
            icon.setBounds( 3, 0, v.getLineHeight(), (int) ( v.getLineHeight() * 0.9 ) );
            v.setCompoundDrawables( icon, null, null, null );
            return true;
        }
        return false;
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     * @param currency The currency to tbe set in the view
     */
    private static void setCurrencyIcon( Context c, TextView v, String currency ) {
        // Get currencies and icons
        final String[] icons = c.getResources().getStringArray( R.array.currency_icon_array );
        final String[] currencies = c.getResources().getStringArray( R.array.currency_array );

        // Set the drawable to the TextView
        final int position = Arrays.asList( currencies ).indexOf( currency );
        Drawable icon = getDrawableByName( c, icons[ position ] );
        icon.setBounds( 3, 0, v.getLineHeight(), ( int ) ( v.getLineHeight() * 0.9 ) );
        v.setCompoundDrawables( icon, null, null, null );
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     */
    public static void setTenderCurrencyIcon( Context c, TextView v ) {
        setCurrencyIcon( c, v, PrefUtils.getTenderCurrency( c ) );
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     */
    public static void setMerchantCurrencyIcon( Context c, TextView v ) {
        setCurrencyIcon( c, v, PrefUtils.getMerchantCurrency( c ) );
    }

    public static void setQuickKeys( LauncherActivity act, String currency ) {
        Button b1 = (Button) act.findViewById( R.id.b1000 );
        Button b2 = (Button) act.findViewById( R.id.b2000 );
        Button b3 = (Button) act.findViewById( R.id.b5000 );
        Button b4 = (Button) act.findViewById( R.id.b10000 );
        Button b5 = (Button) act.findViewById( R.id.b20000 );

        switch( currency ) {
            case "CAD":
            case "USD":
            case "CNY":
            case "EUR":
            case "TRY":
            case "BRL":
                b1.setText( R.string.coins_5 );
                b2.setText( R.string.coins_10 );
                b3.setText( R.string.coins_20 );
                b4.setText( R.string.coins_50 );
                b5.setText( R.string.coins_100 );
                break;

            case "PEN":
            case "UAH":
                b1.setText( R.string.coins_10 );
                b2.setText( R.string.coins_20 );
                b3.setText( R.string.coins_50 );
                b4.setText( R.string.coins_100 );
                b5.setText( R.string.coins_200 );
                break;

            case "PHP":
            case "MXN":
                b1.setText( R.string.coins_20 );
                b2.setText( R.string.coins_50 );
                b3.setText( R.string.coins_100 );
                b4.setText( R.string.coins_200 );
                b5.setText( R.string.coins_500 );
                break;

            case "JPY":
                b1.setText( R.string.coins_500 );
                b2.setText( R.string.coins_1000 );
                b3.setText( R.string.coins_3000 );
                b4.setText( R.string.coins_5000 );
                b5.setText( R.string.coins_10000 );
                break;

            case "INR":
                b1.setText( R.string.coins_10 );
                b2.setText( R.string.coins_20 );
                b3.setText( R.string.coins_50 );
                b4.setText( R.string.coins_100 );
                b5.setText( R.string.coins_500 );
                break;

            case "RUB":
                b1.setText( R.string.coins_10 );
                b2.setText( R.string.coins_50 );
                b3.setText( R.string.coins_100 );
                b4.setText( R.string.coins_500 );
                b5.setText( R.string.coins_1000 );
                break;
        }
    }
}
