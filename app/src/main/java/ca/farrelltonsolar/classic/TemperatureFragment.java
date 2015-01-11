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

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.TemperatureGauge;

/**
 * Created by Graham on 14/12/2014.
 */
public class TemperatureFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.TemperatureTabTitle;
    private boolean useFahrenheit;
    
    public TemperatureFragment() {
        super(R.layout.fragment_temperature);
    }

    @Override
    public void initializeReadings(View view, Bundle savedInstanceState) {
        useFahrenheit = MonitorApplication.chargeControllers().useFahrenheit();
        SetScale();
    }

    private void SetScale() {
        TemperatureGauge gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.BatTemperature);
        gaugeView.setFahrenheit(useFahrenheit);
        gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.FETTemperature);
        gaugeView.setFahrenheit(useFahrenheit);
        gaugeView = (TemperatureGauge) this.getView().findViewById(R.id.PCBTemperature);
        gaugeView.setFahrenheit(useFahrenheit);
    }

    @Override
    public void setReadings(Readings reading) {
        try {
            BaseGauge gaugeView = (BaseGauge) this.getView().findViewById(R.id.BatTemperature);
            float batteryTemp = reading.getFloat(RegisterName.BatTemperature);
            gaugeView.setTargetValue(toSelectedScale(batteryTemp));

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.FETTemperature);
            float fetTemp = reading.getFloat(RegisterName.FETTemperature);
            gaugeView.setTargetValue(toSelectedScale(fetTemp));

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.PCBTemperature);
            float pcbTemp = reading.getFloat(RegisterName.PCBTemperature);
            gaugeView.setTargetValue(toSelectedScale(pcbTemp));

        } catch (Exception ignore) {

        }
    }
    
    private float toSelectedScale (float celcius) {
        return useFahrenheit ? celcius * 1.8f + 32 : celcius;
        
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        if (MonitorApplication.chargeControllers().useFahrenheit() != useFahrenheit) { // changed?
            useFahrenheit = MonitorApplication.chargeControllers().useFahrenheit();
            SetScale();
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
