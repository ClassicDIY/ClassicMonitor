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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.HashMap;
import java.util.Map;

import ca.farrelltonsolar.uicomponents.BaseGauge;

public class SystemFragment extends ReadingFramentBase {

    public static int TabTitle = R.string.SystemTabTitle;
    private boolean unitsInWatts;
    private float originalScaleEnd;
    private boolean isSlaveReceiverRegistered = false;
    Map<String, Float> slaveControllerCurrent = new HashMap<String, Float>();
    Map<String, Float> slaveControllerPower = new HashMap<String, Float>();
    Map<String, Float> slaveControllerWhizbangJr = new HashMap<String, Float>();

    public SystemFragment() {

        super(R.layout.fragment_system);
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null) {
            unitsInWatts = cc.isBidirectionalUnitsInWatts();
        }
    }

    public void setReadings(Readings readings) {
        try {
            View v = this.getView().findViewById(R.id.Load);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                float whizbangBatteryCurrent = 0.0f;
                if (readings.getReadings().containsKey(RegisterName.WhizbangBatCurrent.name())) {
                    whizbangBatteryCurrent = readings.getFloat(RegisterName.WhizbangBatCurrent);
                }
                for (float f : slaveControllerWhizbangJr.values()) {
                    whizbangBatteryCurrent += f;
                }
                float slaveSum = 0.0f;
                for (float f : slaveControllerCurrent.values()) {
                    slaveSum += f;
                }
                float ccCurrent = readings.getFloat(RegisterName.BatCurrent) + slaveSum;
                float loadCurrent = ccCurrent - whizbangBatteryCurrent;
                if (unitsInWatts) {
                    float batteryVolts = readings.getFloat(RegisterName.BatVoltage);
                    gaugeView.setTargetValue(loadCurrent * batteryVolts);
                } else {
                    gaugeView.setTargetValue(loadCurrent);
                }
            }
            v = this.getView().findViewById(R.id.Power);
            if (v != null) {
                BaseGauge gaugeView = (BaseGauge) v;
                float slaveSum = 0.0f;
                for (float f : slaveControllerPower.values()) {
                    slaveSum += f;
                }
                float power = readings.getFloat(RegisterName.Power) + slaveSum;
                gaugeView.setTargetValue(power);
            }
        } catch (Exception ignore) {

        }
    }

    public void initializeReadings(View view, Bundle savedInstanceState) {
        View v = this.getView().findViewById(R.id.Load);
        if (v != null) {

            BaseGauge gaugeView = (BaseGauge) v;
            if (gaugeView != null) {
                originalScaleEnd = gaugeView.getScaleEnd();
                gaugeView.setOnClickListener(GetClickListener(container));
                setupGauge(gaugeView);
            }
        }
        v = this.getView().findViewById(R.id.Power);
        if (v != null) {
            BaseGauge gaugeView = (BaseGauge) v;
            gaugeView.setGreenRange(10, 100);
            gaugeView.setTargetValue(0.0f);
        }
        slaveControllerCurrent.clear();
        slaveControllerPower.clear();
        slaveControllerWhizbangJr.clear();
    }

    private void setupGauge(BaseGauge gaugeView) {
        if (unitsInWatts) {
            gaugeView.setUnit("W");
        } else {
            gaugeView.setUnit("A");
        }
        gaugeView.setGreenRange(10, 100);
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
                        unitsInWatts = !unitsInWatts;
                        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
                        if (cc != null) {
                            cc.setBidirectionalUnitsInWatts(unitsInWatts);
                        }
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

    @Override
    public void onStop() {
        super.onStop();
        if (isSlaveReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mSlaveReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isSlaveReceiverRegistered = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isSlaveReceiverRegistered) {
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mSlaveReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE));
            isSlaveReceiverRegistered = true;
        }
    }

    // Our handler for received Intents.
    protected BroadcastReceiver mSlaveReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle bundle = intent.getBundleExtra("readings");
            String uniqueId = intent.getStringExtra("uniqueId");
            if (bundle.containsKey(RegisterName.WhizbangBatCurrent.name())) {
                slaveControllerWhizbangJr.put(uniqueId, bundle.getFloat(RegisterName.WhizbangBatCurrent.name(), 0));
            }
            slaveControllerCurrent.put(uniqueId, bundle.getFloat(RegisterName.BatCurrent.name(), 0));
            slaveControllerPower.put(uniqueId, bundle.getFloat(RegisterName.Power.name(), 0));
        }
    };
}
