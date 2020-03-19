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

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import android.arch.lifecycle.ProcessLifecycleOwner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.arch.lifecycle.Lifecycle.Event.ON_RESUME;
import static android.arch.lifecycle.Lifecycle.Event.ON_START;
import static android.arch.lifecycle.Lifecycle.Event.ON_STOP;

/**
 * Created by Graham on 26/12/13.
 */

public class MonitorApplication extends Application implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    static Map<Integer, String> chargeStates = new HashMap<Integer, String>();
    static Map<Integer, String> chargeStateTitles = new HashMap<Integer, String>();
    static Map<Integer, String> mpptModes = new HashMap<Integer, String>();
    static Map<Integer, Pair<Severity, String>> messages = new HashMap<Integer, Pair<Severity, String>>();
    static Map<Integer, Pair<Severity, String>> reasonsForResting = new HashMap<Integer, Pair<Severity, String>>();
    static UDPListener UDPListenerService;
    static boolean isUDPListenerServiceBound = false;
    static boolean isModbusServiceBound = false;
    static boolean isMQTTServiceBound = false;
    private static MonitorApplication instance;
    private static ChargeControllers chargeControllers;
    private CONNECTION_TYPE currentConnectionType;
    ModbusService modbusService;
    MQTTService mqttService;
    WifiManager.WifiLock wifiLock;
    private GsonBuilder gsonBuilder;
    private Timer disconnectTimer;


    private ServiceConnection UDPListenerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            UDPListener.UDPListenerServiceBinder binder = (UDPListener.UDPListenerServiceBinder) service;
            UDPListenerService = binder.getService();
            isUDPListenerServiceBound = true;
            if (chargeControllers.autoDetectClassic()) {
                UDPListenerService.listen(chargeControllers);
            }
            Log.d(getClass().getName(), "UDPListener ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isUDPListenerServiceBound = false;
            UDPListenerService = null;
            Log.d(getClass().getName(), "UDPListener ServiceDisconnected");
        }
    };

    private ServiceConnection modbusServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();
            isModbusServiceBound = true;
            Log.d(getClass().getName(), "ModbusService ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(getClass().getName(), "ModbusService onServiceDisconnected");
            isModbusServiceBound = false;
            modbusService = null;
        }
    };

    private ServiceConnection mqttServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            MQTTService.MQTTServiceBinder binder = (MQTTService.MQTTServiceBinder) service;
            mqttService = binder.getService();
            isMQTTServiceBound = true;
            Log.d(getClass().getName(), "MQTTService ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isMQTTServiceBound = false;
            mqttService = null;
            Log.d(getClass().getName(), "MQTTService ServiceDisconnected");
        }
    };

    // Our handler for received Intents.
    private BroadcastReceiver addChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Gson gson = gsonBuilder.create();
            ChargeControllerInfo cc = gson.fromJson(intent.getStringExtra("ChargeController"), ChargeController.class);
            Log.d(getClass().getName(), String.format("adding new controller to list (%s)", cc.toString()));
            chargeControllers.add(cc);
            SaveSettings();
            if (chargeControllers.getConnectionType() == CONNECTION_TYPE.MODBUS && isModbusServiceBound) {
                modbusService.monitorChargeControllers(chargeControllers);
            }
            if (chargeControllers.getConnectionType() == CONNECTION_TYPE.MQTT && isMQTTServiceBound) {
                mqttService.monitorChargeControllers(chargeControllers);
            }
        }
    };
    private BroadcastReceiver removeChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConfigurationChanged();
        }
    };

    public MonitorApplication() {
        super();
    }

    public static void ConfigurationChanged() {
        Log.d("MonitorApplication", "ConfigurationChanged");
        if (UDPListenerService != null) {
            UDPListenerService.stopListening();
            if (chargeControllers.autoDetectClassic()) {
                UDPListenerService.listen(chargeControllers);
            }
        }
        SaveSettings();
    }

    public static void monitorChargeController(int device) {
        if (device < 0 || device >= chargeControllers.count()) {
            return;
        }
        if (chargeControllers.setCurrent(device)) {
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getAppContext());
            Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER);
            pkg.putExtra("DifferentController", true);
            broadcaster.sendBroadcast(pkg); //notify activity
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(getClass().getName(), "onCreate");
        instance = this;
        if (Constants.DEVELOPER_MODE) {
            StrictMode.enableDefaults();
        }
        InitializeChargeStateLookup();
        InitializeChargeStateTitleLookup();
        InitializeMPPTModes();
        InitializeMessageLookup();
        InitializeReasonsForRestingLookup();
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
        try {
            ComplexPreferences configuration = ComplexPreferences.getComplexPreferences(this, null, Context.MODE_PRIVATE);
            int version = configuration.getVersion(getAppContext());
            int versionCode = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0).versionCode;
            chargeControllers = configuration.getObject("devices", ChargeControllers.class);
        } catch (Exception ex) {
            Log.w(getClass().getName(), "getComplexPreferences failed to load");
            chargeControllers = null;
        }
        if (chargeControllers == null) { // save empty collection
            chargeControllers = new ChargeControllers(getApplicationContext());
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.registerActivityLifecycleCallbacks(this);
        WifiManager wifi = (WifiManager) MonitorApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        chargeControllers = chargeControllers;
        if (wifi != null) {
            wifiLock = wifi.createWifiLock("ClassicMonitor");
        }
        currentConnectionType = chargeControllers.getConnectionType();
        if (currentConnectionType == CONNECTION_TYPE.MODBUS) {  // do not use PVOutput & Modbus when MQTT subscriber
            if (chargeControllers.uploadToPVOutput()) {
                try {
                    startService(new Intent(this, PVOutputService.class)); // start PVOutputService intent service
                } catch (Exception ex) {
                    Log.w(getClass().getName(), "Failed to start PVOutput service");
                    Toast.makeText(getApplicationContext(), "Failed to start PVOutput service", Toast.LENGTH_SHORT).show();
                }
            }
        }
        Log.d(getClass().getName(), "onCreate complete");
    }

    @OnLifecycleEvent(ON_START)
    void onStart(LifecycleOwner source) {
        Log.d(getClass().getName(), "onStart");
        if (wifiLock != null) {
            wifiLock.acquire();
        }

            bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(this, MQTTService.class), mqttServiceConnection, Context.BIND_AUTO_CREATE);

        Log.d(getClass().getName(), "onStart Done");
    }

    @OnLifecycleEvent(ON_STOP)
    void onStop(LifecycleOwner source) {
        Log.d(getClass().getName(), "onStop");
        if (wifiLock != null) {
            wifiLock.release();
        }
        SaveSettings();
        try {
            if (isModbusServiceBound && modbusService != null) {
                modbusService.stopMonitoringChargeControllers();
            }
            if (isUDPListenerServiceBound && UDPListenerService != null) {
                UDPListenerService.stopListening();
            }
            if (isMQTTServiceBound && mqttService != null) {
                mqttService.stopMonitoringChargeControllers();
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "stop service exception");
            e.printStackTrace();
        }
        Log.d(getClass().getName(), "onStop Done");
    }

    @OnLifecycleEvent(ON_RESUME)
    void onResume(LifecycleOwner source) {
        Log.d(getClass().getName(), "onResume");

    }

    private static void SaveSettings() {
        try {
            ComplexPreferences configuration = ComplexPreferences.getComplexPreferences(MonitorApplication.getAppContext(), null, Context.MODE_PRIVATE);
            if (configuration != null) {
                configuration.putObject("devices", chargeControllers);
                configuration.commit();
            }
        } catch (Exception e) {
            Log.w("MonitorApplication", "Failed to save settings " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity.getLocalClassName().compareTo("MonitorActivity") == 0) {
            LocalBroadcastManager.getInstance(this).registerReceiver(addChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(this).registerReceiver(removeChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER));
            if (disconnectTimer != null) {
                disconnectTimer.cancel();
                disconnectTimer.purge();
            }
            if (currentConnectionType != chargeControllers.getConnectionType()) {
                currentConnectionType = chargeControllers.getConnectionType();
                if (isModbusServiceBound && modbusService != null){
                    modbusService.stopMonitoringChargeControllers();
                }
                if (isMQTTServiceBound && mqttService != null){
                    mqttService.stopMonitoringChargeControllers();
                }
            }
            if (isModbusServiceBound && modbusService != null && chargeControllers.getConnectionType() == CONNECTION_TYPE.MODBUS){
                modbusService.monitorChargeControllers(chargeControllers);
            }
            if (isMQTTServiceBound && mqttService != null && chargeControllers.getConnectionType() == CONNECTION_TYPE.MQTT){
                mqttService.monitorChargeControllers(chargeControllers);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.getLocalClassName().compareTo("MonitorActivity") == 0) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(addChargeControllerReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(removeChargeControllerReceiver);
            disconnectTimer = new Timer();
            disconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 10 seconds unless Activity Resumed
                    if (isModbusServiceBound && modbusService != null){
                        modbusService.stopMonitoringChargeControllers();
                    }
                    if (isMQTTServiceBound && mqttService != null){
                        mqttService.stopMonitoringChargeControllers();
                    }
                }
            }, 10000);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public static Pair<Severity, String> getMessage(int cs) {
        if (messages.containsKey(cs)) {
            return messages.get(cs);
        }
        return null;
    }

    public static Pair<Severity, String> getReasonsForResting(int cs) {
        if (reasonsForResting.containsKey(cs)) {
            return reasonsForResting.get(cs);
        }
        return null;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public static ChargeControllers chargeControllers() {
        return chargeControllers;
    }

    public static String getChargeStateText(int cs) {
        if (chargeStates.containsKey(cs)) {
            return chargeStates.get(cs);
        }
        return "";
    }

    public static String getChargeStateTitleText(int cs) {
        if (chargeStateTitles.containsKey(cs)) {
            return chargeStateTitles.get(cs);
        }
        return "";
    }

    public static String getMpptModeText(int cs) {
        if (mpptModes.containsKey(cs)) {
            return mpptModes.get(cs);
        }
        return "";
    }

    // get supported language code, default to english
    public static String getLanguage() {
        String rVal = Locale.getDefault().getLanguage();
        switch (rVal) {
            case "en":
            case "it":
            case "de":
            case "es":
            case "fr":
                break;
            default:
                rVal = "en";
                break;
        }
        return rVal;
    }

    private void InitializeMessageLookup() {
        messages.put(0x00000001, new Pair(Severity.alert, getString(R.string.info_message_1)));
        messages.put(0x00000002, new Pair(Severity.alert, getString(R.string.info_message_2)));
        messages.put(0x00000100, new Pair(Severity.info, getString(R.string.info_message_100)));
        messages.put(0x00000200, new Pair(Severity.warning, getString(R.string.info_message_200)));
        messages.put(0x00000400, new Pair(Severity.warning, getString(R.string.info_message_400)));
        messages.put(0x00004000, new Pair(Severity.info, getString(R.string.info_message_4000)));
        messages.put(0x00008000, new Pair(Severity.info, getString(R.string.info_message_8000)));
        messages.put(0x00010000, new Pair(Severity.alert, getString(R.string.info_message_10000)));
        messages.put(0x00020000, new Pair(Severity.alert, getString(R.string.info_message_20000)));
        messages.put(0x00040000, new Pair(Severity.alert, getString(R.string.info_message_40000)));
        messages.put(0x00100000, new Pair(Severity.alert, getString(R.string.info_message_100000)));
        messages.put(0x00400000, new Pair(Severity.warning, getString(R.string.info_message_400000)));
        messages.put(0x08000000, new Pair(Severity.warning, getString(R.string.info_message_8000000)));
    }

    private void InitializeReasonsForRestingLookup() {
        reasonsForResting.put(1, new Pair(Severity.info, getString(R.string.reasonsForResting_message_1)));
        reasonsForResting.put(2, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_2)));
        reasonsForResting.put(3, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_3)));
        reasonsForResting.put(4, new Pair(Severity.info, getString(R.string.reasonsForResting_message_4)));
        reasonsForResting.put(5, new Pair(Severity.info, getString(R.string.reasonsForResting_message_5)));
        reasonsForResting.put(6, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_6)));
        reasonsForResting.put(7, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_7)));
        reasonsForResting.put(8, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_8)));
        reasonsForResting.put(9, new Pair(Severity.info, getString(R.string.reasonsForResting_message_9)));
        reasonsForResting.put(10, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_10)));
        reasonsForResting.put(11, new Pair(Severity.info, getString(R.string.reasonsForResting_message_11)));
        reasonsForResting.put(12, new Pair(Severity.info, getString(R.string.reasonsForResting_message_12)));
        reasonsForResting.put(13, new Pair(Severity.info, getString(R.string.reasonsForResting_message_13)));
        reasonsForResting.put(14, new Pair(Severity.info, getString(R.string.reasonsForResting_message_14)));
        reasonsForResting.put(15, new Pair(Severity.info, getString(R.string.reasonsForResting_message_15)));
        reasonsForResting.put(16, new Pair(Severity.info, getString(R.string.reasonsForResting_message_16)));
        reasonsForResting.put(17, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_17)));
        reasonsForResting.put(18, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_18)));
        reasonsForResting.put(19, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_19)));
        reasonsForResting.put(22, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_22)));
        reasonsForResting.put(25, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_25)));
        reasonsForResting.put(26, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_26)));
        reasonsForResting.put(27, new Pair(Severity.info, getString(R.string.reasonsForResting_message_27)));
        reasonsForResting.put(28, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_28)));
        reasonsForResting.put(29, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_29)));
        reasonsForResting.put(30, new Pair(Severity.info, getString(R.string.reasonsForResting_message_30)));
        reasonsForResting.put(31, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_31)));
        reasonsForResting.put(32, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_32)));
        reasonsForResting.put(33, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_33)));
        reasonsForResting.put(34, new Pair(Severity.info, getString(R.string.reasonsForResting_message_34)));
        reasonsForResting.put(35, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_35)));
        reasonsForResting.put(36, new Pair(Severity.alert, getString(R.string.reasonsForResting_message_36)));
        reasonsForResting.put(38, new Pair(Severity.info, getString(R.string.reasonsForResting_message_38)));
        reasonsForResting.put(136, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_136)));
        reasonsForResting.put(104, new Pair(Severity.warning, getString(R.string.reasonsForResting_message_104)));
        reasonsForResting.put(111, new Pair(Severity.info, getString(R.string.reasonsForResting_message_111)));
    }

    private void InitializeChargeStateLookup() {
        chargeStates.put(-1, getString(R.string.NoConnection));
        chargeStates.put(0, getString(R.string.RestingDescription));
        chargeStates.put(3, getString(R.string.AbsorbDescription));
        chargeStates.put(4, getString(R.string.BulkMPPTDescription));
        chargeStates.put(5, getString(R.string.FloatDescription));
        chargeStates.put(6, getString(R.string.FloatMPPTDescription));
        chargeStates.put(7, getString(R.string.EqualizeDescription));
        chargeStates.put(10, getString(R.string.HyperVocDescription));
        chargeStates.put(18, getString(R.string.EqMPPTDescription));
    }

    private void InitializeChargeStateTitleLookup() {
        chargeStateTitles.put(-1, "");
        chargeStateTitles.put(0, getString(R.string.RestingTitle));
        chargeStateTitles.put(3, getString(R.string.AbsorbTitle));
        chargeStateTitles.put(4, getString(R.string.BulkMPPTTitle));
        chargeStateTitles.put(5, getString(R.string.FloatTitle));
        chargeStateTitles.put(6, getString(R.string.FloatMPPTTitle));
        chargeStateTitles.put(7, getString(R.string.EqualizeTitle));
        chargeStateTitles.put(10, getString(R.string.HyperVocTitle));
        chargeStateTitles.put(18, getString(R.string.EqMpptTitle));
    }

    private void InitializeMPPTModes() {
        mpptModes.put(0x0003, getString(R.string.MPPTMode3));
        mpptModes.put(0x0005, getString(R.string.MPPTMode5));
        mpptModes.put(0x0009, getString(R.string.MPPTMode9));
        mpptModes.put(0x000B, getString(R.string.MPPTModeB));
        mpptModes.put(0x000D, getString(R.string.MPPTModeD));
    }

}