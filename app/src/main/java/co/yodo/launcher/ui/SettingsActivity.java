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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.yodo.launcher.R;
import co.yodo.launcher.helper.AppConfig;
import co.yodo.launcher.helper.BluetoothUtil;
import co.yodo.launcher.helper.GUIUtils;
import co.yodo.launcher.helper.PrefUtils;
import co.yodo.launcher.helper.SystemUtils;
import co.yodo.launcher.service.LocationService;

/**
 * Created by luis on 3/08/15.
 * Settings for the Rocket
 */
public class SettingsActivity extends AppCompatActivity {
    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

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
        private CheckBoxPreference CBP_ADVERTISING;
        private CheckBoxPreference CBP_LOCATING;
        private PreferenceCategory PC_PRINTER;

        @Override
        public void onCreate( final Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName( AppConfig.SHARED_PREF_FILE );
            prefMgr.setSharedPreferencesMode( MODE_PRIVATE );

            addPreferencesFromResource( R.xml.fragment_settings );

            setupGUI();
        }

        private void setupGUI() {
            // get the context
            c = getActivity();

            ETP_SPREF_USERNAME = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CURRENT_BEACON );

            CBP_ADVERTISING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADVERTISING_SERVICE );

            CBP_LOCATING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_LOCATION_SERVICE );

            PC_PRINTER = (PreferenceCategory) findPreference( "PrinterCategory" );

            if( PrefUtils.isLegacy( c ) ) {
                ETP_SPREF_USERNAME.setEnabled( false );
                CBP_ADVERTISING.setEnabled( false );
            }

            if( PrefUtils.isLegacy( c ) || !LocationService.permission( getActivity(), null ) ) {
                CBP_LOCATING.setEnabled( false );
            } else if( LocationService.permission( getActivity(), null ) && !SystemUtils.hasLocationService( c ) ) {
                CBP_LOCATING.setEnabled( false );
            }

            PreferenceScreen screen = getPreferenceScreen();
            if( BluetoothUtil.getDevice() == null ) {
                screen.removePreference( PC_PRINTER );
            }
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
