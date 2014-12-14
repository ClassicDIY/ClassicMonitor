package ca.farrelltonsolar.classic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private IPAddressPreference _IPAddressPreference;
    private EditTextPreference _PortPreference;
    private CheckBoxPreference _autoScan;
    private IPAddressPreference _startScan;
    private EditTextPreference _countScan;
    private CheckBoxPreference _uploadToPVOutput;
    private EditTextPreference _SID;
    private EditTextPreference _APIKey;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.settings_main);

        final Button Cancel = (Button) findViewById(R.id.Cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), MonitorActivity.class));
            }
        });
        final Button Apply = (Button) findViewById(R.id.Apply);
        Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), MonitorActivity.class));
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        try {
            _PortPreference = (EditTextPreference) findPreference(Constants.PORT_PREFERENCE);
            _IPAddressPreference = (IPAddressPreference) findPreference(Constants.IP_ADDRESS_PREFERENCE);
            _autoScan = (CheckBoxPreference) findPreference(Constants.SUBNET_SCAN_PREFERENCE);
            _startScan = (IPAddressPreference) findPreference(Constants.START_IP_PREFERENCE);
            _countScan = (EditTextPreference) findPreference(Constants.END_IP_COUNT_PREFERENCE);
            ScanEnabled(_autoScan.isChecked());
            _uploadToPVOutput = (CheckBoxPreference) findPreference(Constants.UploadToPVOutput);
            _SID = (EditTextPreference) findPreference(Constants.SID);
            _APIKey = (EditTextPreference) findPreference(Constants.APIKey);
            UploadToPVOutputEnabled(_uploadToPVOutput.isChecked());
            _autoScan.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = ((Boolean) newValue).booleanValue();
                    ScanEnabled(isEnabled);
                    return true;
                }
            });
            _uploadToPVOutput.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = ((Boolean) newValue).booleanValue();
                    UploadToPVOutputEnabled(isEnabled);
                    return true;
                }
            });
            _startScan.setSummary(settings.getString(Constants.START_IP_PREFERENCE, ""));
            _countScan.setSummary(settings.getString(Constants.END_IP_COUNT_PREFERENCE, "255"));
            _IPAddressPreference.setSummary(settings.getString(Constants.IP_ADDRESS_PREFERENCE, "Static IP Address of the Classic"));
            _PortPreference.setSummary(settings.getString(Constants.PORT_PREFERENCE, "502"));
            _SID.setSummary(settings.getString(Constants.SID, ""));
            _APIKey.setSummary(settings.getString(Constants.APIKey, "255"));
            Preference button = (Preference) findPreference("ResetLogs");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    LogSaver.ResetLogs();
                    return true;
                }
            });

        } catch (Exception e) {
            settings.edit().clear().commit();
            e.printStackTrace();
        }
        if (!screenIsLarge()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void UploadToPVOutputEnabled(boolean isEnabled) {
        _SID.setEnabled(isEnabled);
        _APIKey.setEnabled(isEnabled);
    }

    private void ScanEnabled(boolean isEnabled) {
        _startScan.setEnabled(isEnabled);
        _countScan.setEnabled(isEnabled);
        _IPAddressPreference.setEnabled(!isEnabled);
    }

    private boolean screenIsLarge() {
        int screenMask = getResources().getConfiguration().screenLayout;
        return (screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE || (screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

        if (key.equals(Constants.IP_ADDRESS_PREFERENCE)) {
            _IPAddressPreference.setSummary(settings.getString(key, ""));
        } else if (key.equals(Constants.PORT_PREFERENCE)) {
            _PortPreference.setSummary(settings.getString(key, "502"));
        } else if (key.equals(Constants.START_IP_PREFERENCE)) {
            _startScan.setSummary(settings.getString(key, ""));
        } else if (key.equals(Constants.END_IP_COUNT_PREFERENCE)) {
            Integer count = 255;
            String countString = settings.getString(key, "");
            if (countString.length() > 0) {
                count = Integer.valueOf(countString);
                if (count < 0 || count > 255) {
                    count = 255;
                }
            }
            _countScan.setSummary(String.valueOf(count));
        }
    }
}
