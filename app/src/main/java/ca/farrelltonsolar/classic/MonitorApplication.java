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

import static android.arch.lifecycle.Lifecycle.Event.ON_START;
import static android.arch.lifecycle.Lifecycle.Event.ON_STOP;

/**
 * Created by Graham on 26/12/13.
 */

public class MonitorApplication extends Application implements LifecycleObserver {
    private static MonitorApplication instance;
    static Map<Integer, String> chargeStates = new HashMap<Integer, String>();
    static Map<Integer, String> chargeStateTitles = new HashMap<Integer, String>();
    static Map<Integer, String> mpptModes = new HashMap<Integer, String>();
    static Map<Integer, Pair<Severity, String>> messages = new HashMap<Integer, Pair<Severity, String>>();
    static Map<Integer, Pair<Severity, String>> reasonsForResting = new HashMap<Integer, Pair<Severity, String>>();
    static UDPListener UDPListenerService;
    static boolean isUDPListenerServiceBound = false;
    private static ChargeControllers chargeControllers;
    private GsonBuilder gsonBuilder;
    ModbusService modbusService;
    static boolean isModbusServiceBound = false;
    WifiManager.WifiLock wifiLock;
    private MqttAndroidClient mqttClient;
    private String statTopic;
    private String cmndTopic;
    private long mqttPublishPeriod;
    private boolean mqttIdle = true;
    private Timer mqttWakeTimer;

    public MonitorApplication() {
        super();
    }

    public void onCreate() {
        super.onCreate();

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
            chargeControllers = configuration.getObject("devices", ChargeControllers.class);
        } catch (Exception ex) {
            Log.w(getClass().getName(), "getComplexPreferences failed to load");
            chargeControllers = null;
        }
        if (chargeControllers == null) { // save empty collection
            chargeControllers = new ChargeControllers(getApplicationContext());
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        if (chargeControllers.mqttType() != MQTT_Type.Subscriber) {  // do not use PVOutput & Modbus when MQTT subscriber
            WifiManager wifi = (WifiManager) MonitorApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            chargeControllers = MonitorApplication.chargeControllers();
            if (chargeControllers.mqttType() != MQTT_Type.Subscriber) {
                if (wifi != null) {
                    wifiLock = wifi.createWifiLock("ClassicMonitor");
                }
            }
            if (chargeControllers.uploadToPVOutput()) {
                try {
                    startService(new Intent(this, PVOutputService.class)); // start PVOutputService intent service
                } catch (Exception ex) {
                    Log.w(getClass().getName(), "Failed to start PVOutput service");
                    Toast.makeText(getApplicationContext(), "Failed to start PVOutput service", Toast.LENGTH_SHORT).show();
                }
            }
            bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
        }
        String rootTopic = chargeControllers.mqttRootTopic();
        if (rootTopic.endsWith("/") == false) {
            rootTopic += "/";
        }
        statTopic = String.format("%s%s", rootTopic, Constants.STAT_TOPIC_SUFFIX);
        cmndTopic = String.format("%s%s", rootTopic, Constants.CMND_TOPIC_SUFFIX);
        mqttPublishPeriod = System.currentTimeMillis();
        Log.d(getClass().getName(), "onCreate complete");
    }

    @OnLifecycleEvent(ON_START)
    void onStart(LifecycleOwner source) {
        Log.d(getClass().getName(), "onStart event");
        if (wifiLock != null) {
            wifiLock.acquire();
        }
        if (chargeControllers.mqttType() != MQTT_Type.Subscriber) {
            LocalBroadcastManager.getInstance(this).registerReceiver(addChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(this).registerReceiver(removeChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER));
            if (isModbusServiceBound && modbusService != null) {
                modbusService.monitorChargeControllers(chargeControllers());
            } else {
                bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
            }
            if (isUDPListenerServiceBound && UDPListenerService != null) {
                if (chargeControllers().autoDetectClassic()) {
                    UDPListenerService.listen(chargeControllers);
                }
            } else {
                bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
        try {
            connectToMQTT();
        } catch (MqttException e) {
            Log.w(getClass().getName(), "connectToMQTT exception");
            e.printStackTrace();
        }

    }

    @OnLifecycleEvent(ON_STOP)
    void onStop(LifecycleOwner source) {
        Log.d(getClass().getName(), "onStop event");
        if (wifiLock != null) {
            wifiLock.release();
        }
        ComplexPreferences configuration = ComplexPreferences.getComplexPreferences(MonitorApplication.getAppContext(), null, Context.MODE_PRIVATE);
        if (configuration != null) {
            configuration.putObject("devices", chargeControllers);
            configuration.commit();
        }
        if (chargeControllers.mqttType() != MQTT_Type.Subscriber) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(addChargeControllerReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(removeChargeControllerReceiver);
            try {
                if (isModbusServiceBound && modbusService != null) {
                    modbusService.stopMonitoringChargeControllers();
                }
                if (isUDPListenerServiceBound && UDPListenerService != null) {
                    UDPListenerService.stopListening();
                }
            } catch (Exception e) {
                Log.w(getClass().getName(), "stop service exception");
                e.printStackTrace();
            }
        }
        try {
            unSubscribe();
            IMqttToken token = mqttClient.disconnect();
//            token.waitForCompletion();
//            mqttClient.unregisterResources();
            Log.d(getClass().getName(), "mqttClient disconnected");
        } catch (Exception e) {
            Log.w(getClass().getName(), "unSubscribe exception");
            e.printStackTrace();
        }
    }

    private boolean connectToMQTT() throws MqttException {
        boolean rVal = false;
        if (chargeControllers.mqttType() != MQTT_Type.Off) {
            try {
                if (mqttClient != null) {
                    rVal = mqttClient.isConnected();
                }
            } catch (Exception ex) {
                mqttClient = null;
            }
            if (rVal == false) {
                Log.d(getClass().getName(), "connectToMQTT");
                String brokerUrl = String.format("tcp://%s:%d", chargeControllers.mqttBrokerHost(), chargeControllers.mqttPort());
                if (mqttClient == null) {
                    mqttClient = new MqttAndroidClient(MonitorApplication.getAppContext(), brokerUrl, generateClientId());
                    Log.d(getClass().getName(), "creating new mqttClient");
                }
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setAutomaticReconnect(true);
                //mqttConnectOptions.setWill(Constants.PUBLISH_TOPIC, "I am going offline".getBytes(), 1, true);
                mqttConnectOptions.setUserName(chargeControllers.mqttUser());
                mqttConnectOptions.setPassword(chargeControllers.mqttPassword().toCharArray());
                IMqttToken token = mqttClient.connect(mqttConnectOptions);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(2048);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttClient.setBufferOpts(disconnectedBufferOptions);
                        Log.d(getClass().getName(), "mqttClient connected");
                        Subscribe();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w(getClass().getName(), String.format("mqttClient failed to connect: %s", exception.getMessage()));
                    }
                });
                rVal = true;
            } else {
                Log.d(getClass().getName(), "already connected To MQTT");
                Subscribe();
            }
        }
        return rVal;
    }

    private static String generateClientId() {
        return Constants.CLIENT_ID + System.currentTimeMillis() * 1000000L;
    }

    protected BroadcastReceiver mMqttReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mqttIdle || mqttPublishPeriod < System.currentTimeMillis()) {
                try {
                    Bundle b = intent.getBundleExtra("readings");
                    Gson gson = gsonBuilder.create();
                    String json = gson.toJson(b);
                    MqttMessage message = new MqttMessage(json.getBytes("UTF-8"));
                    message.setId(320);
                    message.setRetained(false);
                    message.setQos(1);
                    mqttClient.publish(String.format("%s/readings", statTopic), message);
                } catch (Exception e) {
                    Log.w(getClass().getName(), String.format("Failed to publish payload to MQTT broker, ex: %s", e));
                    e.printStackTrace();
                }
            }
            if (mqttPublishPeriod < System.currentTimeMillis()) {
                mqttPublishPeriod = System.currentTimeMillis() + (Constants.MQTT_IDLE_DELAY * 10); // idle: publish every 10 times the MQTT_IDLE_DELAY
                mqttIdle = true;
            }
        }
    };

    private void WakeMQTT(String cmnd) {
        try {
            Gson gson = gsonBuilder.create();
            String json = String.format("{\"%s\"}", cmnd);
            MqttMessage message = new MqttMessage(json.getBytes("UTF-8"));
            message.setId(321);
            message.setRetained(false);
            message.setQos(1);
            mqttClient.publish(String.format("%s/%s", cmndTopic, cmnd), message);
        } catch (Exception e) {
            Log.w(getClass().getName(), String.format("Failed to publish payload to MQTT broker, ex: %s", e));
            e.printStackTrace();
        }

    }

    private void Subscribe() {
        if (chargeControllers.mqttType() == MQTT_Type.Publisher) {
            Log.d(getClass().getName(), "Subscribe to MQTT " + cmndTopic);
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mMqttReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            try {
                IMqttToken token = mqttClient.subscribe(String.format("%s/#", cmndTopic), 1);
                token.setActionCallback(new IMqttActionListener() {
                    String topic = String.format("%s/#", cmndTopic);

                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        Log.d(getClass().getName(), "Subscribe Successfully " + topic);
                        mqttClient.setCallback(new MqttCallbackExtended() {
                            @Override
                            public void connectComplete(boolean b, String s) {
                                Log.d(getClass().getName(), "connectComplete " + topic);
                            }

                            @Override
                            public void connectionLost(Throwable throwable) {
                                Log.w(getClass().getName(), "connectionLost " + topic);
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                                try {
                                    Gson gson = gsonBuilder.create();
                                    if (topic.endsWith("info")) {
                                        ChargeControllerTransfer controller = chargeControllers.getCurrentChargeController().GetTransfer();
                                        String json = gson.toJson(controller);
                                        MqttMessage message = new MqttMessage(json.getBytes("UTF-8"));
                                        message.setId(322);
                                        message.setRetained(false);
                                        message.setQos(1);
                                        mqttClient.publish(String.format("%s/info", statTopic), message);
                                        mqttIdle = false; // publish readings every second for 5 minutes
                                        mqttPublishPeriod = System.currentTimeMillis() + Constants.MQTT_IDLE_DELAY;
                                    } else if (topic.endsWith("wake")) {
                                        mqttIdle = false; // publish readings every second for 5 minutes
                                        mqttPublishPeriod = System.currentTimeMillis() + Constants.MQTT_IDLE_DELAY;
                                    }
                                } catch (Exception e) {
                                    Log.w(getClass().getName(), "MQTT deserialize Exception " + topic);
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                                Log.d(getClass().getName(), "MQTT Publisher deliveryComplete ");
                            }
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        Log.w(getClass().getName(), "Subscribe Failed " + topic);

                    }
                });
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Subscribe Exception %s/#", cmndTopic));
                e.printStackTrace();
            }
        } else if (chargeControllers.mqttType() == MQTT_Type.Subscriber) {
            Log.d(getClass().getName(), "Subscribe to MQTT " + statTopic);
            try {
                IMqttToken token = mqttClient.subscribe(String.format("%s/#", statTopic), 1);
                token.setActionCallback(new IMqttActionListener() {
                    String topic = String.format("%s/#", statTopic);

                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        Log.d(getClass().getName(), "Subscribe Successfully " + topic);
                        mqttClient.setCallback(new MqttCallbackExtended() {
                            @Override
                            public void connectComplete(boolean b, String s) {
                                Log.d(getClass().getName(), "connectComplete " + topic);
                            }

                            @Override
                            public void connectionLost(Throwable throwable) {
                                Log.w(getClass().getName(), "connectionLost " + topic);
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                                try {
                                    String str = mqttMessage.toString();
                                    Gson gson = gsonBuilder.create();
                                    if (topic.endsWith("readings")) {

                                        Bundle b = gson.fromJson(str, Bundle.class);
                                        Readings readings = new Readings(b);
                                        readings.broadcastReadings(MonitorApplication.getAppContext(), "MQTT", Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS);
                                    } else if (topic.endsWith("info")) {
                                        ChargeControllerTransfer t = gson.fromJson(str, ChargeControllerTransfer.class);
                                        if (chargeControllers.count() == 0) {
                                            ChargeControllerInfo c = new ChargeControllerInfo();
                                            c.setIsCurrent(true);
                                            c.setIsReachable(true);
                                            c.LoadTransfer(t);
                                            chargeControllers.add(c);
                                        } else {
                                            chargeControllers.getCurrentChargeController().LoadTransfer(t);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(getClass().getName(), "MQTT deserialize Exception " + topic);
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                                Log.d(getClass().getName(), "MQTT Subscriber deliveryComplete ");
                            }
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        Log.w(getClass().getName(), "Subscribe Failed " + topic);

                    }
                });
                WakeMQTT("info");
                mqttWakeTimer = new Timer();
                mqttWakeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        WakeMQTT("wake");
                    }

                }, (Constants.MQTT_IDLE_DELAY - 2000), (Constants.MQTT_IDLE_DELAY - 2000));
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Subscribe Exception %s/#", statTopic));
                e.printStackTrace();
            }
        }
    }

    private void unSubscribe() throws MqttException {
        if (mqttClient.isConnected()) {
            String topic = statTopic;
            if (chargeControllers.mqttType() == MQTT_Type.Publisher) {
                topic = cmndTopic;
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mMqttReadingsReceiver);
            }
            IMqttToken token = mqttClient.unsubscribe(topic);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(getClass().getName(), "UnSubscribe Successfully ");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.w(getClass().getName(), "UnSubscribe Failed ");
                }
            });
        }
        if (mqttWakeTimer != null) {
            mqttWakeTimer.cancel();
            mqttWakeTimer.purge();
            Log.d(getClass().getName(), "mqttWakeTimer.purge");
        }
    }

    public static Pair<Severity, String> getMessage(int cs) {
        if (messages.containsKey(cs)) {
            return messages.get(cs);
        }
        return null;
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

    public static Pair<Severity, String> getReasonsForResting(int cs) {
        if (reasonsForResting.containsKey(cs)) {
            return reasonsForResting.get(cs);
        }
        return null;
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

    private ServiceConnection UDPListenerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            UDPListener.UDPListenerServiceBinder binder = (UDPListener.UDPListenerServiceBinder) service;
            UDPListenerService = binder.getService();
            isUDPListenerServiceBound = true;
            if (chargeControllers().autoDetectClassic()) {
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
            Log.d(getClass().getName(), "ModbusService ServiceConnected");
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();
            isModbusServiceBound = true;
            modbusService.monitorChargeControllers(MonitorApplication.chargeControllers());
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(getClass().getName(), "ModbusService onServiceDisconnected");
            isModbusServiceBound = false;
            modbusService = null;
            Log.d(getClass().getName(), "ModbusService ServiceDisconnected");
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
            modbusService.monitorChargeControllers(chargeControllers());
        }
    };

    private BroadcastReceiver removeChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConfigurationChanged();
        }
    };

    public static void ConfigurationChanged() {
        if (UDPListenerService != null) {
            UDPListenerService.stopListening();
            if (chargeControllers().autoDetectClassic()) {
                UDPListenerService.listen(chargeControllers);
            }
        }
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
}