package co.yodo.launcher.ui.option;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.option.contract.IOption;
import co.yodo.restapi.network.YodoRequest;

/**
 * Created by hei on 22/06/16.
 * Implements the About Option of the MainActivity
 */
public class AboutOption extends IOption {
    /** Data of the about */
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;
    private final String mMessage;
    private final String mEmail;
    private final View mLayout;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public AboutOption( Activity activity ) {
        super( activity );
        // Data
        this.mHardwareToken = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle = this.mActivity.getString( R.string.about );
        this.mMessage =
                this.mActivity.getString( R.string.imei )    + " " +
                PrefUtils.getHardwareToken( this.mActivity ) + "\n" +
                this.mActivity.getString( R.string.label_currency )    + " " +
                PrefUtils.getMerchantCurrency( this.mActivity ) + "\n" +
                this.mActivity.getString( R.string.version_label ) + " " +
                this.mActivity.getString( R.string.version_value ) + "/" +
                YodoRequest.getSwitch() + "\n\n" +
                this.mActivity.getString( R.string.about_message );
        this.mEmail = this.mActivity.getString( R.string.about_email );

        // Gets and sets the dialog layout
        LayoutInflater inflater = (LayoutInflater) this.mActivity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.mLayout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this.mActivity ), false );
        setupLayout( this.mLayout );
    }

    /**
     * Prepares a layout for the About dialog
     * @param layout The layout to be prepared
     */
    private void setupLayout( View layout ) {
        // GUI controllers of the dialog
        TextView emailView = (TextView) layout.findViewById( R.id.emailView );
        TextView messageView = (TextView) layout.findViewById( R.id.messageView );
        SpannableString ssEmail = new SpannableString( this.mEmail );

        // Set text to the controllers
        ssEmail.setSpan( new UnderlineSpan(), 0, ssEmail.length(), 0 );
        emailView.setText( ssEmail );
        messageView.setText( this.mMessage  );

        // Create the onClick listener
        emailView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_SEND );
                String[] recipients = { mEmail };
                intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                intent.putExtra( Intent.EXTRA_SUBJECT, mHardwareToken );
                intent.setType( "text/html" );
                mActivity.startActivity( Intent.createChooser( intent, "Send Email" ) );
            }
        });
    }

    @Override
    public void execute() {
        // Generate the AlertDialog
        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                this.mLayout
        );
    }
}
