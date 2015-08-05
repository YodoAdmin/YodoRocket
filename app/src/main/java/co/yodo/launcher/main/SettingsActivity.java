package co.yodo.launcher.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.AppUtils;

/**
 * Created by luis on 3/08/15.
 * Settings for the Rocket
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( SettingsActivity.this );
        setContentView( R.layout.activity_settings );

        setupGUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );

        setSupportActionBar( toolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        getFragmentManager().beginTransaction().replace( R.id.content, new PrefsFragmentInner() ).commit();
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Context c;

        private CheckBoxPreference
                ETP_ADVERTISING;

        private EditTextPreference
                ETP_SPREF_USERNAME;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName( AppConfig.SHARED_PREF_FILE );
            prefMgr.setSharedPreferencesMode( MODE_PRIVATE );

            addPreferencesFromResource( R.xml.fragment_settings);

            setupGUI();
        }

        private void setupGUI() {
            // get the context
            c = getActivity();

            ETP_SPREF_USERNAME = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CURRENT_BEACON );

            ETP_ADVERTISING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADVERTISING_SERVICE );

            if( !AppUtils.hasBluetooth() )
                ETP_ADVERTISING.setEnabled( false );
        }

        private void updateStatus(String key) {
            if( key.equals( AppConfig.SPREF_ADVERTISING_SERVICE ) )
                AppUtils.setupAdvertising( c, AppUtils.isAdvertisingServiceRunning( c ), true );
        }

        @Override
        public void onResume() {
            super.onResume();
            // register listener to update when value change
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
            setAllSummaries();
        }

        @Override
        public void onPause() {
            super.onPause();
            // unregister listener
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
        }

        private void setAllSummaries() {
            ETP_SPREF_USERNAME.setSummary( AppUtils.getBeaconName( c ) );
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setAllSummaries();
            updateStatus( key );
        }
    }
}
