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

package ca.classicdiy.classic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class ChargeControllers {

    final transient Object lock = new Object();
    private static Context context;
    private String APIKey = "";
    private List<ChargeController> modbusDevices = new ArrayList<>();
    private List<ChargeController> mqttDevices = new ArrayList<>();
    private boolean useFahrenheit = false;
    private boolean autoDetectClassic = true;
    private boolean showPopupMessages = true;
    private boolean uploadToPVOutput = false;
    private boolean bidirectionalUnitsInWatts;
    private boolean systemViewEnabled = false;
    private String mqttBrokerHost = "mqtt.dioty.co";
    private int mqttPort = 1883;
    private String mqttUser = "";
    private String mqttPassword = "";
    private String mqttRootTopic = "";
    private CONNECTION_TYPE connectionType = CONNECTION_TYPE.MODBUS;
    private PVOutputSetting pVOutputSetting = new PVOutputSetting();

    // default ctor for de-serialization
    public ChargeControllers() {
    }

    public ChargeControllers(Context context) {
        this.context = context;
    }

    private List<ChargeController> Devices() {
        return (connectionType == CONNECTION_TYPE.MODBUS) ? modbusDevices : mqttDevices;
    }
    
    public ChargeController get(int position) {
        synchronized (Devices()) {
            return Devices().get(position);
        }
    }

    public ChargeController getCurrentChargeController() {
        synchronized (Devices()) {
            for (ChargeController cc : Devices()) {
                if (cc.isCurrent()) {
                    return cc;
                }
            }
        }
        return null; // none selected
    }

    public int getCurrentControllerIndex() {
        synchronized (Devices()) {
            for (int index = 0; index < Devices().size(); index++) {
                if (Devices().get(index).isCurrent()) {
                    return index;
                }
            }
        }
        return -1; // none selected
    }

    public boolean setCurrent(int position) {
        if (position >= Devices().size()) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (Devices()) {
            for (int index = 0; index < Devices().size(); index++) {
                ChargeController cc = Devices().get(index);
                if (cc.isCurrent() && index == position) {
                    return false; // already current
                } else {
                    cc.setIsCurrent(false);
                }
            }
            ChargeControllerInfo cc = Devices().get(position);
            Devices().get(position).setIsCurrent(true);
        }
        return true;
    }

    public void add(ChargeControllerInfo ccInfo) {
        ChargeController newCC = new ChargeController(ccInfo);
        synchronized (Devices()) {
            Devices().add(newCC);
        }
        BroadcastUpdateNotification();
    }

    public void remove(ChargeControllerInfo cc) {
        synchronized (Devices()) {
            cc.clearLogCache();
            Devices().remove(cc);
        }
        BroadcastUpdateNotification();
        BroadcastRemoveNotification(cc.uniqueId());
    }

    public int count() {
        synchronized (Devices()) {
            return Devices().size();
        }
    }

    // number of classics configured or the number of devices that provide day log data
    public int classicCount() {
        synchronized (Devices()) {
            int count = 0;
            for (ChargeController cc : Devices()) {
                if (cc.deviceType() == DeviceType.Classic) {
                    count++;
                }
            }
            return count;
        }
    }

    public void clear() {
        synchronized (Devices()) {
            Devices().clear();
        }
        BroadcastUpdateNotification();
    }

    public void load(ArrayAdapter adapter) {
        synchronized (Devices()) {
            adapter.addAll(Devices());
        }
    }

    public void load(ArrayList<InetSocketAddress> arr, boolean staticOnly) throws UnknownHostException {
        synchronized (Devices()) {
            for (ChargeController cc : Devices()) {
                if (cc.isCurrent()) {
                    arr.add(cc.getInetSocketAddress());
                } else if (!staticOnly || cc.isStaticIP()) { // all non current or all static non current
                    arr.add(cc.getInetSocketAddress());
                }
            }
        }
    }

    // modbus device
    public void setReachable(String deviceIpAddress, int port, boolean state) {
        boolean updated = false;
        synchronized (Devices()) {
            for (ChargeController cc : Devices()) {
                if (deviceIpAddress.compareTo(cc.deviceIpAddress()) == 0 && port == cc.port()) {
                    updated = cc.setIsReachable(state);
                    break;
                }
            }
        }
        if (updated) {
            BroadcastUpdateNotification();
        }
    }

    // MQTT device
    public void setReachable(String deviceName, boolean state) {
        boolean updated = false;
        synchronized (Devices()) {
            for (ChargeController cc : Devices()) {
                if (deviceName.compareTo(cc.deviceName()) == 0) {
                    updated = cc.setIsReachable(state);
                    break;
                }
            }
        }
        if (updated) {
            BroadcastUpdateNotification();
        }
    }

    private void BroadcastUpdateNotification() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS);
        broadcaster.sendBroadcast(pkg);
    }

    private void BroadcastRemoveNotification(String removedCC) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER);
        pkg.putExtra("uniqueId", removedCC);
        broadcaster.sendBroadcast(pkg);
    }


    public synchronized boolean isBidirectionalUnitsInWatts() {
        return bidirectionalUnitsInWatts;
    }

    public synchronized void setBidirectionalUnitsInWatts(boolean bidirectionalUnitsInWatts) {
        this.bidirectionalUnitsInWatts = bidirectionalUnitsInWatts;
    }

    public synchronized boolean useFahrenheit() {
        return useFahrenheit;
    }

    public synchronized void setFahrenheit(boolean useFahrenheit) {
        this.useFahrenheit = useFahrenheit;
    }

    public synchronized boolean autoDetectClassic() {

        return autoDetectClassic && connectionType != CONNECTION_TYPE.MQTT;
    }

    public synchronized void setAutoDetectClassic(boolean autoDetectClassic) {
        this.autoDetectClassic = autoDetectClassic;
    }

    public synchronized boolean showPopupMessages() {
        return showPopupMessages;
    }

    public synchronized void setShowPopupMessages(boolean showPopupMessages) {
        this.showPopupMessages = showPopupMessages;
    }

    public synchronized boolean showSystemView() {

        return systemViewEnabled && count() > 1;
    }

    public synchronized boolean systemViewEnabled() {
        return systemViewEnabled;
    }

    public synchronized void setSystemViewEnabled(boolean systemViewEnabled) {
        this.systemViewEnabled = systemViewEnabled;
    }

    public synchronized String aPIKey() {
        return APIKey;
    }

    public synchronized void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public synchronized Boolean uploadToPVOutput() {
        return uploadToPVOutput;
    }

    public synchronized void setUploadToPVOutput(Boolean uploadToPVOutput) {
        this.uploadToPVOutput = uploadToPVOutput;
    }

    public synchronized PVOutputSetting getPVOutputSetting() {
        if (systemViewEnabled) {
            return pVOutputSetting;
        }
        else {
            ChargeController controller = getCurrentChargeController();
            if (controller != null) {
                PVOutputSetting rSetting = controller.getPVOutputSetting();
                // Sid could have been set when in systemView
                if (rSetting.getSID() == null || rSetting.getSID().length() == 0) {
                    rSetting.setSID(pVOutputSetting.getSID());
                }
                return rSetting;
            }
            else {
                return null;
            }
        }
    }

    public void resetPVOutputLogs() {
        pVOutputSetting.resetPVOutputEntry();
        synchronized (Devices()) {
            for (ChargeController cc : Devices()) {
                cc.getPVOutputSetting().resetPVOutputEntry();
            }
        }
    }

    public void resetCurrentPVOutputLogs() {
        if (systemViewEnabled) {
            pVOutputSetting.resetPVOutputEntry();
        }
        else {
            ChargeController controller = getCurrentChargeController();
            if (controller != null) {
                controller.getPVOutputSetting().resetPVOutputEntry();
            }
        }
    }

    public synchronized CONNECTION_TYPE getConnectionType() {
        return connectionType;
    }

    public synchronized boolean setConnectionType(CONNECTION_TYPE val) {
        boolean hasChanged = this.connectionType != val;
        this.connectionType = val;
        return hasChanged;
    }

    public synchronized String mqttBrokerHost() {
        return mqttBrokerHost;
    }

    public synchronized boolean setMqttBrokerHost(String brokerHost) {
        boolean rVal = this.mqttBrokerHost != brokerHost;
        this.mqttBrokerHost = brokerHost;
        return rVal;
    }

    public synchronized int mqttPort() {
        return mqttPort;
    }

    public synchronized boolean setMqttPort(int mqttPort) {
        boolean rVal = this.mqttPort != mqttPort;
        this.mqttPort = mqttPort;
        return rVal;
    }

    public synchronized String mqttUser() {
        return mqttUser;
    }

    public synchronized boolean setMqttUser(String mqttUser) {
        boolean rVal = this.mqttUser != mqttUser;
        this.mqttUser = mqttUser;
        return rVal;
    }

    public synchronized String mqttRootTopic() {
        return mqttRootTopic;
    }

    public synchronized boolean setMqttRootTopic(String mqttRootTopic) {
        boolean rVal = this.mqttRootTopic != mqttRootTopic;
        this.mqttRootTopic = mqttRootTopic;
        return rVal;
    }

    public synchronized String mqttPassword() {
        return mqttPassword;
    }

    public synchronized boolean setMqttPassword(String mqttPassword) {
        boolean rVal = this.mqttPassword != mqttPassword;
        this.mqttPassword = mqttPassword;
        return rVal;
    }

}
