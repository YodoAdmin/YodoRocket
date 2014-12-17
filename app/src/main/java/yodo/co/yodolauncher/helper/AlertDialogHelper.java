package yodo.co.yodolauncher.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import yodo.co.yodolauncher.R;

/**
 * Created by luis on 16/12/14.
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
        builder.setIcon( R.drawable.ic_launcher );
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
        builder.setIcon( R.drawable.ic_launcher );
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
     * @param title The title of the dialog
     * @param message A message to show
     */
    public static void showAlertDialog(final Context c, final String title, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage( message );
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
                                       final EditText input, final boolean show, final boolean remember,
                                       final DialogInterface.OnClickListener clickListener) {
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );

        LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_with_password, new LinearLayout( c ), false );
        ((LinearLayout) layout).addView(input, 0);

        if( show ) {
            CheckBox showPassword = (CheckBox) layout.findViewById(R.id.showPassword);
            showPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((CheckBox)v).isChecked())
                        input.setInputType( InputType.TYPE_TEXT_VARIATION_PASSWORD );
                    else
                        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
                }
            });
        }

        if( remember ) {
            CheckBox rememberPassword = (CheckBox) layout.findViewById(R.id.rememberPassword);
            rememberPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setView(layout);
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );
        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
