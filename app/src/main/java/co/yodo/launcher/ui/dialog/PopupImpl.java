package co.yodo.launcher.ui.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.dialog.contract.IPopup;
import co.yodo.launcher.utils.FormatUtils;
import co.yodo.launcher.utils.GuiUtils;

/**
 * Created by hei on 25/04/17.
 * Implements the basics for the Popup options
 */
public final class PopupImpl implements IPopup {
    private TextView textView;
    private ProgressBar progressBar;

    public PopupImpl(BaseActivity activity, TextView showView) {
        LinearLayout viewGroup = (LinearLayout) activity.findViewById(R.id.popup_window);
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Popup Window and GUI
        final PopupWindow popup = new PopupWindow(activity);
        View view = layoutInflater.inflate(R.layout.popup_window, viewGroup);

        textView = (TextView) view.findViewById(R.id.cashTenderText);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarPopUp);
        GuiUtils.setMerchantCurrencyIcon(activity, textView);

        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setContentView(view);

        // Setup the preview of the Total with ic_discount
        showView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                popup.showAtLocation(v, Gravity.CENTER, 0, 0);
                return false;
            }
        } );

        // Setup the dismiss of the preview
        showView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        popup.dismiss();
                        break;
                }
                return false;
            }
        } );
    }

    @Override
    public void load() {
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setData(String value) {
        textView.setText(FormatUtils.truncateDecimal(value));
        textView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
