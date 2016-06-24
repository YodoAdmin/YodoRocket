package co.yodo.launcher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.ui.notification.ToastMaster;

/**
 * Created by luis on 3/08/15.
 * Settings for the Rocket
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        GUIUtils.setLanguage( this );
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

    /**
     * Configures the main GUI Controllers
     */
    private void setupGUI() {
        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );

        // Setup the toolbar
        setSupportActionBar( toolbar );
        ActionBar actionbar = getSupportActionBar();
        if( actionbar != null )
            actionbar.setDisplayHomeAsUpEnabled( true );

        getFragmentManager().beginTransaction().replace( R.id.content, new PrefsFragmentInner() ).commit();
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        /** Context object */
        private Context c;

        /** GUI Controllers */
        private EditTextPreference ETP_SPREF_USERNAME;
        private CheckBoxPreference ETP_ADVERTISING;
        private CheckBoxPreference ETP_LOCATING;

        @Override
        public void onCreate( final Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );

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

            ETP_LOCATING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_LOCATION_SERVICE );

            if( PrefUtils.isLegacy( c ) )
                ETP_ADVERTISING.setEnabled( false );

            if( !SystemUtils.hasLocationService( c ) )
                ETP_LOCATING.setEnabled( false );
        }

        private void updateStatus( String key ) {
            if( key.equals( AppConfig.SPREF_CURRENT_LANGUAGE ) ) {
                getActivity().setResult( RESULT_FIRST_USER );
                getActivity().finish();
            }
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
            ETP_SPREF_USERNAME.setSummary( PrefUtils.getBeaconName( c ) );
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setAllSummaries();
            updateStatus( key );
        }
    }
}
