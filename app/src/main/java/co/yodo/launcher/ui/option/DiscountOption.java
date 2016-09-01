package co.yodo.launcher.ui.option;

import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.manager.PromotionManager;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.component.ClearEditText;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.notification.MessageHandler;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.AuthenticateRequest;

/**
 * Created by hei on 22/06/16.
 * Implements the Discount Option of the Launcher
 */
public class DiscountOption extends IRequestOption implements ApiClient.RequestsListener {
    /** Elements for the request */
    private final PromotionManager mPromotionManager;

    /** Response codes for the server requests */
    private static final int AUTH_REQ = 0x00;

    /** Handles the promotions */
    private boolean isPublishing = false;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DiscountOption( LauncherActivity activity, MessageHandler handlerMessages, PromotionManager promotionManager ) {
        super( activity, handlerMessages );

        // Request
        this.mPromotionManager = promotionManager;

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                mAlertDialog.dismiss();
                final String pip = etInput.getText().toString();

                mProgressManager.createProgressDialog(
                        mActivity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );
                mRequestManager.setListener( DiscountOption.this );
                mRequestManager.invoke(
                        new AuthenticateRequest(
                                AUTH_REQ,
                                mHardwareToken,
                                pip
                        )
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

        String code    = response.getCode();
        String message = response.getMessage();

        switch( responseCode ) {
            case AUTH_REQ:

                switch( code ) {

                    case ServerResponse.AUTHORIZED:
                        final String title = mActivity.getString( R.string.text_set_discount ) + " (%)";
                        final EditText inputBox = new ClearEditText( mActivity );
                        inputBox.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL );

                        InputFilter filter = new InputFilter() {
                            final int maxDigitsBeforeDecimalPoint = 2;
                            final int maxDigitsAfterDecimalPoint = 2;

                            @Override
                            public CharSequence filter( CharSequence source, int start, int end, Spanned dest, int dstart, int dend ) {
                                StringBuilder builder = new StringBuilder( dest );
                                builder.replace( dstart, dend, source.subSequence( start, end ).toString() );
                                if( !builder.toString().matches(
                                        "(([" + ( maxDigitsBeforeDecimalPoint - 1 ) + "-9])([0-9]?)?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

                                ) ) {
                                    if( source.length() == 0 )
                                        return dest.subSequence( dstart, dend );
                                    return "";
                                }

                                return null;
                            }
                        };
                        inputBox.setFilters( new InputFilter[]{ filter } );
                        inputBox.setText( PrefUtils.getDiscount( mActivity ) );

                        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                            public void onClick( DialogInterface dialog, int item ) {
                                ((LauncherActivity) mActivity).discount( inputBox );
                            }
                        };

                        AlertDialogHelper.showAlertDialog(
                                mActivity,
                                title,
                                inputBox,
                                onClick
                        );
                        break;

                    case ServerResponse.ERROR_FAILED:
                        message = this.mActivity.getString( R.string.message_incorrect_pip );
                        MessageHandler.sendMessage( mHandlerMessages, code, message );
                        break;

                    default:
                        MessageHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }
                break;
        }
    }
}
