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
    protected final AlertDialog mDialog;

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    protected IDialog( DialogBuilder builder ) {
        this.mDialog = builder.mDialog;
        this.mDialog.show();
    }

    /**
     * Show the inner dialog
     */
    public void show() {
        this.mDialog.show();
    }

    /**
     * Abstract class for the Dialog Builders
     */
    protected static abstract class DialogBuilder {
        /** Context object */
        protected final Context mContext;

        /** Dialog to be build */
        protected final AlertDialog mDialog;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         * @param layout The layout for the dialog
         */
        protected DialogBuilder( Context context, int layout, int title ) {
            this.mContext = context;
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(
                    this.mContext,
                    R.style.AppCompatAlertDialogStyle
            );
            mBuilder.setIcon( R.drawable.icon );
            mBuilder.setTitle( title );
            mBuilder.setView( layout );
            mBuilder.setCancelable( false );
            mBuilder.setPositiveButton( R.string.ok, null );
            this.mDialog = mBuilder.show();
        }

        /**
         * Builds the IDialog
         * @return an IDialog
         */
        public abstract IDialog build();
    }

}