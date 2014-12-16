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

/**
 * Created by Graham on 14/12/2014.
 */
public class EnergyFragment extends GaugeFramentBase {

    public static int TabTitle = R.string.EnergyTabTitle;

    public EnergyFragment() {
        super(R.layout.fragment_energy);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        BaseGauge energyTodayGauge = (BaseGauge) view.findViewById(R.id.EnergyToday);
        energyTodayGauge.setTargetValue(0.0f);
        energyTodayGauge.setGreenRange(10, 100);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setReadings(Readings readings) {
        try {

            BaseGauge energyTodayGauge = (BaseGauge) this.getView().findViewById(R.id.EnergyToday);
            energyTodayGauge.setTargetValue(autoAdjustScale(energyTodayGauge.getId(), readings.GetFloat(RegisterName.EnergyToday)));
            TextView tv = (TextView) this.getView().findViewById(R.id.EnergyTotalValue);
            tv.setText(String.valueOf(readings.GetFloat(RegisterName.TotalEnergy)));

        } catch (Exception ignore) {

        }
    }

}
