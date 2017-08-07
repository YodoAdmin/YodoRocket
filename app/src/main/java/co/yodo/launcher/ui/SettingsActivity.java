package co.yodo.launcher.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import java.io.File;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AlertDialogHelper;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.utils.AppConfig;
import co.yodo.launcher.utils.BluetoothUtil;
import co.yodo.launcher.utils.GuiUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.utils.SystemUtils;
import co.yodo.restapi.YodoApi;

/**
 * Created by luis on 3/08/15.
 * Settings for the Rocket
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupGUI();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Configures the main GUI Controllers
     */
    @Override
    protected void setupGUI() {
        // Setup the toolbar
        GuiUtils.setActionBar(this);

        getFragmentManager().beginTransaction().replace(R.id.content, new PrefsFragmentInner()).commit();
    }

    @Override
    protected void updateData() {
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        /** Context object */
        private Context context;

        /** GUI Controllers */
        private EditTextPreference ETP_SPREF_USERNAME;
        private CheckBoxPreference CBP_ADVERTISING;
        private CheckBoxPreference CBP_LOCATING;
        private PreferenceCategory PC_PRINTER;
        private Preference P_RESET;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(AppConfig.SHARED_PREF_FILE);
            prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

            addPreferencesFromResource(R.xml.fragment_settings);

            setupGUI();
        }

        private void setupGUI() {
            // get the context
            context = getActivity();

            ETP_SPREF_USERNAME = (EditTextPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_CURRENT_BEACON);

            CBP_ADVERTISING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_ADVERTISING_SERVICE);

            CBP_LOCATING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_LOCATION_SERVICE);

            P_RESET = getPreferenceScreen().findPreference(AppConfig.SPREF_RESET);

            PC_PRINTER = (PreferenceCategory) findPreference("PrinterCategory");

            if (PrefUtils.isLegacy(context)) {
                ETP_SPREF_USERNAME.setEnabled(false);
                CBP_ADVERTISING.setEnabled(false);
            }

            if (PrefUtils.isLegacy(context) || !SystemUtils.hasLocationService(context)) {
                CBP_LOCATING.setEnabled(false);
            }

            PreferenceScreen screen = getPreferenceScreen();
            if (BluetoothUtil.getDevice() == null) {
                screen.removePreference(PC_PRINTER);
            }

            P_RESET.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PrefUtils.clearPrefConfig( context );
                            SystemUtils.deleteDir(new File(SystemUtils.RESOURCES_PATH));
                            YodoApi.clear();

                            // Start app from zero
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    };

                    new AlertDialog.Builder(context)
                        .setMessage(R.string.text_settings_reset_confirmation)
                        .setPositiveButton(R.string.text_ok, onClick)
                        .setNegativeButton(R.string.text_cancel, null)
                        .show();
                    return false;
                }
            });
        }

        private void updateStatus(String key) {
            if (key.equals(AppConfig.SPREF_CURRENT_LANGUAGE)) {
                getActivity().setResult(RESULT_FIRST_USER);
                getActivity().finish();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // register listener to update when value change
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            setAllSummaries();
        }

        @Override
        public void onPause() {
            super.onPause();
            // unregister listener
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        private void setAllSummaries() {
            ETP_SPREF_USERNAME.setSummary(PrefUtils.getBeaconName(context));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setAllSummaries();
            updateStatus(key);
        }
    }
}
