package co.yodo.launcher.ui.option;

import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.widget.EditText;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.component.ClearEditText;
import co.yodo.launcher.ui.notification.AlertDialogHelper;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.launcher.ui.notification.YodoHandler;
import co.yodo.launcher.ui.option.contract.IOption;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.model.ServerResponse;

/**
 * Created by hei on 22/06/16.
 * Implements the Discount Option of the Launcher
 */
public class DiscountOption extends IOption implements YodoRequest.RESTListener {
    /** Elements for the request */
    private final YodoRequest mRequestManager;
    private final YodoHandler mHandlerMessages;
    private final String mHardwareToken;

    /** Elements for the AlertDialog */
    private final String mTitle;

    /** Response codes for the server requests */
    private static final int AUTH_REQ = 0x00;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DiscountOption( LauncherActivity activity,  YodoRequest requestManager, YodoHandler handlerMessages ) {
        super( activity );
        // Request
        this.mRequestManager  = requestManager;
        this.mHandlerMessages = handlerMessages;
        this.mHardwareToken   = PrefUtils.getHardwareToken( this.mActivity );

        // AlertDialog
        this.mTitle = this.mActivity.getString( R.string.input_pip );
    }

    @Override
    public void execute() {
        etInput.setText( "" );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int item ) {
                GUIUtils.hideSoftKeyboard( mActivity );
                final String pip = etInput.getText().toString();

                ProgressDialogHelper.getInstance().createProgressDialog(
                        mActivity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );

                mRequestManager.setListener( DiscountOption.this );
                mRequestManager.requestMerchAuth(
                        AUTH_REQ,
                        mHardwareToken,
                        pip
                );
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                this.mTitle,
                this.etInput,
                false,
                onClick
        );
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Set listener to the principal activity
        ProgressDialogHelper.getInstance().destroyProgressDialog();
        mRequestManager.setListener( ( (LauncherActivity) this.mActivity ) );

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
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;

                    default:
                        YodoHandler.sendMessage( mHandlerMessages, code, message );
                        break;
                }
                break;
        }
    }
}
