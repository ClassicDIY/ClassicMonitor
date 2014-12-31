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
    private boolean isCurrent;
    private DeviceType deviceType;
    private transient boolean isReachable;
    private transient String model;
    private transient String macAddress;
    private transient float lastVOC;
    private transient String appVersion;
    private transient String netVersion;
    private transient String buildDate;
    private transient int nominalBatteryVoltage;

    public int getNominalBatteryVoltage() {
        return nominalBatteryVoltage;
    }

    public void setNominalBatteryVoltage(int nominalBatteryVoltage) {
        this.nominalBatteryVoltage = nominalBatteryVoltage;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    // default ctor for de-serialization
    public ChargeControllerInfo() {
    }

    public ChargeControllerInfo(String deviceIP, int port, boolean staticIP) {
        this.deviceIP = deviceIP;
        this.port = port;
        this.deviceType = DeviceType.Unknown;
        this.staticIP = staticIP;
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

    @Override
    public String toString() {
        return deviceName == null || deviceName.isEmpty() ? deviceIP == null || deviceIP.isEmpty() ? "ChargeController" : deviceIP : deviceName;
    }

    public String deviceIpAddress() {
        return deviceIP;
    }

    public boolean setDeviceIP(String deviceIP) {
        boolean rVal = this.deviceIP != deviceIP;
        this.deviceIP = deviceIP;
        return rVal;
    }

    public boolean setDeviceName(String deviceName) {
        boolean rVal = this.deviceName != deviceName;
        this.deviceName = deviceName;
        return rVal;
    }

    public String deviceName() {
        return toString();
    }

    public int port() {
        return port;
    }

    public boolean setPort(int port) {
        boolean rVal = this.port != port;
        this.port = port;
        return rVal;
    }

    public boolean isStaticIP() {
        return staticIP;
    }

    public int unitID() {
        return unitID;
    }

    public boolean setUnitID(int unitID) {
        boolean rval = this.unitID != unitID;
        this.unitID = unitID;
        return rval;
    }

    public DeviceType deviceType() {
        return deviceType;
    }

    public boolean setDeviceType(DeviceType deviceType) {
        boolean rVal = this.deviceType != deviceType;
        this.deviceType = deviceType;
        return rVal;
    }

    public boolean hasWhizbang() {
        return hasWhizbang;
    }

    public boolean setHasWhizbang(boolean hasWhizbang) {
        boolean rVal = this.hasWhizbang != hasWhizbang;
        this.hasWhizbang = hasWhizbang;
        return rVal;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(deviceIP, port);
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public boolean setIsReachable(boolean isReachable) {
        boolean rVal = this.isReachable != isReachable;
        this.isReachable = isReachable;
        return rVal;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setLastVOC(float lastVOC) {
        this.lastVOC = lastVOC;
    }

    public float getLastVOC() {
        return lastVOC;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setNetVersion(String netVersion) {
        this.netVersion = netVersion;
    }

    public String getNetVersion() {
        return netVersion;
    }

    public void updateBoilerPlate(Bundle info) {
        setModel(info.getString("model"));
        setMacAddress(info.getString("macAddress"));
        setLastVOC(info.getFloat("lastVOC"));
        setAppVersion(info.getString("appVersion"));
        setNetVersion(info.getString("netVersion"));
    }
}

