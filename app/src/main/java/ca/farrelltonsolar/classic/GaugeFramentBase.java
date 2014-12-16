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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.farrelltonsolar.uicomponents.BaseGauge;

/**
 * Created by Graham on 14/12/2014.
 */
public abstract class GaugeFramentBase extends Fragment implements GaugeFragmentInterface {

    int layoutId;

    protected GaugeFramentBase(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(layoutId, container, false);
        return theView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeReadings(view, savedInstanceState);
        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));

    }

    // Our handler for received Intents.
    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            setReadings(new Readings(intent.getBundleExtra("readings")));
        }
    };


    protected float autoAdjustScale(int gaugeId, float target) {
        BaseGauge gauge = (BaseGauge) this.getActivity().findViewById(gaugeId);
        gauge.setScaleEnd(getScale(gauge.getScaleEnd(), target));
        return target;
    }

    protected float getScale(float currentScaleEnd, float target) {
        while (currentScaleEnd < target) {
            currentScaleEnd *= 2;
        }
        return currentScaleEnd;
    }

}
