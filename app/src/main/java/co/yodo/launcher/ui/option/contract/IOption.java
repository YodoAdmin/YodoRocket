package co.yodo.launcher.ui.option.contract;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

/**
 * Created by hei on 14/06/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options
 */
public abstract class IOption {
    /** Main options elements */
    protected final Activity mActivity;
    protected AlertDialog mAlertDialog;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    protected IOption( Activity activity ) {
        this.mActivity = activity;
    }

    /**
     * Executes an option
     */
    public abstract void execute();
}
