package yodo.co.yodolauncher.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

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
    public static void showAlertDialog(final Context c, final String title, EditText input,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setTitle( title );
        builder.setView( input );
        builder.setCancelable( false );

        builder.setPositiveButton(c.getString(R.string.ok), clickListener);
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
    public static void showAlertDialog(final Context c, final String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.ic_launcher );
        builder.setTitle( title );
        builder.setMessage(message);
        builder.setCancelable( false );

        builder.setNegativeButton( c.getString( R.string.cancel ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
