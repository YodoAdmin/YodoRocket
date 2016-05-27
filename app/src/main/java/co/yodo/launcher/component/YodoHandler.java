package co.yodo.launcher.component;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import co.yodo.launcher.ui.component.AlertDialogHelper;
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

        // message arrived after activity death
        if( main == null )
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

        String code     = msg.getData().getString( CODE, ServerResponse.ERROR_FAILED );
        String response = msg.getData().getString( MESSAGE );

        switch( code ) {
            default:
                AlertDialogHelper.showAlertDialog( main, code, response, clickListener );
        }
    }
}
