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

package ca.classicdiy.classic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import ca.classicdiy.uicomponents.BaseGauge;
import ca.classicdiy.uicomponents.SOCGauge;

/**
 * Created by Graham on 14/12/2014.
 */
public class StateOfChargeFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.StateOfChargeTabTitle;
    private boolean bidirectionalUnitsInWatts;
    private float originalScaleEnd;
    
    public StateOfChargeFragment() {
        super(R.layout.fragment_state_of_charge);
        bidirectionalUnitsInWatts = MonitorApplication.chargeControllers().isBidirectionalUnitsInWatts();
    }

    public void setReadings(Readings readings) {
        try {
            View v = this.getView().findViewById(R.id.BidirectionalCurrent);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                float batteryCurrent = readings.getFloat(RegisterName.WhizbangBatCurrent);
                if (bidirectionalUnitsInWatts) {
                    float batteryVolts = readings.getFloat(RegisterName.BatVoltage);
                    gaugeView.setTargetValue(batteryCurrent * batteryVolts);
                } else {
                    gaugeView.setTargetValue(batteryCurrent);
                }
                int socVal = readings.getInt(RegisterName.SOC);
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
            if (gaugeView != null) {
                originalScaleEnd = gaugeView.getScaleEnd();
                gaugeView.setOnClickListener(GetClickListener(container));
                setupGauge(gaugeView);
            }
        }
    }

    private void setupGauge(BaseGauge gaugeView) {
        if (bidirectionalUnitsInWatts) {
            gaugeView.setTitle(this.getString(R.string.BatPowerTitle));
            gaugeView.setUnit("W");
        } else {
            gaugeView.setTitle(this.getString(R.string.BatCurrentTitle));
            gaugeView.setUnit("A");
        }
        gaugeView.setGreenRange(50, 100);
        gaugeView.setTargetValue(0.0f);
        
    }

    private View.OnClickListener GetClickListener(final ViewGroup container) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        bidirectionalUnitsInWatts = !bidirectionalUnitsInWatts;
                        MonitorApplication.chargeControllers().setBidirectionalUnitsInWatts(bidirectionalUnitsInWatts);
                        BaseGauge gauge = (BaseGauge) v;
                        if (gauge != null) {
                            setupGauge(gauge);
                            gauge.setScaleEnd(originalScaleEnd);
                        }
                        Animation animation2 = new AlphaAnimation(0.0f, 1.0f);
                        animation2.setDuration(500);
                        v.startAnimation(animation2);
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }

                });
                v.startAnimation(animation);
            }
        };
    }
}
