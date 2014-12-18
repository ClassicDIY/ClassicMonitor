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
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public final class ChargeControllers {

    final Object lock = new Object();
    private static Context context;
    private int currentController = -1;

    public ChargeControllers(Context context) {
        this.context = context;
        this.devices = new ArrayList<>();
    }

    private List<ChargeController> devices;

    public ChargeController get(int position) {
        synchronized (lock) {
            return devices.get(position);
        }
    }

    public ChargeController getCurrentChargeController() {
        synchronized (lock) {
            if (currentController != -1 && currentController < devices.size()) {
                return devices.get(currentController);
            }
        }
        return null; // none selected
    }

    public int getCurrentControllerIndex() {
        return currentController;
    }

    public boolean setCurrent(int position) {
        if (position >= devices.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (currentController == position) {
            return false; // already current
        }
        currentController = position;
        return true;
    }

    public void add(ChargeController cc) {
        synchronized (lock) {
            devices.add(cc);
        }
        BroadcastChangeNotification();
    }

    public void remove(ChargeController cc) {
        synchronized (lock) {
            if (devices.indexOf(cc) == currentController) {
                currentController = -1;
            }
            devices.remove(cc);
        }
        BroadcastChangeNotification();
    }

    public int count() {
        synchronized (lock) {
            return devices.size();
        }
    }

    public void clear() {
        synchronized (lock) {
            devices.clear();
            currentController = -1;
        }
        BroadcastChangeNotification();
    }

    public void load(ArrayAdapter adapter) {
        synchronized (lock) {
            adapter.addAll(devices);
        }
    }

    public void load(ArrayList<InetSocketAddress> arr, boolean staticOnly) {
        synchronized (lock) {
            for (ChargeController cc : devices) {
                if (!staticOnly || cc.isStaticIP()) {
                    arr.add(cc.getInetSocketAddress());
                }
            }
        }
    }

    // update unit information
    public void update(Bundle info, String deviceIpAddress, int port, boolean useUnitIdAsKey) {
        int unitId = info.getInt("UnitID");
        String unitName = info.getString("UnitName");
        DeviceType deviceType = (DeviceType) info.getSerializable("DeviceType");
        boolean hasWhizbang = info.getBoolean("FoundWhizbang");
        synchronized (lock) {
            for (ChargeController cc : devices) {
                if (useUnitIdAsKey ? cc.unitID() == unitId : deviceIpAddress.compareTo(cc.deviceIpAddress()) == 0) {
                    cc.setUnitID(unitId);
                    cc.setDeviceName(unitName);
                    cc.setDeviceIP(deviceIpAddress);
                    cc.setPort(port);
                    cc.setDeviceType(deviceType);
                    cc.setHasWhizbang(hasWhizbang);
                    break;
                }
            }
        }
        BroadcastChangeNotification();
    }


    public void clearDynamic() {
        List<ChargeController> staticDevices = new ArrayList<>();
        synchronized (lock) {
            for (ChargeController cc : devices) {
                if (cc.isStaticIP()) {
                    staticDevices.add(cc);
                }
            }
            devices = staticDevices;
        }
        BroadcastChangeNotification();
    }

    private void BroadcastChangeNotification() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS);
        broadcaster.sendBroadcast(pkg);
    }
}
