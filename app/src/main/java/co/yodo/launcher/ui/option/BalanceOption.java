package co.yodo.launcher.ui.option;

import android.view.View;

import java.math.BigDecimal;

import co.yodo.launcher.R;
import co.yodo.launcher.business.manager.PromotionManager;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.helper.ProgressDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.ui.dialog.BalanceDialog;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import co.yodo.launcher.utils.ErrorUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.Params;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.QueryHistoryBalanceRequest;
import co.yodo.restapi.network.requests.QueryTodayBalanceRequest;

/**
 * Created by hei on 21/06/16.
 * Implements the Balance Option of the Launcher
 */
public class BalanceOption extends IRequestOption {
    /** Elements for the request */
    private final PromotionManager promotionManager;

    /** Handles the promotions */
    private boolean isPublishing = false;

    /** PIP temporal */
    private String tempPip = null;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public BalanceOption(final BaseActivity activity, final PromotionManager promotionManager) {
        super(activity);

        // Get the promotion manager
        this.promotionManager = promotionManager;

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTempPIP(etInput.getText().toString());

                progressManager.create(
                        activity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );

                YodoApi.execute(
                        new QueryHistoryBalanceRequest(tempPip),
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
                                if (response.getCode().equals(ServerResponse.AUTHORIZED)) {
                                    alertDialog.dismiss();
                                    requestTodayBalance(response.getParams());
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
                                startPublishing();
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

    /**
     * Sets the temporary PIP to a string value
     * @param pip The String PIP
     */
    private void setTempPIP(String pip) {
        this.tempPip = pip;
    }

    /**
     * Starts publishing again if it was publishing
     * before the request
     */
    private void startPublishing() {
        if (isPublishing) {
            isPublishing = false;
            promotionManager.publish();
        }
    }

    /**
     * Requests the balance for the current day
     * @param totalParams The total balance, used to be displayed in dialog
     */
    private void requestTodayBalance(final Params totalParams) {
        YodoApi.execute(
                new QueryTodayBalanceRequest(tempPip),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse( ServerResponse response ) {
                        progressManager.destroy();

                        // Sets all the balance data in the dialog
                        BigDecimal todayBalance = BigDecimal.ZERO;
                        BigDecimal todayCredits = new BigDecimal(response.getParams().getCredit());
                        BigDecimal todayDebits = new BigDecimal(response.getParams().getDebit());

                        todayBalance = todayBalance
                                .add(todayCredits)
                                .subtract(todayDebits);

                        BigDecimal historyBalance = BigDecimal.ZERO;
                        BigDecimal historyCredits = new BigDecimal(totalParams.getCredit());
                        BigDecimal historyDebits = new BigDecimal(totalParams.getDebit());

                        historyBalance = historyBalance
                                .add(historyCredits)
                                .subtract(historyDebits);

                        new BalanceDialog.Builder(activity)
                                .todayCredits(todayCredits.toString())
                                .todayDebits(todayDebits.toString())
                                .todayBalance(todayBalance.toString())
                                .historyCredits(historyCredits.toString())
                                .historyDebits(historyDebits.toString())
                                .historyBalance(historyBalance.toString())
                                .build();

                        startPublishing();
                    }

                    @Override
                    public void onError(Throwable error) {
                        progressManager.destroy();
                        ErrorUtils.handleApiError(activity, error, false);
                        startPublishing();
                    }
                }
        );
    }
}
