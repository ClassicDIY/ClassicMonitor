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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ca.farrelltonsolar.uicomponents.AbstractSeries;
import ca.farrelltonsolar.uicomponents.ChartView;
import ca.farrelltonsolar.uicomponents.LabelAdapter;
import ca.farrelltonsolar.uicomponents.LinearSeries;
import ca.farrelltonsolar.uicomponents.ValueLabelAdapter;

/**
 * Created by Graham on 19/12/2014.
 */
public class HourLogChart extends Fragment {

    private boolean isReceiverRegistered;
    ChartView chartView;
    private List<AbstractSeries> mSeries = new ArrayList<AbstractSeries>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View theView = inflater.inflate(R.layout.hour_logs_chart, container, false);
        // Find the chart view
        chartView = (ChartView) theView.findViewById(R.id.chart_view);

        LabelAdapter left = new ValueLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.VERTICAL, "%.1f");
        LabelAdapter bottom = new HourLabelAdapter(this.getActivity(), ValueLabelAdapter.LabelOrientation.HORIZONTAL);
        chartView.setLeftLabelAdapter(left);
        chartView.setBottomLabelAdapter(bottom);
        setHasOptionsMenu(true);
        return theView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(HourLogChart.this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setupSpinner( MenuItem item )
    {
        item.setVisible(true);
        item.setActionView(R.layout.action_chart_select);
        View view = MenuItemCompat.getActionView(item);
        if (view instanceof Spinner)
        {
            Spinner spinner = (Spinner) view;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    chartView.clearSeries();
                    if (position < mSeries.size())
                        chartView.addSeries(mSeries.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.chart_menu, menu); // inflate the menu
        MenuItem shareItem = menu.findItem(R.id.chart_preferences);
        setupSpinner(shareItem);


        super.onCreateOptionsMenu(menu, inflater);
    }

    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(HourLogChart.this.getActivity()).unregisterReceiver(mReadingsReceiver);
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
                    new ChartLoader(logs).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Log.d(getClass().getName(), String.format("Hour Log Chart received logs from classic %s", Thread.currentThread().getName()));
                }
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Hour Log Chart failed to load logs %s ex: %s", Thread.currentThread().getName(), e));
            }

        }
    };

    private class ChartLoader extends AsyncTask<String, Void, String> {
        private ChartLoader(LogEntry logs) {
            this.logs = logs;
        }
        LogEntry logs;
        LinearSeries seriesPower;
        LinearSeries seriesInputVoltage;
        LinearSeries seriesBatteryVoltage;
        LinearSeries seriesOutputCurrent;
        LinearSeries seriesChargeState;
        LinearSeries seriesEnergy;

        @Override
        protected String doInBackground(String... params) {
            short[] timeStamps = logs.getShortArray(Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY);
            seriesPower = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_POWER_HOURLY_CATEGORY));
            seriesInputVoltage = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY));
            seriesBatteryVoltage = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY));
            seriesOutputCurrent = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY));
            seriesChargeState = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY));
            seriesEnergy = getLinearSeries(timeStamps, logs.getFloatArray(Constants.CLASSIC_ENERGY_HOURLY_CATEGORY));
            Log.d(getClass().getName(), String.format("Chart doInBackground completed %s", Thread.currentThread().getName()));
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            mSeries.add(seriesPower);
            mSeries.add(seriesInputVoltage);
            mSeries.add(seriesBatteryVoltage);
            mSeries.add(seriesOutputCurrent);
            mSeries.add(seriesChargeState);
            mSeries.add(seriesEnergy);
            chartView.addSeries(seriesPower);
            Log.d(getClass().getName(), String.format("Chart onPostExecute completed %s", Thread.currentThread().getName()));
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private LinearSeries getLinearSeries(short[] timeStamps, float[] yAxis) {
        // Create the data points
        LinearSeries series = new LinearSeries();
        series.setLineColor(Color.YELLOW);
        series.setLineWidth(4);
        if (timeStamps != null && yAxis != null && yAxis.length >= timeStamps.length ) {
            short offset = 1440; // 24 hrs ago
            for (int i = 0; i < timeStamps.length; i++) {
                short t = timeStamps[i];
                t = (short)(offset - t);
                series.addPoint(new LinearSeries.LinearPoint(t, yAxis[i]));
            }
        }
        else {
            for (double i = 0d; i <= (2d * Math.PI); i += 0.1d) {
                series.addPoint(new LinearSeries.LinearPoint(i, Math.sin(i))); // test pattern
            }
        }
        return series;
    }

}
