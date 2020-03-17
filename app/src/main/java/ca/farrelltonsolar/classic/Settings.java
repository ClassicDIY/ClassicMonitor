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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CheckBoxPreference uploadToPVOutput;
    private CheckBoxPreference useFahrenheit;
    private CheckBoxPreference autoDetectClassics;
    private CheckBoxPreference showPopupMessages;
    private CheckBoxPreference systemViewEnabled;
    private EditTextPreference _SID;
    private EditTextPreference _APIKey;
    private ListPreference _mProtocol;
    private EditTextPreference _brokerHost;
    private EditTextPreference _mqttPort;
    private EditTextPreference _mqttUser;
    private EditTextPreference _mqttPassword;
    private EditTextPreference _mqttRootTopic;
    private boolean _hasChanged = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.settings_main);

        final ImageButton help = (ImageButton) findViewById(R.id.SettingsHelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                String helpContext = String.format("http://graham22.github.io/Classic/classicmonitor/help_%s.html#Settings", MonitorApplication.getLanguage());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpContext)));
                return;
            }
        });
        final Button Cancel = (Button) findViewById(R.id.Cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                Settings.this.finish();
            }
        });
        _hasChanged = false;
        final Button Apply = (Button) findViewById(R.id.Apply);
        Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                CONNECTION_TYPE mqtt = CONNECTION_TYPE.valueOf(_mProtocol.getValue());
                MonitorApplication.chargeControllers().setConnectionType(mqtt);
                MonitorApplication.chargeControllers().setMqttBrokerHost(_brokerHost.getText());
                MonitorApplication.chargeControllers().setMqttPort(Integer.parseInt(_mqttPort.getText()));
                MonitorApplication.chargeControllers().setMqttUser(_mqttUser.getText());
                MonitorApplication.chargeControllers().setMqttPassword(_mqttPassword.getText());
                MonitorApplication.chargeControllers().setMqttRootTopic(_mqttRootTopic.getText());
                MonitorApplication.chargeControllers().setAPIKey(_APIKey.getText());
                MonitorApplication.chargeControllers().setFahrenheit(useFahrenheit.isChecked());
                MonitorApplication.chargeControllers().setAutoDetectClassic(autoDetectClassics.isChecked());
                MonitorApplication.chargeControllers().setShowPopupMessages(showPopupMessages.isChecked());
                MonitorApplication.chargeControllers().setUploadToPVOutput(uploadToPVOutput.isChecked());
                MonitorApplication.chargeControllers().setSystemViewEnabled(systemViewEnabled.isChecked());
                PVOutputSetting setting = MonitorApplication.chargeControllers().getPVOutputSetting();
                if (setting != null) {
                    setting.setSID(_SID.getText());
                }
                Intent i =new Intent();
                i.putExtra("hasChanged", _hasChanged);
                setResult(RESULT_OK, i);
                Settings.this.finish();
            }
        });

        try {
            uploadToPVOutput = (CheckBoxPreference) findPreference(Constants.UploadToPVOutput);
            useFahrenheit = (CheckBoxPreference) findPreference(Constants.UseFahrenheit);
            autoDetectClassics = (CheckBoxPreference) findPreference(Constants.AutoDetectClassic);
            showPopupMessages = (CheckBoxPreference) findPreference(Constants.ShowPopupMessages);
            systemViewEnabled = (CheckBoxPreference) findPreference(Constants.SystemViewEnabled);
            _SID = (EditTextPreference) findPreference(Constants.SID);
            _APIKey = (EditTextPreference) findPreference(Constants.APIKey);
            _mProtocol = (ListPreference)getPreferenceScreen().findPreference("protocol");
            _brokerHost = (EditTextPreference) findPreference("brokerHost");
            _mqttPort = (EditTextPreference) findPreference("mqttPort");
            _mqttUser = (EditTextPreference) findPreference("mqttUser");
            _mqttPassword = (EditTextPreference) findPreference("mqttPassword");
            _mqttRootTopic = (EditTextPreference) findPreference("mqttRootTopic");
            useFahrenheit.setChecked(MonitorApplication.chargeControllers().useFahrenheit());
            autoDetectClassics.setChecked(MonitorApplication.chargeControllers().autoDetectClassic());
            showPopupMessages.setChecked(MonitorApplication.chargeControllers().showPopupMessages());
            systemViewEnabled.setEnabled(MonitorApplication.chargeControllers().count() > 1);
            systemViewEnabled.setChecked(MonitorApplication.chargeControllers().systemViewEnabled());
            uploadToPVOutput.setChecked(MonitorApplication.chargeControllers().uploadToPVOutput());

            uploadToPVOutput.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isEnabled = ((Boolean) newValue).booleanValue();
                    UploadToPVOutputEnabled(isEnabled);
                    return true;
                }
            });
            UploadToPVOutputEnabled(uploadToPVOutput.isChecked());

            _APIKey.setSummary(MonitorApplication.chargeControllers().aPIKey());
            _APIKey.setText(MonitorApplication.chargeControllers().aPIKey());
            PVOutputSetting setting = MonitorApplication.chargeControllers().getPVOutputSetting();
            if (setting != null) {
                _SID.setSummary(setting.getSID());
                _SID.setText(setting.getSID());
            }
            Preference button = (Preference) findPreference("ResetLogs");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    MonitorApplication.chargeControllers().resetPVOutputLogs();
                    return true;
                }
            });

            CharSequence[] entries = new CharSequence[]{"Modbus", "MQTT"};
            CharSequence[] entryValues = new CharSequence[]{ CONNECTION_TYPE.MODBUS.name(), CONNECTION_TYPE.MQTT.name() };
            _mProtocol.setEntries(entries);
            _mProtocol.setEntryValues(entryValues);
            _mProtocol.setValueIndex(MonitorApplication.chargeControllers().getConnectionType().ordinal());
            _mProtocol.setSummary(_mProtocol.getEntry().toString());
            _brokerHost.setSummary(MonitorApplication.chargeControllers().mqttBrokerHost());
            _brokerHost.setText(MonitorApplication.chargeControllers().mqttBrokerHost());
            _mqttPort.setSummary( Integer.toString(MonitorApplication.chargeControllers().mqttPort()));
            _mqttPort.setText( Integer.toString(MonitorApplication.chargeControllers().mqttPort()));
            _mqttUser.setSummary(MonitorApplication.chargeControllers().mqttUser());
            _mqttUser.setText(MonitorApplication.chargeControllers().mqttUser());
            _mqttPassword.setSummary(MonitorApplication.chargeControllers().mqttPassword());
            _mqttPassword.setText(MonitorApplication.chargeControllers().mqttPassword());
            _mqttRootTopic.setSummary(MonitorApplication.chargeControllers().mqttRootTopic());
            _mqttRootTopic.setText(MonitorApplication.chargeControllers().mqttRootTopic());
            MQTTEnabled();
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("settings failed ex: %s", ex));
        }

    }

    private void UploadToPVOutputEnabled(boolean isEnabled) {
        _SID.setEnabled(isEnabled);
        _APIKey.setEnabled(isEnabled);
    }

    private void MQTTEnabled() {
        boolean isEnabled = _mProtocol.getValue().compareTo(CONNECTION_TYPE.MODBUS.name()) != 0;
        _brokerHost.setEnabled(isEnabled);
        _mqttPort.setEnabled(isEnabled);
        _mqttUser.setEnabled(isEnabled);
        _mqttPassword.setEnabled(isEnabled);
        _mqttRootTopic.setEnabled(isEnabled);
        isEnabled = _mProtocol.getValue().compareTo(CONNECTION_TYPE.MQTT.name()) != 0;
        autoDetectClassics.setEnabled(isEnabled);
        autoDetectClassics.setChecked(isEnabled);
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
        _hasChanged = true;
        updatePreferences(findPreference(key));
        if (key.equals("protocol")) {
            _mProtocol.setSummary(_mProtocol.getEntry().toString());
            MQTTEnabled();
        }
    }
}
