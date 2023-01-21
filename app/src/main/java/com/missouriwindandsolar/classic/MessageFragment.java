/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.missouriwindandsolar.classic;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Graham on 29/12/2014.
 */
public class MessageFragment extends ListFragment {

    private boolean isReceiverRegistered;
    protected ViewGroup container;
    MessageListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adapter = new MessageListAdapter(container.getContext());
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        Log.d(getClass().getName(), "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    // Our handler for received Intents.
    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle readings = intent.getBundleExtra("readings");
            int infoFlag = readings.getInt(RegisterName.InfoFlagsBits.name(), 0);
            adapter.clear();
            int bitMask = 0x40000000;
            while (bitMask > 0) {           // until all bits are zero
                if ((bitMask & infoFlag) != 0) {
                    Pair<Severity, String> item = MonitorApplication.getMessage(bitMask & infoFlag);
                    if (item != null) {
                        adapter.add(item);
                    }
                }
                bitMask >>= 1;              // shift bits, removing lower bit
            }
            float batteryVolt = readings.getFloat(RegisterName.BatVoltage.name(), 0);
            float whizbangAmp = readings.getFloat(RegisterName.WhizbangBatCurrent.name(), 0);
            float VbattRegSetPTmpComp = readings.getFloat(RegisterName.VbattRegSetPTmpComp.name(), 0);
            ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
            adapter.add(new Pair<Severity, String>(Severity.info, String.format(getString(R.string.TargetVoltage), VbattRegSetPTmpComp, batteryVolt)));
            if (cc.hasWhizbang())
            {
                adapter.add(new Pair<Severity, String>(Severity.info, String.format(getString(R.string.EndingAmps), cc.getEndingAmps(), whizbangAmp)));
            }
            int floatTime = readings.getInt(RegisterName.FloatTimeTodaySeconds.name(), 0);
            adapter.add(new Pair<Severity, String>(Severity.info, String.format(getString(R.string.FloatTime), formatSeconds(floatTime))));
            int absorbTime = readings.getInt(RegisterName.AbsorbTime.name(), 0);
            adapter.add(new Pair<Severity, String>(Severity.info, String.format(getString(R.string.AbsorbTime), formatSeconds(absorbTime))));
            int equalizeTime = readings.getInt(RegisterName.EqualizeTime.name(), 0);
            adapter.add(new Pair<Severity, String>(Severity.info, String.format(getString(R.string.EqualizeTime), formatSeconds(equalizeTime))));
            int reasonForResting = readings.getInt(RegisterName.ReasonForResting.name(), 0);
            Pair<Severity, String> item = MonitorApplication.getReasonsForResting(reasonForResting);
            if (item != null)
            {
                adapter.add(new Pair(item.first, String.format(getString(R.string.ReasonForResting), item.second)));
            }
            adapter.notifyDataSetChanged();
        }
    };

    private static String formatSeconds(int timeInSeconds)
    {
        String formattedTime = "";
        if (timeInSeconds < 0)
        {
            formattedTime = "-";
            timeInSeconds = Math.abs(timeInSeconds);
        }
        int hours = timeInSeconds / 3600;
        int secondsLeft = timeInSeconds - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + ":";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds ;

        return formattedTime;
    }

}
