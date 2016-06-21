package co.yodo.launcher.ui.notification;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.PrefsUtils;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {

    /**
     * Shows an alert dialog for a list
     * @param c The context of the application
     * @param title The title of the dialog
     * @param values Values to be shown
     * @param current Current selected
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final CharSequence[] values, final int current,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setSingleChoiceItems( values, current, clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog for a list
     * @param c The context of the application
     * @param title The title of the dialog
     * @param adapter Adapter for the list
     * @param current Current selected
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final ListAdapter adapter, final int current,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setCancelable( false );

        builder.setSingleChoiceItems( adapter, current, clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
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
     * @param clickListener Action for the selection
     */
    public static void showAlertDialog( final Context c, final int message,
                                       final DialogInterface.OnClickListener clickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
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
     * @return The created dialog
     */
    public static AlertDialog showAlertDialog( final Context c, final int message,
                                               final DialogInterface.OnClickListener positiveClick,
                                               final DialogInterface.OnClickListener negativeClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), positiveClick );
        builder.setNegativeButton( c.getString( R.string.cancel ), negativeClick );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
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
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
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
     * @param c The context of the application
     * @param title The title of the dialog
     * @param view The view of the dialog
     */
    public static void showAlertDialog( final Context c, final String title, final View view ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setView( view );
        builder.setCancelable( false );

        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input The edit text for the password
     * @param show The boolean to include a checkbox to show the password
     * @param remember The boolean to include a checkbox to remember the password
     * @param clickListener Action attached to the dialog
     */
    public static void showAlertDialog(final Context c, final String title,
                                       final EditText input, final boolean show,
                                       final boolean remember, final CheckBox rememberPassword,
                                       final DialogInterface.OnClickListener clickListener) {
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );

        LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_password, new LinearLayout( c ), false );
        ((LinearLayout) layout).addView( input, 0 );
        CheckBox showPassword = (CheckBox) layout.findViewById( R.id.showPassword );

        if( show ) {
            showPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((CheckBox)v).isChecked())
                        input.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
                    else
                        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
                }
            });
        } else
            showPassword.setVisibility( View.GONE );

        if( remember ) {
            ((LinearLayout) layout).addView( rememberPassword );

            String password = PrefsUtils.getPassword( c );

            if( password != null ) {
                input.setText( password );
                rememberPassword.setChecked( true );
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setView(layout);
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
