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

import java.net.InetSocketAddress;

/**
 * Created by Graham on 10/12/2014.
 */
public class ChargeController {
    public ChargeController(String deviceIP, int port, boolean staticIP) {
        this.deviceIP = deviceIP;
        this.port = port;
        this.staticIP = staticIP;
    }

    public ChargeController(InetSocketAddress socketAddress) {
        this.deviceIP = socketAddress.getAddress().getHostAddress();
        this.deviceName = "";
        this.port = socketAddress.getPort();
        this.staticIP = false;
    }

    private int unitID;
    private String deviceIP;
    private String deviceName;
    private int port;
    private boolean staticIP;

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

    public InetSocketAddress getInetSocketAddress() {
        InetSocketAddress address = new InetSocketAddress(deviceIP, port);
        return address;
    }
}

