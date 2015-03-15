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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class ChargeControllers {

    private static Context context;
    private String APIKey = "";
    private List<ChargeController> devices = new ArrayList<>();
    private boolean useFahrenheit;

    // default ctor for de-serialization
    public ChargeControllers() {
    }

    public ChargeControllers(Context context) {
        this.context = context;
    }

    public ChargeController get(int position) {
        synchronized (devices) {
            return devices.get(position);
        }
    }

    public ChargeController getCurrentChargeController() {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isCurrent()) {
                    return cc;
                }
            }
        }
        return null; // none selected
    }

    public int getCurrentControllerIndex() {
        synchronized (devices) {
            for (int index = 0; index < devices.size(); index++) {
                if (devices.get(index).isCurrent()) {
                    return index;
                }
            }
        }
        return -1; // none selected
    }

    public boolean setCurrent(int position) {
        if (position >= devices.size()) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (devices) {
            for (int index = 0; index < devices.size(); index++) {
                ChargeController cc = devices.get(index);
                if (cc.isCurrent() && index == position) {
                    return false; // already current
                } else {
                    cc.setIsCurrent(false);
                }
            }
            ChargeControllerInfo cc = devices.get(position);
            if (cc.deviceType() != DeviceType.Unknown) {
                devices.get(position).setIsCurrent(true);
            } else {
                return false; // can't do it, unknown device
            }
        }
        return true;
    }

    public void add(ChargeControllerInfo ccInfo) {
        ChargeController newCC = new ChargeController(ccInfo);
        synchronized (devices) {
            devices.add(newCC);
        }
        BroadcastUpdateNotification();
    }

    public void remove(ChargeControllerInfo cc) {
        synchronized (devices) {
            cc.clearLogCache();
            devices.remove(cc);
        }
        BroadcastUpdateNotification();
        BroadcastRemoveNotification();
    }

    public int count() {
        synchronized (devices) {
            return devices.size();
        }
    }

    public void clear() {
        synchronized (devices) {
            devices.clear();
        }
        BroadcastUpdateNotification();
    }

    public void load(ArrayAdapter adapter) {
        synchronized (devices) {
            adapter.addAll(devices);
        }
    }

    public void load(ArrayList<InetSocketAddress> arr, boolean staticOnly) throws UnknownHostException {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isCurrent()) {
                    arr.add(cc.getInetSocketAddress());
                } else if (!staticOnly || cc.isStaticIP()) { // all non current or all static non current
                    arr.add(cc.getInetSocketAddress());
                }
            }
        }
    }

    // update unit information
    public void update(ChargeControllerInfo ccToUpdate, boolean useUnitIdAsKey) throws UnknownHostException {
        int unitId = ccToUpdate.unitID();
        String unitName = ccToUpdate.deviceName();
        DeviceType deviceType = ccToUpdate.deviceType();
        boolean hasWhizbang = ccToUpdate.hasWhizbang();
        String deviceIpAddress = ccToUpdate.deviceIpAddress();
        int port = ccToUpdate.port();
        boolean updated = false;
        synchronized (devices) {
            for (ChargeController cc : devices) {
                String ipAddress = cc.getDeviceIp();
                if (useUnitIdAsKey ? cc.unitID() == unitId  && unitId != -1 : (ipAddress != null && (deviceIpAddress.compareTo(ipAddress) == 0 && port == cc.port()))) {
                    if (cc.setUnitID(unitId)) {
                        updated = true;
                    }
                    if (cc.setDeviceName(unitName)) {
                        updated = true;
                    }
                    if (cc.setDeviceIP(deviceIpAddress)) {
                        updated = true;
                    }
                    if (cc.setPort(port)) {
                        updated = true;
                    }
                    if (cc.setDeviceType(deviceType)) {
                        updated = true;
                    }
                    if (cc.setHasWhizbang(hasWhizbang)) {
                        updated = true;
                    }
                    if (cc.setIsReachable(true)) {
                        updated = true;
                    }
                    break;
                }
            }
        }
        if (updated) {
            BroadcastUpdateNotification();
        } else if (useUnitIdAsKey) { // retry matching on IP address if no cc matched UnitID
            update(ccToUpdate, false);
        }
    }

    public void setReachable(String deviceIpAddress, int port, boolean state) {
        boolean updated = false;
        synchronized (devices) {
            for (ChargeController cc : devices) {
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

    public void clearDynamic() {
        List<ChargeController> staticDevices = new ArrayList<>();
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isStaticIP()) {
                    staticDevices.add(cc);
                }
            }
            devices = staticDevices;
        }
        BroadcastUpdateNotification();
    }

    private void BroadcastUpdateNotification() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS);
        broadcaster.sendBroadcast(pkg);
    }

    private void BroadcastRemoveNotification() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER);
        broadcaster.sendBroadcast(pkg);
    }


    public synchronized boolean useFahrenheit() {
        return useFahrenheit;
    }

    public synchronized void setFahrenheit(boolean useFahrenheit) {
        this.useFahrenheit = useFahrenheit;
    }

    public synchronized String aPIKey() {
        return APIKey;
    }

    public synchronized void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public void resetPVOutputLogs() {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                cc.resetPVOutputLogs();
            }
        }
    }

    // true if any cc is uploading
    public boolean uploadToPVOutput() {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.uploadToPVOutput()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateUnknownStatic() {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isStaticIP() && cc.deviceType() == DeviceType.Unknown) {
                    DeviceUpdater updater = new DeviceUpdater(cc);
                    updater.run();
                }
            }
        }
        return;
    }
}
