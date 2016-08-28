package co.yodo.launcher.ui.option.contract;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import javax.inject.Inject;

import co.yodo.launcher.R;
import co.yodo.launcher.YodoApplication;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.restapi.network.YodoRequest;

/**
 * Created by hei on 04/08/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options including requests
 */
public abstract class IRequestOption extends IOption {
    /** User identifier */
    protected final String mHardwareToken;

    /** Handler for messages */
    protected MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    protected YodoRequest mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    protected ProgressDialogHelper mProgressManager;

    /** GUI elements */
    protected EditText etInput;
    protected CheckBox cbShowPIP;
    private TextInputLayout tilPip;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     * @param handlerMessages The Messages handler
     */
    protected IRequestOption( Activity activity, MessageHandler handlerMessages ) {
        super( activity );

        // Gets request's data
        this.mHardwareToken = PrefUtils.getHardwareToken( mActivity );

        // Injection
        YodoApplication.getComponent().inject( this );

        // Gets the messages handler
        this.mHandlerMessages = handlerMessages;
    }

    /**
     * Creates the layout for the input pip
     * @return View The layout
     */
    protected View buildLayout() {
        // Dialog
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_with_pip, new LinearLayout( mActivity ), false );

        // GUI setup
        etInput = (EditText) layout.findViewById( R.id.cetPIP );
        cbShowPIP = (CheckBox) layout.findViewById( R.id.cbOption );
        tilPip = (TextInputLayout) etInput.getParent();

        cbShowPIP.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                GUIUtils.showPassword( cbShowPIP, etInput );
            }
        } );

        return layout;
    }

    /**
     * Builds a listener for the positive button
     * @param onPositive The new procedure for the positive button
     * @return The listener
     */
    protected DialogInterface.OnShowListener buildOnClick( final View.OnClickListener onPositive ) {
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow( DialogInterface dialog ) {
                // Get the AlertDialog and the positive Button
                mAlertDialog = AlertDialog.class.cast( dialog );
                final Button button = mAlertDialog.getButton( AlertDialog.BUTTON_POSITIVE );

                // Sets the action for the positive Button
                button.setOnClickListener( onPositive );
            }
        };
    }

    /**
     * Clears the layout
     */
    protected void clearGUI() {
        etInput.setText( "" );
        tilPip.setErrorEnabled( false );
        tilPip.setError( null );
        etInput.requestFocus();
        cbShowPIP.setChecked( false );
        GUIUtils.showPassword( cbShowPIP, etInput );
    }
}
