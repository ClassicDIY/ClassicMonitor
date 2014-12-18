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

/**
 * Created by Graham on 14/12/2014.
 */
public abstract class ReadingFramentBase extends Fragment implements ReadingFragmentInterface {

    int layoutId;
    boolean restoreOriginalScale = false;

    protected ReadingFramentBase(int layoutId) {
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
        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mMonitorReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER));
    }

    // Our handler for received Intents.
    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            doSetReadings(new Readings(intent.getBundleExtra("readings")));
        }
    };

    protected void doSetReadings(Readings reading) {
        if (this.isVisible()) {
            setReadings(reading);
        }
    }

    protected BroadcastReceiver mMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            restoreOriginalScale = intent.getBooleanExtra("DifferentController", true);
        }
    };

}
