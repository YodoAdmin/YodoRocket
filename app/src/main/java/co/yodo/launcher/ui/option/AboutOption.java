package co.yodo.launcher.ui.option;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import co.yodo.launcher.BuildConfig;
import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.option.contract.IOption;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.restapi.YodoApi;

/**
 * Created by hei on 22/06/16.
 * Implements the About Option of the MainActivity
 */
public class AboutOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public AboutOption(BaseActivity activity) {
        super(activity);

        // Gets and sets the dialog layout
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.dialog_about, new LinearLayout(this.activity), false);
        setupLayout(layout);
    }

    /**
     * Prepares a layout for the About dialog
     * @param layout The layout to be prepared
     */
    private void setupLayout(View layout) {
        // GUI controllers of the dialog
        TextView emailView = (TextView) layout.findViewById(R.id.emailView);
        TextView messageView = (TextView) layout.findViewById(R.id.messageView);

        // Get data
        final String identifier = YodoApi.getIdentifier();
        final String email = activity.getString(R.string.text_about_email);
        final String message = String.format(Locale.getDefault(), "%s %s\n%s %s\n%s %s/%s\n\n%s",
                activity.getString(R.string.text_imei), identifier,
                activity.getString(R.string.text_currency), PrefUtils.getMerchantCurrency(activity),
                activity.getString( R.string.text_version), BuildConfig.VERSION_NAME, YodoApi.getAlias(),
                activity.getString( R.string.text_about_message)
        );

        // Set text to the controllers
        SpannableString ssEmail = new SpannableString(email);
        ssEmail.setSpan(new UnderlineSpan(), 0, ssEmail.length(), 0);
        emailView.setText(ssEmail);
        messageView.setText(message);

        // Create the onClick listener
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] recipients = { email };
                intent.putExtra(Intent.EXTRA_EMAIL, recipients) ;
                intent.putExtra(Intent.EXTRA_SUBJECT, identifier);
                intent.setType("text/html");
                activity.startActivity(Intent.createChooser(
                        intent,
                        activity.getString(R.string.text_send_mail)
                ));
            }
        });

        // Generate the AlertDialog
        alertDialog = AlertDialogHelper.create(
                activity,
                R.string.text_tool_about,
                layout
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
    }
}
