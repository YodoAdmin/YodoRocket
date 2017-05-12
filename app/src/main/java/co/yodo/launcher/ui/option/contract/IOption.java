package co.yodo.launcher.ui.option.contract;

import android.support.v7.app.AlertDialog;

import co.yodo.launcher.ui.contract.BaseActivity;

/**
 * Created by hei on 14/06/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options
 */
public abstract class IOption {
    /** Main options elements */
    protected final BaseActivity activity;
    protected AlertDialog alertDialog;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    protected IOption(BaseActivity activity) {
        this.activity = activity;
    }

    /**
     * Executes an option
     */
    public abstract void execute();
}
