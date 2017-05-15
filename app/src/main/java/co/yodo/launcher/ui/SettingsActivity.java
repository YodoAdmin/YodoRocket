package co.yodo.launcher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import co.yodo.launcher.R;
import co.yodo.launcher.ui.contract.BaseActivity;
import co.yodo.launcher.utils.AppConfig;
import co.yodo.launcher.utils.BluetoothUtil;
import co.yodo.launcher.utils.GuiUtils;
import co.yodo.launcher.utils.PrefUtils;
import co.yodo.launcher.utils.SystemUtils;

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
        private Context c;

        /** GUI Controllers */
        private EditTextPreference ETP_SPREF_USERNAME;
        private CheckBoxPreference CBP_ADVERTISING;
        private CheckBoxPreference CBP_LOCATING;
        private PreferenceCategory PC_PRINTER;

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
            c = getActivity();

            ETP_SPREF_USERNAME = (EditTextPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_CURRENT_BEACON);

            CBP_ADVERTISING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_ADVERTISING_SERVICE);

            CBP_LOCATING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(AppConfig.SPREF_LOCATION_SERVICE);

            PC_PRINTER = (PreferenceCategory) findPreference("PrinterCategory");

            if (PrefUtils.isLegacy(c)) {
                ETP_SPREF_USERNAME.setEnabled(false);
                CBP_ADVERTISING.setEnabled(false);
            }

            if (PrefUtils.isLegacy(c) || !SystemUtils.hasLocationService(c)) {
                CBP_LOCATING.setEnabled(false);
            }

            PreferenceScreen screen = getPreferenceScreen();
            if (BluetoothUtil.getDevice() == null) {
                screen.removePreference(PC_PRINTER);
            }
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
            ETP_SPREF_USERNAME.setSummary(PrefUtils.getBeaconName(c));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setAllSummaries();
            updateStatus(key);
        }
    }
}
