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
 * Thread safe ChargeController used to save settings of each device
 */
public class ChargeController extends ChargeControllerInfo {

    final Object lock = new Object();
    private String logDate; //date logs from classic were recorded for upload to PVOutput.org
    private String uploadDate; // last date the logs were uploaded to pvoutput.org
    private String SID; // pvoutput system id
    private boolean bidirectionalUnitsInWatts;

    // default ctor for de-serialization
    public ChargeController() {
        super();
    }

    public ChargeController(InetSocketAddress socketAddress) {
        super(socketAddress);
    }

    public ChargeController(ChargeControllerInfo ccInfo) {
        super(ccInfo.deviceIpAddress(), ccInfo.port(), ccInfo.isStaticIP());
        super.setDeviceType(ccInfo.deviceType());
        super.setUnitID(ccInfo.unitID());
        super.setHasWhizbang(ccInfo.hasWhizbang());

    }

    @Override
    public String deviceIpAddress() {
        synchronized (lock) {
            return super.deviceIpAddress();
        }
    }

    public boolean setDeviceIP(String deviceIP) {
        synchronized (lock) {
            return super.setDeviceIP(deviceIP);
        }
    }

    public boolean setDeviceName(String deviceName) {
        synchronized (lock) {
            return super.setDeviceName(deviceName);
        }
    }

    public String deviceName() {
        synchronized (lock) {
            return toString();
        }
    }

    public int port() {
        synchronized (lock) {
            return super.port();
        }
    }

    public boolean setPort(int port) {
        synchronized (lock) {
            return  super.setPort(port);
        }
    }

    public boolean isStaticIP() {
        synchronized (lock) {
            return super.isStaticIP();
        }
    }

    public int unitID() {
        synchronized (lock) {
            return super.unitID();
        }
    }

    public boolean setUnitID(int unitID) {
        synchronized (lock) {
            return super.setUnitID(unitID);
        }
    }

    public DeviceType deviceType() {
        synchronized (lock) {
            return super.deviceType();
        }
    }

    public boolean setDeviceType(DeviceType deviceType) {
        synchronized (lock) {
            return super.setDeviceType(deviceType);
        }
    }

    public boolean hasWhizbang() {
        synchronized (lock) {
            return super.hasWhizbang();
        }
    }

    public boolean setHasWhizbang(boolean hasWhizbang) {
        synchronized (lock) {
            return super.setHasWhizbang(hasWhizbang);
        }
    }

    public boolean isBidirectionalUnitsInWatts() {
        synchronized (lock) {
            return bidirectionalUnitsInWatts;
        }
    }

    public void setBidirectionalUnitsInWatts(boolean bidirectionalUnitsInWatts) {
        synchronized (lock) {
            this.bidirectionalUnitsInWatts = bidirectionalUnitsInWatts;
        }
    }

    public String getSID() {
        synchronized (lock) {
            return SID;
        }
    }

    public void setSID(String SID) {
        synchronized (lock) {
            this.SID = SID;
        }
    }

    public String logDate() {
        synchronized (lock) {
            return logDate;
        }
    }

    public void setLogDate(String logDate) {
        synchronized (lock) {
            this.logDate = logDate;
        }
    }


    public String uploadDate() {
        synchronized (lock) {
            return uploadDate;
        }
    }

    public void setUploadDate(String uploadDate) {
        synchronized (lock) {
            this.uploadDate = uploadDate;
        }
    }

    public void resetPVOutputLogs() {
        String fname = logDate();
        if (fname.length() > 0) {
            MonitorApplication.getAppContext().deleteFile(fname);
        }
        synchronized (lock) {
            uploadDate = "";
            logDate = "";
        }
    }
}

