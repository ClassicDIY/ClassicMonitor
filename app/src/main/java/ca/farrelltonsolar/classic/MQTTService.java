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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MQTTService extends Service {
    private final IBinder mBinder = new MQTTServiceBinder();
    private String currentDeviceName = "";
    private MqttAndroidClient mqttClient;
    private Timer mqttWakeTimer;
    private GsonBuilder gsonBuilder;
    private List<ModbusTask> tasks = new ArrayList<>();
    public MQTTService() {

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

    @Override
    public File getCacheDir() {
        return super.getCacheDir();
    }

    public class MQTTServiceBinder extends Binder {
        MQTTService getService() {
            return MQTTService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(getClass().getName(), "onCreate");
        super.onCreate();
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new BundleTypeAdapterFactory());
    }

    private synchronized String mqttRootTopic() {
        ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
        String mqttRootTopic;
        mqttRootTopic = chargeControllers.mqttRootTopic();
        if (mqttRootTopic.endsWith("/") == false) {
            mqttRootTopic += "/";
        }
        return mqttRootTopic;
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getName(), "onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(removeChargeControllerReceiver);
        stopMonitoringChargeControllers();
        super.onDestroy();
    }

    private BroadcastReceiver removeChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uniqueId = intent.getStringExtra("uniqueId");
            stopMonitoringChargeControllers();
        }
    };


    public void monitorChargeControllers(ChargeControllers controllers) {
        Log.d(getClass().getName(), "monitorChargeControllers");
        if (controllers == null || controllers.count() == 0) {
            return;
        }
        ChargeController controller = controllers.getCurrentChargeController();
        if (controller != null) {
            stopMonitoringChargeControllers();
            currentDeviceName = controller.deviceName();
            try {
                connectToMQTT();
            } catch (MqttException e) {
                Log.w(getClass().getName(), "connectToMQTT exception");
                e.printStackTrace();
            }
        }
    }

    public void stopMonitoringChargeControllers() {
        try {
            UnSubscribe();
            if (mqttClient != null && mqttClient.isConnected()) {
                IMqttToken token = mqttClient.disconnect();
//            token.waitForCompletion();
//            mqttClient.unregisterResources();
                Log.d(getClass().getName(), "mqttClient disconnected");
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "unSubscribe exception");
            e.printStackTrace();
        }
    }

    private boolean connectToMQTT() throws MqttException {
        boolean rVal = false;
        try {
            if (mqttClient != null) {
                rVal = mqttClient.isConnected();
            }
        } catch (Exception ex) {
            mqttClient = null;
        }
        if (rVal == false) {
            ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
            Log.d(getClass().getName(), "connectToMQTT");
            String brokerUrl = String.format("tcp://%s:%d", chargeControllers.mqttBrokerHost(), chargeControllers.mqttPort());
            if (mqttClient == null) {
                String clientId = Constants.CLIENT_ID + System.currentTimeMillis() * 1000000L;
                mqttClient = new MqttAndroidClient(MonitorApplication.getAppContext(), brokerUrl, clientId);
                SetupMQTTCallback();
                Log.d(getClass().getName(), "creating new mqttClient");
            }
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
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
                    Subscribe();
                    Log.d(getClass().getName(), "mqttClient connected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(getClass().getName(), String.format("mqttClient failed to connect: %s", exception.getMessage()));
                    BroadcastToast("Failed to connect to MQTT broker");
                }
            });
            rVal = true;
        }
        return rVal;
    }

    private void WakeMQTT(String cmnd) {
        try {
            Gson gson = gsonBuilder.create();
            String json = String.format("{\"%s\"}", cmnd);
            MqttMessage message = new MqttMessage(json.getBytes("UTF-8"));
            message.setId(321);
            message.setRetained(false);
            message.setQos(1);
            ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
            if (chargeControllers.systemViewEnabled() == false) {
                mqttClient.publish(String.format("%s/%s", String.format("%s%s/%s", mqttRootTopic(), currentDeviceName, Constants.CMND_TOPIC_SUFFIX), cmnd), message);
            }
            else {
                for (int i = 0; i < chargeControllers.count(); i++) {
                    String controllerName = MonitorApplication.chargeControllers().get(i).deviceName();
                    mqttClient.publish(String.format("%s/%s", String.format("%s%s/%s", mqttRootTopic(), controllerName, Constants.CMND_TOPIC_SUFFIX), cmnd), message);
                }
            }

        } catch (Exception e) {
            Log.w(getClass().getName(), String.format("Failed to publish payload to MQTT broker, ex: %s", e));
            e.printStackTrace();
        }
    }

    private void Subscribe() {
        Log.d(getClass().getName(), "Subscribe to MQTT ");
        if (mqttClient.isConnected()) {
            try {
                ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
                if (chargeControllers.systemViewEnabled() == false) {
                    SubscribeTo(String.format("%s%s/%s", mqttRootTopic(), currentDeviceName, Constants.STAT_TOPIC_SUFFIX));
                    SubscribeTo(String.format("%s%s/%s", mqttRootTopic(), currentDeviceName, Constants.TELE_TOPIC_SUFFIX));
                }
                else {
                    for (int i = 0; i < chargeControllers.count(); i++) {
                        String controllerName = MonitorApplication.chargeControllers().get(i).deviceName();
                        SubscribeTo(String.format("%s%s/%s", mqttRootTopic(), controllerName, Constants.STAT_TOPIC_SUFFIX));
                        SubscribeTo(String.format("%s%s/%s", mqttRootTopic(), controllerName, Constants.TELE_TOPIC_SUFFIX));
                    }
                }
                WakeMQTT("info");
                mqttWakeTimer = new Timer();
                mqttWakeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        WakeMQTT("wake");
                    }

                }, Constants.MQTT_WAKE_DELAY, (Constants.MQTT_IDLE_DELAY - 2000));
            } catch (Exception e) {
                Log.w(getClass().getName(), e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void SubscribeTo(String topic) throws MqttException {
        Log.d(getClass().getName(), "SubscribeTo " + topic);
        IMqttToken token = mqttClient.subscribe(String.format("%s/#", topic), 1);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(getClass().getName(), "Subscribe Successful " + (iMqttToken.getTopics().length > 0 ? iMqttToken.getTopics()[0] : ""));
            }
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.w(getClass().getName(), "Subscribe Failed " + iMqttToken.getException().getMessage());
                BroadcastToast("Failed to subscribe: " + iMqttToken.getException().getMessage());
            }
        });
    }

    private void SetupMQTTCallback() {
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d(getClass().getName(), "connectComplete " + s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.w(getClass().getName(), "connectionLost ");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                try {
                    String str = mqttMessage.toString();
                    Gson gson = gsonBuilder.create();
                    String[] elements = topic.split("/");
                    String deviceName = elements.length >= 4 ? elements[elements.length - 3] : "classic";  // devicename
                    Log.d(getClass().getName(), "MQTT messageArrived for " + deviceName);
                    ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
                    ChargeController current = chargeControllers.getCurrentChargeController();
                    if (current != null && current.deviceName().compareTo(deviceName) == 0) {
                        if (topic.endsWith("readings")) {
                            Bundle b = gson.fromJson(str, Bundle.class);
                            Readings readings = new Readings(b);
                            readings.broadcastReadings(MonitorApplication.getAppContext(), "MQTT", Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS);
                        } else if (topic.endsWith("info")) {
                            ChargeControllerTransfer t = gson.fromJson(str, ChargeControllerTransfer.class);
                            t.deviceName = deviceName; // use name defined in publisher
                            current.LoadTransfer(t);
                            if (current.isReachable() == false) {
                                chargeControllers.setReachable(deviceName, true);
                            }
                        } else if (topic.endsWith("LWT")) {
                            if (str.compareTo("Offline") == 0) {
                                clearReadings(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS);
                                chargeControllers.setReachable(deviceName, false);
                            } else {
                                WakeMQTT("wake");
                            }
                        }
                    }
                    else {
                        if (topic.endsWith("readings")) {
                            Bundle b = gson.fromJson(str, Bundle.class);
                            Readings readings = new Readings(b);
                            readings.broadcastReadings(MonitorApplication.getAppContext(), "MQTT", Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE);
                        }
                        else if (topic.endsWith("LWT")) {
                            if (str.compareTo("Offline") == 0) {
                                clearReadings(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE);
                                chargeControllers.setReachable(deviceName, false);
                            } else {
                                chargeControllers.setReachable(deviceName, true);
                                WakeMQTT("wake");
                            }
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

    private void clearReadings(String action) {
        Readings readings = new Readings();
        readings.set(RegisterName.Power, 0.0f);
        readings.set(RegisterName.BatVoltage, 0.0f);
        readings.set(RegisterName.BatCurrent, 0.0f);
        readings.set(RegisterName.PVVoltage, 0.0f);
        readings.set(RegisterName.PVCurrent, 0.0f);
        readings.set(RegisterName.EnergyToday, 0.0f);
        readings.set(RegisterName.TotalEnergy, 0.0f);
        readings.set(RegisterName.ChargeState, -1);
        readings.set(RegisterName.ConnectionState, 0);
        readings.set(RegisterName.SOC, 0);
        readings.set(RegisterName.Aux1, false);
        readings.set(RegisterName.Aux2, false);
        readings.broadcastReadings(MonitorApplication.getAppContext(), "MQTT", action);
    }

    private void UnSubscribe() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                ChargeControllers chargeControllers = MonitorApplication.chargeControllers();
                if (chargeControllers.systemViewEnabled() == false) {
                    UnSubscribeTo(String.format("%s%s/%s", mqttRootTopic(), currentDeviceName, Constants.STAT_TOPIC_SUFFIX));
                    UnSubscribeTo(String.format("%s%s/%s", mqttRootTopic(), currentDeviceName, Constants.TELE_TOPIC_SUFFIX));
                }
                else {
                    for (int i = 0; i < chargeControllers.count(); i++) {
                        String controllerName = MonitorApplication.chargeControllers().get(i).deviceName();
                        UnSubscribeTo(String.format("%s%s/%s", mqttRootTopic(), controllerName, Constants.STAT_TOPIC_SUFFIX));
                        UnSubscribeTo(String.format("%s%s/%s", mqttRootTopic(), controllerName, Constants.TELE_TOPIC_SUFFIX));
                    }
                }

            } catch (Exception e) {
                Log.w(getClass().getName(), e.getMessage());
                e.printStackTrace();
            }
        }
        if (mqttWakeTimer != null) {
            mqttWakeTimer.cancel();
            mqttWakeTimer.purge();
            Log.d(getClass().getName(), "mqttWakeTimer.purge");
        }
    }
    private void UnSubscribeTo(String topic) throws MqttException {
        IMqttToken token = mqttClient.unsubscribe(topic);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(getClass().getName(), " UnSubscribe Successful ");
            }
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.w(getClass().getName(), " UnSubscribe Failed " + iMqttToken.getException().getMessage());
            }
        });
    }

    private void BroadcastToast(String message) {
        Intent intent2 = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_TOAST);
        intent2.putExtra("message", message);
        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).sendBroadcast(intent2);
    }
}
