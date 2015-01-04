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

package ca.farrelltonsolar.classic;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by Graham on 21/12/2014.
 */
public class DayLogCalendar extends Fragment {
    private static final String ARG_MONTH = "month";
    private DateTime month;
    private CalendarAdapter adapter;
    private View theView;
    private boolean isReceiverRegistered;

    public static DayLogCalendar newInstance(int month) {
        DayLogCalendar fragment = new DayLogCalendar();
        Bundle args = new Bundle();
        args.putInt(ARG_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        theView = inflater.inflate(R.layout.day_log_calendar, container, false);
        Bundle args = getArguments();
        int monthOffset = args != null ? args.getInt(ARG_MONTH) : 0;
        month = DateTime.now().minusMonths(monthOffset).withTimeAtStartOfDay().withDayOfMonth(1);
        adapter = new CalendarAdapter(this.getActivity(), month);
        GridView gridview = (GridView) theView.findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        gridview.setVelocityScale(5);

        TextView title  = (TextView) theView.findViewById(R.id.title);
        title.setText(month.toString("MMMM yyyy"));
        View linearLayout =  theView.findViewById(R.id.headerlayout);
        DateTime days = month;

        for (int i = 0; i < 7; i++) {
            int d = ((i + 6) % 7) +1;
            days = days.withDayOfWeek(d);
            TextView aDay = new TextView(theView.getContext());
            aDay.setText(DateTimeFormat.forPattern("E").print(days));
            aDay.setGravity(Gravity.CENTER);
            aDay.setTextColor(Color.BLACK);
            aDay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            ((LinearLayout) linearLayout).addView(aDay);

        }

        return theView;
    }

    public void refreshCalendar()
    {
        TextView title  = (TextView) theView.findViewById(R.id.title);

        adapter.refreshDays(month);
        adapter.notifyDataSetChanged();
        title.setText(month.toString("MMMM yyyy"));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(DayLogCalendar.this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterReceiver();
        Log.d(getClass().getName(), "onStop");
    }
    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(DayLogCalendar.this.getActivity()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                LogEntry logs = (LogEntry)intent.getSerializableExtra("logs");
                if (logs != null) {
                    unRegisterReceiver();
                    adapter.setPowerSeries(logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY));
                    adapter.setFloatSeries(logs.getFloatArray(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY));
                    adapter.setHighPowerSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY));
                    adapter.setHighTempSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY));
                    adapter.setHighPVVoltSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY));
                    adapter.setHighBatVoltSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY));
                    adapter.notifyDataSetChanged();
                    Log.d(getClass().getName(), String.format("Day calendar received logs from classic %s", Thread.currentThread().getName()));
                }
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Day calendar failed to load logs %s ex: %s", Thread.currentThread().getName(), e));
            }

        }
    };
}
