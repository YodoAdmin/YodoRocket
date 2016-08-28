package co.yodo.launcher.ui.notification;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The context of the activity
     * @param message The message of the dialog
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, Integer message, View layout,
                                      DialogInterface.OnShowListener okClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac, R.style.AppCompatAlertDialogStyle );

        if( message != null )
            builder.setMessage( message );

        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( R.string.ok, null );

        if( okClick != null )
            builder.setNegativeButton( R.string.cancel, null );

        final AlertDialog oDialog = builder.create();

        if( okClick != null ) {
            oDialog.setOnShowListener( okClick );
            oDialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE );
        }

        return oDialog;
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The context of the activity
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, View layout, DialogInterface.OnShowListener okClick ) {
        return create( ac, null, layout, okClick );
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param ac The context of the application
     * @param message The message of the dialog
     * @param clickListener Action for the selection
     */
    public static AlertDialog create( Context ac, Integer title, Integer message, View layout,
                                      DialogInterface.OnClickListener clickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac, R.style.AppCompatAlertDialogStyle );

        if( title != null )
            builder.setTitle( title );

        if( message != null )
            builder.setMessage( message );

        if( layout != null )
            builder.setView( layout );

        builder.setCancelable( false );

        builder.setPositiveButton( R.string.ok, clickListener );

        if( clickListener != null )
            builder.setNegativeButton( R.string.cancel, null );

        return builder.create();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param ac The context of the application
     * @param title The title of the dialog
     * @param layout The view of the dialog
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, Integer title, View layout ) {
        return create( ac, title, null, layout, null );
    }

    /**
     * Shows an alert dialog for a list
     * @param c The context of the application
     * @param title The title of the dialog
     * @param adapter Adapter for the list
     * @param current Current selected
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog( final Context c, final String title,
                                        final ListAdapter adapter, final int current,
                                        final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setSingleChoiceItems( adapter, current, clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input An EditText to receive data
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog(final Context c, final String title, final EditText input,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setView( input );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString(R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param positiveClick Action for the positive button
     * @param negativeClick Action for the negative button
     */
    public static void showAlertDialog( final Context c, final int message,
                                        final DialogInterface.OnClickListener positiveClick,
                                        final DialogInterface.OnClickListener negativeClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.icon );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), positiveClick );
        builder.setNegativeButton( c.getString( R.string.cancel ), negativeClick );

        builder.show();
    }

    public static void showAlertDialog( final Context c, final int message,
                                        final DialogInterface.OnClickListener clickListener ) {
        showAlertDialog( c, message, clickListener, null );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message A message to show
     * @param clickListener click for the negative button
     */
    public static void showAlertDialog(final Context c, final String title, final String message,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param ac The context of the application
     * @param title The title of the dialog
     * @param input The edit text for the password
     * @param remember The boolean to include a checkbox to remember the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog( final Context ac,
                                        final String title, final EditText input,
                                        final boolean remember,
                                        final DialogInterface.OnClickListener clickListener ) {
        // Remove the parent if already has one
        if( input.getParent() != null )
            ( (ViewGroup ) input.getParent() ).removeView( input );

        // Find the layout dialog_with password, and add the input
        LayoutInflater inflater = (LayoutInflater) ac.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_pip, new LinearLayout( ac ), false );
        ((LinearLayout) layout).addView( input, 0 );

        // Changes the input type to password
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );

        // Sets the CheckBox function to show the input text or remember the password
        final CheckBox cbOption = (CheckBox) layout.findViewById( R.id.cbOption );
        if( !remember ) {
            cbOption.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    GUIUtils.showPassword( cbOption, input );
                }
            });
        } else {
            cbOption.setText( R.string.remember_pass );
            final String password = PrefUtils.getPassword( ac );

            if( password != null ) {
                input.setText( password );
                cbOption.setChecked( true );
            }
        }

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder( ac, R.style.AppCompatAlertDialogStyle );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( ac.getString( R.string.ok ), new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                if( remember ) {
                    SystemUtils.Logger( "Save", cbOption.isChecked() + "" );
                    if( cbOption.isChecked() ) {
                        final String pip = input.getText().toString();
                        PrefUtils.savePassword( ac, pip );
                    } else
                        PrefUtils.savePassword( ac, null );
                }

                clickListener.onClick( dialog, which );
            }
        } );

        builder.setNegativeButton( ac.getString( R.string.cancel ), null );

        builder.show();
    }
}
