package yodo.co.yodolauncher.component;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import yodo.co.yodolauncher.R;

/**
 * Created by luis on 15/12/14.
 */
public class YodoHandler extends Handler {
    /** Id for Messages */
    public static final int NO_INTERNET   = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int UNKNOWN_ERROR = 2;
    public static final int SUCCESS       = 3;

    private final WeakReference<Activity> wMain;

    public YodoHandler(Activity main) {
        super();
        this.wMain = new WeakReference<>(main);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Activity main = wMain.get();

        // message arrived after activity death
        if(main == null)
            return;

        if(msg.what == GENERAL_ERROR) {
            ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
        }
        else if(msg.what == NO_INTERNET) {
            ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
        else if(msg.what == UNKNOWN_ERROR) {
            String response = msg.getData().getString("message");
            ToastMaster.makeText(main, response, Toast.LENGTH_LONG).show();
        }
    }
}
