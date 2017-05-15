package co.yodo.launcher.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.components.TransparentProgressDialog;

/**
 * Created by hei on 25/05/16.
 * Handles a progress dialog
 */
public class ProgressDialogHelper {
    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Progress dialog */
    private ProgressDialog progressDialog = null;
    private TransparentProgressDialog transProgressDialog = null;

    /**
     * Creates a new progress dialog on a respective activity
     * @param activity This context must be an activity (e.g. MainActivity.this)
     */
    public void create(Activity activity, ProgressDialogType type) {
        if (progressDialog != null || transProgressDialog != null) {
            destroy();
        }

        switch (type) {
            case NORMAL:
                progressDialog = new ProgressDialog(activity, R.style.TransparentProgressDialog);
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setContentView(R.layout.custom_progressdialog);
                break;

            case TRANSPARENT:
                transProgressDialog = new TransparentProgressDialog(activity, R.mipmap.ic_spinner);
                transProgressDialog.show();
                break;
        }
    }

    /**
     * Verifies if the dialog is being showed
     * @return A boolean that shows if the progress dialog is showing
     */
    public boolean isShowing() {
        return (progressDialog != null && progressDialog.isShowing()) ||
               (transProgressDialog != null && transProgressDialog.isShowing());
    }

    /**
     * Destroys the current progress dialog
     */
    public void destroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (transProgressDialog != null && transProgressDialog.isShowing()) {
            transProgressDialog.dismiss();
        }

        progressDialog = null;
        transProgressDialog = null;
    }
}
