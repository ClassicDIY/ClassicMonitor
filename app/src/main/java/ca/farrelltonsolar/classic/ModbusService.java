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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;

public class ModbusService extends Service {

    private final IBinder mBinder = new ModbusServiceBinder();
    ModbusTask task;
    private Timer pollTimer;

    public ModbusService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        //Init();
        return mBinder;
    }

    public void disconnect(boolean clearReadings) {
        if (task != null) {
            task.disconnect();
            if (clearReadings) {
                task.clearReadings();
            }
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
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy");
        disconnect(false);
        super.onDestroy();
    }

    public void Monitor(ChargeController controller, boolean differentCC) {
        if (controller == null) {
            return;
        }
        new MonitorLauncher(controller, differentCC).execute();
    }

    private void doMonitor(ChargeController controller, boolean differentCC) {
        disconnect(differentCC);
        if (differentCC) {
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
            Intent pkg = new Intent("ca.farrelltonsolar.classic.MonitorChargeController");
            broadcaster.sendBroadcast(pkg);
        }
        pollTimer = new Timer();
        task = new ModbusTask(controller, this.getBaseContext());
        pollTimer.schedule(task, 1000, Constants.MODBUS_POLL_TIME);
        Log.d(getClass().getName(), String.format("Monitor running on: %s this thread is %s", controller.toString(), Thread.currentThread().getName()));
    }

    private class MonitorLauncher extends AsyncTask<String, Void, String> {

        private MonitorLauncher(ChargeController controller, boolean differentCC) {
            this.controller = controller;
            this.differentCC = differentCC;
        }

        ChargeController controller;
        boolean differentCC;

        @Override
        protected String doInBackground(String... params) {
            doMonitor(controller, differentCC);
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

}
