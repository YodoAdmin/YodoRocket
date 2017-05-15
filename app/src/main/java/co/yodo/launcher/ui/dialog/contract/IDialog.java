package co.yodo.launcher.ui.dialog.contract;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import co.yodo.launcher.R;

/**
 * Created by hei on 16/06/16.
 * implements the Dialog abstract class
 */
public abstract class IDialog {
    /** Dialog to be build */
    private final AlertDialog dialog;

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    protected IDialog(DialogBuilder builder) {
        dialog = builder.dialog;
        dialog.show();
    }

    /**
     * Show the inner dialog
     */
    public void show() {
        dialog.show();
    }

    /**
     * Abstract class for the Dialog Builders
     */
    protected static abstract class DialogBuilder {
        /** Context object */
        protected final Context context;

        /** Dialog to be build */
        protected final AlertDialog dialog;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder(Context context, int layout, int title) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(R.mipmap.icon);
            builder.setTitle(title);
            builder.setView(layout);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.text_ok, null);
            this.context = context;
            this.dialog = builder.show();
        }

        /**
         * Builds the IDialog
         * @return an IDialog
         */
        public abstract IDialog build();
    }

}