package co.yodo.launcher.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;

import co.yodo.launcher.R;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Shows a dialog for alert messages
     * @param c The context of the application
     * @param message A message to setData
     * @param onClick click for the positive button
     */
    public static void create(Context c, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(c.getString(R.string.text_ok), onClick);

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param c The context of the application
     * @param message The message of the dialog
     * @param onClick Action for the selection
     */
    public static AlertDialog create(Context c, Integer title, Integer message, View layout,
                                      DialogInterface.OnClickListener onClick, boolean cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);

        if (title != null) {
            builder.setTitle(title);
        }

        if (message != null) {
            builder.setMessage(message);
        }

        if (layout != null) {
            builder.setView(layout);
        }

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.text_ok, onClick);

        if (cancel) {
            builder.setNegativeButton(R.string.text_cancel, null);
        }

        return builder.create();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param layout The view of the dialog
     * @return The AlertDialog object
     */
    public static AlertDialog create(Context c, int title, View layout) {
        // Creates a dialog only with ok
        return create(c, title, null, layout, null, false);
    }


    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param c The context of the activity
     * @param message The message of the dialog
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create(Context c, Integer message, View layout, DialogInterface.OnShowListener okClick) {
        // Creates a dialog with ok and cancel
        final AlertDialog dialog = create(c, null, message, layout, null, true);

        if (okClick != null) {
            dialog.setOnShowListener(okClick);
        }

        return dialog;
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param c The context of the activity
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create(Context c, View layout, DialogInterface.OnShowListener okClick) {
        return create(c, null, layout, okClick);
    }

    /**
     * Shows an alert dialog for the response from the server
     * @param c The context of the application
     * @param message A message to setData
     */
    public static AlertDialog create(Context c, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(c.getString(R.string.text_ok), null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return alertDialog;
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param okClick Action for the positive button
     * @param cancelClick Action for the negative button
     */
    public static void create(Context c, int message, DialogInterface.OnClickListener okClick,
                               DialogInterface.OnClickListener cancelClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(c.getString( R.string.text_ok), okClick);

        if (cancelClick != null) {
            builder.setNegativeButton(c.getString(R.string.text_cancel), cancelClick);
        }

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     */
    public static void create(Context c, int message, DialogInterface.OnClickListener okClick) {
        create(c, message, okClick, null);
    }

    /**
     * Shows an alert dialog for a list
     * @param c The context of the application
     * @param title The title of the dialog
     * @param adapter Adapter for the list
     * @param current Current selected
     * @param okClick Action for the selection
     */
    public static void create(Context c, String title, ListAdapter adapter, int current, DialogInterface.OnClickListener okClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title);
        builder.setCancelable(false);

        builder.setSingleChoiceItems(adapter, current, okClick);
        builder.setNegativeButton(c.getString( R.string.text_cancel), null );

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param input An EditText to receive data
     * @param okClick Action for the selection
     */
    public static void create(Context c, String title, EditText input, DialogInterface.OnClickListener okClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title);
        builder.setView(input);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.text_ok, okClick);
        builder.setNegativeButton(R.string.text_cancel, null);

        builder.show();
    }
}
