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

import android.os.Bundle;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by Graham on 10/12/2014.
 * Serializable version on ChargeController, used by UDPListeners to broadcast new devices
 */
public class ChargeControllerInfo implements Serializable {

    private int unitID;
    private String deviceIP;
    private String deviceName;
    private int port;
    private boolean staticIP;
    private boolean hasWhizbang;
    private DeviceType deviceType;

    public ChargeControllerInfo(String deviceIP, int port, boolean staticIP) {
        this.deviceIP = deviceIP;
        this.port = port;
        this.staticIP = staticIP;
    }

    public ChargeControllerInfo(Bundle info, String deviceIP, int port, boolean staticIP) {
        this(deviceIP, port, staticIP);
        setDeviceName(info.getString("UnitName"));
        setUnitID(info.getInt("UnitID"));
        setDeviceType((DeviceType) info.getSerializable("DeviceType"));
        setHasWhizbang(info.getBoolean("FoundWhizbang"));
    }

    public ChargeControllerInfo(InetSocketAddress socketAddress) {
        this.unitID = -1;
        this.deviceIP = socketAddress.getAddress().getHostAddress();
        this.deviceName = "";
        this.port = socketAddress.getPort();
        this.staticIP = false;
        this.hasWhizbang = false;
        this.deviceType = DeviceType.Unknown;
    }

    public ChargeControllerInfo() {

    }

    @Override
    public String toString() {
        return deviceName == null || deviceName.isEmpty() ? deviceIP == null || deviceIP.isEmpty() ? "ChargeController" : deviceIP : deviceName;
    }

    public String deviceIpAddress() {
            return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
            this.deviceIP = deviceIP;
    }

    public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
    }

    public String deviceName() {
        return toString();
    }

    public int port() {
            return port;
    }

    public void setPort(int port) {
            this.port = port;
    }

    public boolean isStaticIP() {
            return staticIP;
    }

    public int unitID() {
            return unitID;
    }

    public void setUnitID(int unitID) {
            this.unitID = unitID;
    }

    public DeviceType deviceType() {
            return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
    }

    public boolean hasWhizbang() {
            return hasWhizbang;
    }

    public void setHasWhizbang(boolean hasWhizbang) {
            this.hasWhizbang = hasWhizbang;
    }

    public InetSocketAddress getInetSocketAddress() {
            return new InetSocketAddress(deviceIP, port);
    }
}

