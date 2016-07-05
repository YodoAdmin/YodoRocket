package co.yodo.launcher.ui.notification;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.restapi.network.model.ServerResponse;

/**
 * Created by luis on 15/12/14.
 * Handler for messages
 */
public class YodoHandler extends Handler {
    /** Id for Messages */
    public static final int INIT_ERROR   = 1;
    public static final int SERVER_ERROR = 2;

    /** Id for the content */
    public static final String CODE    = "code";
    public static final String MESSAGE = "message";

    private final WeakReference<Activity> wMain;

    public YodoHandler( Activity main ) {
        super();
        this.wMain = new WeakReference<>(main);
    }

    @Override
    public void handleMessage( Message msg ) {
        super.handleMessage( msg );
        final Activity main = wMain.get();

        // message arrived after activity death or while dying
        if( main == null || main.isFinishing() )
            return;

        DialogInterface.OnClickListener clickListener = null;

        if( msg.what == INIT_ERROR ) {
            clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    main.finish();
                }
            };
        }

        GUIUtils.errorSound( main );
        String code     = msg.getData().getString( CODE, ServerResponse.ERROR_FAILED );
        String response = msg.getData().getString( MESSAGE );

        switch( code ) {
            case ServerResponse.ERROR_DUP_AUTH:
                response = main.getString( R.string.message_error_amount_exceed );
                break;

            case ServerResponse.ERROR_INSUFF_FUNDS:
                response = main.getString( R.string.message_error_insufficient_funds );
                break;
        }

        AlertDialogHelper.showAlertDialog(
                main,
                code,
                response,
                clickListener
        );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( int messageType, YodoHandler handlerMessages, String title, String message ) {
        Message msg = new Message();
        msg.what = messageType;

        Bundle bundle = new Bundle();
        bundle.putString( YodoHandler.CODE, title );
        bundle.putString( YodoHandler.MESSAGE, message );
        msg.setData( bundle );

        handlerMessages.sendMessage( msg );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( YodoHandler handlerMessages, String title, String message ) {
        sendMessage( YodoHandler.SERVER_ERROR, handlerMessages, title, message );
    }
}
