/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.SOCGauge;

/**
 * Created by Graham on 14/12/2014.
 */
public class StateOfChargeFragment extends GaugeFramentBase {

    public static int TabTitle = R.string.StateOfChargeTabTitle;
    private boolean _bidirectionalUnitsInWatts;

    public StateOfChargeFragment() {

        super(R.layout.fragment_state_of_charge);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        _bidirectionalUnitsInWatts = settings.getBoolean(Constants.BIDIRECTIONALUNIT_PREFERENCE, false);
    }

    public void setReadings(Readings readings) {
        try {
            restoreOriginalScale();
            View v = this.getView().findViewById(R.id.BidirectionalCurrent);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                float batteryCurrent = readings.GetFloat(RegisterName.BatCurrent);
                if (_bidirectionalUnitsInWatts) {
                    float batteryVolts = readings.GetFloat(RegisterName.BatVoltage);
                    gaugeView.setTargetValue(batteryCurrent * batteryVolts);
                } else {
                    gaugeView.setTargetValue(batteryCurrent);
                }
                int socVal = readings.GetInt(RegisterName.SOC);
                SOCGauge soc = (SOCGauge) this.getView().findViewById(R.id.SOC);
                soc.setValue(socVal);
            }
        } catch (Exception ignore) {

        }
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = this.getView().findViewById(R.id.BidirectionalCurrent);
        if (v != null) {
            BaseGauge gaugeView = (BaseGauge) v;
            if (_bidirectionalUnitsInWatts) {
                gaugeView.setTitle(this.getString(R.string.BatPowerTitle));
                gaugeView.setUnit("W");
            } else {
                gaugeView.setTitle(this.getString(R.string.BatCurrentTitle));
                gaugeView.setUnit("A");
            }
            gaugeView.setGreenRange(50, 100);
            gaugeView.setTargetValue(0.0f);
        }
    }

    public void restoreOriginalScale() {
        if (restoreOriginalScale) {
            restoreOriginalScale = false;

            View v = this.getView().findViewById(R.id.BidirectionalCurrent);
            if (v != null) {
                BaseGauge gauge = (BaseGauge) v;
                gauge.restoreOriginalScaleEnd();
            }
        }
        return;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
