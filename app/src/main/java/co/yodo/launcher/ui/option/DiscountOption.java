package co.yodo.launcher.ui.option;

import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;

import java.util.Locale;

import co.yodo.launcher.R;
import co.yodo.launcher.business.manager.PromotionManager;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.helper.ProgressDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import co.yodo.launcher.utils.ErrorUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.AuthMerchDevicePipRequest;

/**
 * Created by hei on 22/06/16.
 * Implements the Discount Option of the Launcher
 */
public class DiscountOption extends IRequestOption {
    /** Elements for the request */
    private final PromotionManager promotionManager;

    /** Handles the promotions */
    private boolean isPublishing = false;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DiscountOption(final BaseActivity activity, final PromotionManager promotionManager) {
        super(activity);

        // Get promotion manager
        this.promotionManager = promotionManager;

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressManager.create(
                        activity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );

                final String pip = etInput.getText().toString();
                YodoApi.execute(
                        new AuthMerchDevicePipRequest(pip),
                        new RequestCallback() {
                            @Override
                            public void onPrepare() {
                                if (PrefUtils.isAdvertising(activity)) {
                                    promotionManager.unpublish();
                                    isPublishing = true;
                                }
                            }

                            @Override
                            public void onResponse(ServerResponse response) {
                                progressManager.destroy();
                                // If it was publishing before the request
                                if (isPublishing) {
                                    isPublishing = false;
                                    promotionManager.publish();
                                }

                                if (response.getCode().equals(ServerResponse.AUTHORIZED)) {
                                    alertDialog.dismiss();
                                    final String title = String.format(
                                            Locale.getDefault(),
                                            "%s (%%)", activity.getString(R.string.text_option_discount)
                                    );
                                    final EditText inputBox = new EditText(activity);
                                    inputBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                                    InputFilter filter = new InputFilter() {
                                        final int maxDigitsBeforeDecimalPoint = 2;
                                        final int maxDigitsAfterDecimalPoint = 2;

                                        @Override
                                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                            StringBuilder builder = new StringBuilder(dest);
                                            builder.replace(dstart, dend, source.subSequence(start, end).toString());
                                            if (!builder.toString().matches(
                                                    "(([" + (maxDigitsBeforeDecimalPoint - 1) + "-9])([0-9]?)?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

                                            )) {
                                                if (source.length() == 0)
                                                    return dest.subSequence(dstart, dend);
                                                return "";
                                            }

                                            return null;
                                        }
                                    };
                                    inputBox.setFilters(new InputFilter[]{ filter });
                                    inputBox.setText(String.valueOf(PrefUtils.getDiscount(activity)));

                                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            PrefUtils.saveDiscount(activity, inputBox.getText().toString());
                                            activity.updateUI();
                                        }
                                    };

                                    AlertDialogHelper.create(
                                            activity,
                                            title,
                                            inputBox,
                                            onClick
                                    );
                                } else {
                                    progressManager.destroy();
                                    tilPip.setError(activity.getString(R.string.error_mip));
                                }
                            }

                            @Override
                            public void onError(Throwable error) {
                                alertDialog.dismiss();
                                progressManager.destroy();
                                ErrorUtils.handleApiError(activity, error, false);

                                // If it was publishing before the request
                                if (isPublishing) {
                                    isPublishing = false;
                                    promotionManager.publish();
                                }
                            }
                        }
                );
            }
        };

        alertDialog = AlertDialogHelper.create(
                activity,
                layout,
                buildOnClick(okClick)
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }
}
