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
import android.os.Binder;
import android.os.IBinder;
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

    public void disconnect() {
        if (task != null) {
            task.disconnect();
        }
        if (pollTimer != null) {
            pollTimer.cancel();
            pollTimer.purge();
        }
        Log.d(getClass().getName(), "disconnect");
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
        disconnect();
        super.onDestroy();
    }

    public void Monitor(ChargeController controller) {
        if (controller == null) {
            return;
        }
        disconnect();
        pollTimer = new Timer();
        task = new ModbusTask(controller.getInetSocketAddress(), this.getBaseContext());
        pollTimer.schedule(task, 1000, Constants.MODBUS_POLL_TIME);
        Log.d(getClass().getName(), "Monitor running");
    }


}
