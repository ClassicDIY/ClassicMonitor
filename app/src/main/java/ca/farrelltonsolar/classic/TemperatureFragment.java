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

/**
 * Created by Graham on 14/12/2014.
 */
public class TemperatureFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.TemperatureTabTitle;

    public TemperatureFragment() {
        super(R.layout.fragment_temperature);
    }

    @Override
    public void initializeReadings(View view, Bundle savedInstanceState) {
        BaseGauge gaugeView = (BaseGauge) this.getView().findViewById(R.id.BatTemperature);
        gaugeView.setGreenRange(0, 60.0);

        gaugeView = (BaseGauge) this.getView().findViewById(R.id.FETTemperature);
        gaugeView.setGreenRange(0, 60.0);

        gaugeView = (BaseGauge) this.getView().findViewById(R.id.PCBTemperature);
        gaugeView.setGreenRange(0, 60.0);
    }

    @Override
    public void setReadings(Readings reading) {
        try {

            BaseGauge gaugeView = (BaseGauge) this.getView().findViewById(R.id.BatTemperature);
            float batteryTemp = reading.getFloat(RegisterName.BatTemperature);
            gaugeView.setTargetValue(batteryTemp);

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.FETTemperature);
            float fetTemp = reading.getFloat(RegisterName.FETTemperature);
            gaugeView.setTargetValue(fetTemp);

            gaugeView = (BaseGauge) this.getView().findViewById(R.id.PCBTemperature);
            float pcbTemp = reading.getFloat(RegisterName.PCBTemperature);
            gaugeView.setTargetValue(pcbTemp);

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
