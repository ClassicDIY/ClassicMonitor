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
import ca.farrelltonsolar.uicomponents.Odometer;

/**
 * Created by Graham on 14/12/2014.
 */
public class EnergyFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.EnergyTabTitle;

    public EnergyFragment() {
        super(R.layout.fragment_energy);
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = this.getView().findViewById(R.id.EnergyToday);
        if (v != null) {
            BaseGauge energyTodayGauge = (BaseGauge) v;
            energyTodayGauge.setTargetValue(0.0f);
            energyTodayGauge.setGreenRange(10, 100);
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

    public void setReadings(Readings readings) {
        try {
            View v = this.getView().findViewById(R.id.EnergyToday);
            if (v != null) {
                BaseGauge energyTodayGauge = (BaseGauge) v;
                energyTodayGauge.setTargetValue(readings.GetFloat(RegisterName.EnergyToday));

            }
            v = this.getView().findViewById(R.id.EnergyTotalValue);
            if (v != null) {
                Odometer odometer = (Odometer) v;
                float val = readings.GetFloat(RegisterName.TotalEnergy) * 10;
                int decval = (int) val;
                odometer.setValue(decval);
            }
        } catch (Exception ignore) {

        }
    }

    public void restoreOriginalScale() {
        if (restoreOriginalScale) {
            restoreOriginalScale = false;
            View v = this.getView().findViewById(R.id.EnergyToday);
            if (v != null) {
                BaseGauge gauge = (BaseGauge) v;
                gauge.restoreOriginalScaleEnd();
            }
        }
        return;
    }

}
