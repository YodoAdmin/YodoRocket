package co.yodo.launcher.ui.notification;

import android.app.ProgressDialog;
import android.content.Context;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.component.TransparentProgressDialog;

/**
 * Created by hei on 25/05/16.
 * Handles a progress dialog
 */
public class ProgressDialogHelper {
    /** Singleton instance */
    private static ProgressDialogHelper instance = null;

    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Progress dialog */
    private ProgressDialog progressDialog;
    private TransparentProgressDialog transProgressDialog;

    /**
     * Private constructor needed for the singleton
     */
    private ProgressDialogHelper() {
    }

    /**
     * The initializer for the singleton
     * @return The instance
     */
    public static synchronized ProgressDialogHelper getInstance() {
        if( instance == null )
            instance = new ProgressDialogHelper();
        return instance;
    }

    /**
     * Creates a new progress dialog on a respective activity
     * @param context This context must be an activity (e.g. MainActivity.this)
     */
    public void createProgressDialog( Context context, ProgressDialogType type ) {
        if( progressDialog != null || transProgressDialog != null )
            throw new ExceptionInInitializerError( "There is already a progress dialog in front" );

        switch( type ) {
            case NORMAL:
                progressDialog = new ProgressDialog( context );
                progressDialog.setCancelable( false );
                progressDialog.show();
                progressDialog.setContentView( R.layout.custom_progressdialog );
                break;

            case TRANSPARENT:
                transProgressDialog = new TransparentProgressDialog( context, R.drawable.spinner );
                transProgressDialog.show();
                break;
        }
    }

    /**
     * Verifies if the dialog is being showed
     * @return A boolean that shows if the progress dialog is showing
     */
    public boolean isProgressDialogShowing() {
        return ( progressDialog != null && progressDialog.isShowing() ) ||
               ( transProgressDialog != null && transProgressDialog.isShowing() );
    }

    /**
     * Destroys the current progress dialog
     */
    public void destroyProgressDialog() {
        if( progressDialog != null && progressDialog.isShowing() ) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if( transProgressDialog != null && transProgressDialog.isShowing() ) {
            transProgressDialog.dismiss();
            transProgressDialog = null;
        }
    }
}
