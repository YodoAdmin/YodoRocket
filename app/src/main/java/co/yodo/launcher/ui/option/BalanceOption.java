package co.yodo.launcher.ui.option;

import android.view.View;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.HashMap;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.manager.PromotionManager;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.dialog.BalanceDialog;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.ToastMaster;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.builder.ServerRequest;
import co.yodo.restapi.network.model.ServerResponse;

/**
 * Created by hei on 21/06/16.
 * Implements the Balance Option of the Launcher
 */
public class BalanceOption extends IRequestOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final PromotionManager mPromotionManager;

    /** Response codes for the server requests */
    private static final int QRY_HBAL_REQ = 0x00; // History Balance
    private static final int QRY_TBAL_REQ = 0x01; // Today Balance

    /** Response params temporal */
    private HashMap<String, String> mTempData = null;

    /** PIP temporal */
    private String mTempPIP = null;

    /** Handles the promotions */
    private boolean isPublishing = false;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public BalanceOption( LauncherActivity activity, MessageHandler handlerMessages, PromotionManager promotionManager ) {
        super( activity, handlerMessages );

        // Request
        this.mPromotionManager = promotionManager;

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                mAlertDialog.dismiss();
                setTempPIP( etInput.getText().toString() );

                mProgressManager.createProgressDialog(
                        mActivity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );
                mRequestManager.setListener( BalanceOption.this );
                mRequestManager.requestQuery(
                        QRY_HBAL_REQ,
                        mHardwareToken,
                        mTempPIP,
                        ServerRequest.QueryRecord.HISTORY_BALANCE
                );
            }
        };

        mAlertDialog = AlertDialogHelper.create(
                mActivity,
                layout,
                buildOnClick( okClick )
        );
    }

    @Override
    public void execute() {
        mAlertDialog.show();
        clearGUI();
    }

    /**
     * Sets the temporary PIP to a string value
     * @param pip The String PIP
     */
    private void setTempPIP( String pip ) {
        this.mTempPIP = pip;
    }

    /**
     * Sets the temporary Data to a MapValue (response params)
     * @param response The ServerResponse params
     */
    private void setTempData( HashMap<String, String> response ) {
        this.mTempData = response;
    }

    @Override
    public void onPrepare() {
        if( PrefUtils.isAdvertising( this.mActivity ) ) {
            mPromotionManager.unpublish();
            isPublishing = true;
        }
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        mProgressManager.destroyProgressDialog();
        mRequestManager.setListener( (LauncherActivity) mActivity );

        // If it was publishing before the request
        if( isPublishing )
            mPromotionManager.publish();

        String code = response.getCode();
        String message = response.getMessage();

        switch( code ) {
            case ServerResponse.AUTHORIZED:

                switch( responseCode ) {
                    case QRY_HBAL_REQ:
                        if( response.getParams().size() == 1 ) {
                            ToastMaster.makeText( mActivity, R.string.no_balance, Toast.LENGTH_SHORT ).show();
                            return;
                        }
                        setTempData( response.getParams() );
                        mProgressManager.createProgressDialog(
                                mActivity,
                                ProgressDialogHelper.ProgressDialogType.NORMAL
                        );

                        mRequestManager.setListener( BalanceOption.this );
                        mRequestManager.requestQuery(
                                QRY_TBAL_REQ,
                                mHardwareToken,
                                mTempPIP,
                                ServerRequest.QueryRecord.TODAY_BALANCE
                        );
                        break;

                    case QRY_TBAL_REQ:
                        BigDecimal todayBalance = BigDecimal.ZERO;
                        BigDecimal todayCredits = new BigDecimal( response.getParam( ServerResponse.CREDIT ) );
                        BigDecimal todayDebits = new BigDecimal( response.getParam( ServerResponse.DEBIT ) );

                        todayBalance = todayBalance
                                .add( todayCredits )
                                .subtract( todayDebits );

                        BigDecimal historyBalance = BigDecimal.ZERO;
                        BigDecimal historyCredits = new BigDecimal( mTempData.get( ServerResponse.CREDIT ) );
                        BigDecimal historyDebits = new BigDecimal( mTempData.get( ServerResponse.DEBIT ) );

                        historyBalance = historyBalance
                                .add( historyCredits )
                                .subtract( historyDebits );

                        new BalanceDialog.Builder( mActivity )
                                .todayCredits( todayCredits.toString() )
                                .todayDebits( todayDebits.toString() )
                                .todayBalance( todayBalance.toString() )
                                .historyCredits( historyCredits.toString() )
                                .historyDebits( historyDebits.toString() )
                                .historyBalance( historyBalance.toString() )
                                .build();
                        setTempData( null );
                        break;
                }
                break;

            case ServerResponse.ERROR_FAILED:
                message = mActivity.getString( R.string.message_incorrect_pip );
                MessageHandler.sendMessage( mHandlerMessages, code, message );
                setTempData( null );
                break;

            default:
                MessageHandler.sendMessage( mHandlerMessages, code, message );
                setTempData( null );
                break;
        }

        setTempPIP( null );
    }
}
