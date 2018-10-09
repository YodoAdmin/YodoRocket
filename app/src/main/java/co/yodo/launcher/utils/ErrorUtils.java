package co.yodo.launcher.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.io.IOException;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;

/**
 * Created by hei on 24/02/17.
 * Handle errors
 */
public class ErrorUtils {
    /**
     * Handle general errors
     * @param activity The activity
     * @param message The message id
     * @param close If the dialog should close the app
     */
    public static void handleError(final Activity activity, int message, boolean close) {
        DialogInterface.OnClickListener onClick;

        if (close) {
            onClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finish();
                }
            };
        } else {
            onClick = null;
        }

        AlertDialogHelper.create(
                activity,
                activity.getString(message),
                onClick
        );
    }

    /**
     * Handles errors from the requests
     * @param activity The activity that needs to handle the error
     * @param error The error object
     * @param close If the dialog should close the app
     */
    public static void handleApiError(final Activity activity, Throwable error, boolean close) {
        int message;
        if(error instanceof IOException) {
            // Network error
            message = R.string.error_network;
        } else if(error instanceof NullPointerException)  {
            // Server error
            message = R.string.error_server;
        } else {
            // Unknown error
            message = R.string.error_unknown;
        }

        handleError(activity, message, close);
    }

    /**
     * Handles an error over the edit
     * @param context The application context
     * @param editText The edit text with the information
     */
    public static void handleFieldError(Context context, TextInputEditText editText, int message) {
        // The shake animation for wrong inputs and layout for error
        Animation aShake = AnimationUtils.loadAnimation(context, R.anim.shake);
        TextInputLayout layout = (TextInputLayout) editText.getParent().getParent();

        // The action over the edit text
        editText.startAnimation(aShake);
        layout.setError(context.getString(message));
    }
}
