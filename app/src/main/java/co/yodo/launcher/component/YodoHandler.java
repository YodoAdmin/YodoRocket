package co.yodo.launcher.component;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;

/**
 * Created by luis on 15/12/14.
 * Handler for messages
 */
public class YodoHandler extends Handler {
    /** Id for Messages */
    public static final int NO_INTERNET   = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int SERVER_ERROR  = 2;
    public static final int SUCCESS       = 3;

    /** Id for the content */
    public static final String CODE    = "code";
    public static final String MESSAGE = "message";

    private final WeakReference<Activity> wMain;

    public YodoHandler( Activity main ) {
        super();
        this.wMain = new WeakReference<>( main );
    }

    @Override
    public void handleMessage( Message msg ) {
        super.handleMessage( msg );
        Activity main = wMain.get();

        // message arrived after activity death
        if( main == null )
            return;

        if( msg.what == GENERAL_ERROR ) {
            ToastMaster.makeText( main, R.string.error, Toast.LENGTH_LONG ).show();
        }
        else if( msg.what == NO_INTERNET ) {
            ToastMaster.makeText( main, R.string.message_no_internet, Toast.LENGTH_LONG ).show();
        }
        else if( msg.what == SERVER_ERROR ) {
            String code     = msg.getData().getString( CODE );
            String response = msg.getData().getString( MESSAGE );
            AlertDialogHelper.showAlertDialog( main, code, response, null );
        }
    }
}
