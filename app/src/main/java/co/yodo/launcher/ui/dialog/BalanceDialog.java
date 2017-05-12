package co.yodo.launcher.ui.dialog;

import android.content.Context;
import android.widget.TextView;

import co.yodo.launcher.R;
import co.yodo.launcher.utils.FormatUtils;
import co.yodo.launcher.ui.dialog.contract.IDialog;

/**
 * Created by hei on 21/06/16.
 * builds a receipt dialog from data
 */
public class BalanceDialog extends IDialog {
    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    private BalanceDialog(DialogBuilder builder) {
        super(builder);
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private TextView tvTodayCredits;
        private TextView tvTodayDebits;
        private TextView tvTodayBalance;
        private TextView tvHistoryCredits;
        private TextView tvHistoryDebits;
        private TextView tvHistoryBalance;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         */
        public Builder(Context context) {
            super(context, R.layout.dialog_balance, R.string.text_balance_title);

            // Controllers
            this.tvTodayCredits = (TextView) dialog.findViewById(R.id.tvTodayCredits);
            this.tvTodayDebits  = (TextView) dialog.findViewById(R.id.tvTodayDebits);
            this.tvTodayBalance = (TextView) dialog.findViewById(R.id.tvTodayBalance);

            this.tvHistoryCredits = (TextView) dialog.findViewById(R.id.tvHistoryCredits);
            this.tvHistoryDebits  = (TextView) dialog.findViewById(R.id.tvHistoryDebits);
            this.tvHistoryBalance = (TextView) dialog.findViewById(R.id.tvHistoryBalance);
        }

        public Builder todayCredits(String todayCredits) {
            this.tvTodayCredits.setText(FormatUtils.truncateDecimal(todayCredits));
            return this;
        }

        public Builder todayDebits(String todayDebits) {
            this.tvTodayDebits.setText(FormatUtils.truncateDecimal(todayDebits));
            return this;
        }

        public Builder todayBalance(String todayBalance) {
            this.tvTodayBalance.setText(FormatUtils.truncateDecimal(todayBalance));
            return this;
        }

        public Builder historyCredits(String historyCredits) {
            this.tvHistoryCredits.setText(FormatUtils.truncateDecimal(historyCredits));
            return this;
        }

        public Builder historyDebits(String historyDebits) {
            tvHistoryDebits.setText(FormatUtils.truncateDecimal(historyDebits));
            return this;
        }

        public Builder historyBalance(String historyBalance) {
            this.tvHistoryBalance.setText(FormatUtils.truncateDecimal(historyBalance));
            return this;
        }

        @Override
        public BalanceDialog build() {
            return new BalanceDialog(this);
        }
    }
}
