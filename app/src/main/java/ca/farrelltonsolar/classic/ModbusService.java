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

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;

public class ModbusService extends Service {


    private final IBinder mBinder = new ModbusServiceBinder();
    private Timer pollTimer;
    ModbusTask task;

    public ModbusService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(getClass().getName(), "onBind");
        return mBinder;
    }

    public void stopMonitoringChargeController() {
        if (task != null) {
            Log.d(getClass().getName(), "before disconnect");
            Disconnector d = new Disconnector(task);
            d.execute();
            Log.d(getClass().getName(), "after disconnect");
            task = null;
        }
        if (pollTimer != null) {
            pollTimer.cancel();
        }
    }

    public class ModbusServiceBinder extends Binder {
        ModbusService getService() {
            return ModbusService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(getClass().getName(), "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy");
        stopMonitoringChargeController();
        super.onDestroy();
    }

    public void monitorChargeController(ChargeController controller) {
        if (controller == null) {
            return;
        }
        new MonitorLauncher(controller).execute();
    }

    private void doMonitor(ChargeController controller) {
        stopMonitoringChargeController();
        pollTimer = new Timer();
        task = new ModbusTask(controller, this.getBaseContext());
        pollTimer.schedule(task, 100, Constants.MODBUS_POLL_TIME);
        Log.d(getClass().getName(), String.format("Monitor running on: %s this thread is %s", controller.toString(), Thread.currentThread().getName()));
    }

    private class MonitorLauncher extends AsyncTask<String, Void, String> {

        private MonitorLauncher(ChargeController controller) {
            this.controller = controller;
        }

        ChargeController controller;

        @Override
        protected String doInBackground(String... params) {
            doMonitor(controller);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class Disconnector extends AsyncTask<String, Void, String> {

        private Disconnector(ModbusTask task) {
            this.task = task;
        }

        ModbusTask task;

        @Override
        protected String doInBackground(String... params) {
            task.disconnect();
            task = null;
            return null;
        }
    }

}
