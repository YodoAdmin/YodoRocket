package co.yodo.launcher.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import co.yodo.launcher.ui.scanner.factory.QRScannerFactory;
import co.yodo.launcher.utils.PrefUtils;

/**
 * Created by hei on 26/06/16.
 * handles the items for the scanner
 */
public class ScannerAdapter extends ArrayAdapter<QRScannerFactory.SupportedScanner> {
    /** Context object */
    private Context ac;

    public ScannerAdapter(Context context, int resource, QRScannerFactory.SupportedScanner[] values) {
        super(context, resource, values);
        this.ac = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        QRScannerFactory.SupportedScanner scanner = getItem(position);
        TextView textView = (TextView) super.getView(position, convertView, parent);
        if (scanner != null) {
            textView.setText(ac.getString(scanner.getValue()));
        }

        return textView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        QRScannerFactory.SupportedScanner scanner = getItem(position);
        TextView textView = (TextView) super.getView(position, convertView, parent);
        if (scanner != null) {
            textView.setText(ac.getString(scanner.getValue()));
            textView.setTextColor(Color.WHITE);
        }

        return textView;
    }

    /**
     * Initializes the ic_spinner for the scanners
     * @param context The application context
     * @param sScannerSelector The spinner
     */
    public static void initializeScannerSpinner(final Context context, Spinner sScannerSelector) {
        // Add item listener to the ic_spinner
        sScannerSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                PrefUtils.saveScanner(context, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Create the adapter for the supported qr scanners
        ArrayAdapter<QRScannerFactory.SupportedScanner> adapter = new ScannerAdapter(
                context,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanner.values()
        );

        // Set the current scanner
        sScannerSelector.setAdapter(adapter);
        sScannerSelector.setSelection(PrefUtils.getScanner(context));
    }
}
