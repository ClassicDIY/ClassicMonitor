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
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.SolarGauge;


/**
 * This fragment displays the power gauges
 */
public class PowerFragment extends GaugeFramentBase {

    public static int TabTitle = R.string.PowerTabTitle;

    public PowerFragment() {
        super(R.layout.fragment_power);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        BaseGauge powerGauge = (BaseGauge) view.findViewById(R.id.Power);
        powerGauge.setTargetValue(0.0f);
        powerGauge.setGreenRange(10, 100);
        BaseGauge gauge;
        gauge = (BaseGauge) view.findViewById(R.id.PVVoltage);
        gauge.setGreenRange(25.0, 75.0);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.PVCurrent);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.BatVoltage);
        gauge.setGreenRange(55.0, 72.5);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.BatCurrent);
        gauge.setTargetValue(0.0f);
        TextView tv = (TextView) this.getView().findViewById(R.id.ChargeState);
        tv.setText(getString(R.string.NoConnection));
    }

    public void setReadings(Readings readings) {
        try {

            SolarGauge powerGauge = (SolarGauge) this.getView().findViewById(R.id.Power);
            powerGauge.setTargetValue(autoAdjustScale(powerGauge.getId(), readings.GetFloat(RegisterName.Power)));
            powerGauge.setLeftLed(readings.GetBoolean(RegisterName.Aux1));
            powerGauge.setRightLed(readings.GetBoolean(RegisterName.Aux2));

            BaseGauge gauge = (BaseGauge) this.getView().findViewById(R.id.PVVoltage);
            gauge.setTargetValue(autoAdjustScale(gauge.getId(), readings.GetFloat(RegisterName.PVVoltage)));
            gauge = (BaseGauge) this.getView().findViewById(R.id.PVCurrent);
            gauge.setTargetValue(autoAdjustScale(gauge.getId(), readings.GetFloat(RegisterName.PVCurrent)));
            gauge = (BaseGauge) this.getView().findViewById(R.id.BatVoltage);
            float bVolts = readings.GetFloat(RegisterName.BatVoltage);
            if (bVolts > 125) { // 120 volt system!
                gauge.setScaleEnd(200);
                gauge.setTargetValue(bVolts);
            } else { // 12, 24, 48, 96
                gauge.setTargetValue(autoAdjustScale(gauge.getId(), bVolts));
            }
            gauge = (BaseGauge) this.getView().findViewById(R.id.BatCurrent);
            float batAmps = readings.GetFloat(RegisterName.BatCurrent);
            if (batAmps < 0) {
                throw new Exception("bad amps reading: " + batAmps);
            }
            gauge.setTargetValue(autoAdjustScale(gauge.getId(), batAmps));

            TextView tv = (TextView) this.getView().findViewById(R.id.ChargeStateTitle);
            int cs = readings.GetInt(RegisterName.ChargeState);
            tv.setText(MonitorApplication.getChargeStateTitleText(cs));
            tv = (TextView) this.getView().findViewById(R.id.ChargeState);
            tv.setText(MonitorApplication.getChargeStateText(cs));
        } catch (Exception ignore) {

        }
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
