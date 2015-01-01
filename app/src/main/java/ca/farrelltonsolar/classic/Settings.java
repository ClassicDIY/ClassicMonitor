/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

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
                Settings.this.finish();
            }
        });
        final Button Apply = (Button) findViewById(R.id.Apply);
        Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                MonitorApplication.chargeControllers().setAPIKey(_APIKey.getText());
                ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
                if (cc != null) {
                    cc.setUploadToPVOutput(_uploadToPVOutput.isChecked());
                    cc.setSID(_SID.getText());
                }
                Settings.this.finish();
            }
        });

        try {
            _uploadToPVOutput = (CheckBoxPreference) findPreference(Constants.UploadToPVOutput);

            _SID = (EditTextPreference) findPreference(Constants.SID);
            _APIKey = (EditTextPreference) findPreference(Constants.APIKey);
            UploadToPVOutputEnabled(_uploadToPVOutput.isChecked());

            _uploadToPVOutput.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = ((Boolean) newValue).booleanValue();
                    UploadToPVOutputEnabled(isEnabled);
                    return true;
                }
            });

            ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
            if (cc != null) {
                _uploadToPVOutput.setChecked(cc.uploadToPVOutput());
                _SID.setSummary(cc.getSID());
            }
            _APIKey.setSummary(MonitorApplication.chargeControllers().aPIKey());
            Preference button = (Preference) findPreference("ResetLogs");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    MonitorApplication.chargeControllers().resetPVOutputLogs();
                    return true;
                }
            });

        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("settings failed ex: %s", ex));
        }

    }



    private void UploadToPVOutputEnabled(boolean isEnabled) {
        _SID.setEnabled(isEnabled);
        _APIKey.setEnabled(isEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updatePreferences(Preference p) {
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferences(findPreference(key));
    }
}
