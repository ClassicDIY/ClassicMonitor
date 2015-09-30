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

    final transient Object lock = new Object();
    private boolean uploadToPVOutput;
    private String logDate; //date logs from classic were recorded for upload to PVOutput.org
    private String uploadDate; // last date the logs were uploaded to pvoutput.org
    private String SID; // pvoutput system id
    private boolean bidirectionalUnitsInWatts;
    private int dayLogMenuSelection;
    private int hourLogMenuSelection;

    // default ctor for de-serialization
    public ChargeController() {
        super();
    }

    public ChargeController(InetSocketAddress socketAddress) {
        super(socketAddress);
    }

    public ChargeController(ChargeControllerInfo ccInfo) {
        super(ccInfo.deviceUri(), ccInfo.deviceIpAddress(), ccInfo.port(), ccInfo.isStaticIP());
        super.setDeviceType(ccInfo.deviceType());
        super.setUnitID(ccInfo.unitID());
        super.setHasWhizbang(ccInfo.hasWhizbang());

    }

    @Override
    public synchronized String deviceIpAddress() {
            return super.deviceIpAddress();
    }

    public synchronized boolean setDeviceIP(String deviceIP) {
            return super.setDeviceIP(deviceIP);
    }

    public synchronized boolean setDeviceName(String deviceName) {
            return super.setDeviceName(deviceName);

    }

    public synchronized String deviceName() {
            return toString();
    }

    public synchronized int port() {
            return super.port();
    }

    public synchronized boolean setPort(int port) {
            return  super.setPort(port);
    }

    public synchronized boolean isStaticIP() {
            return super.isStaticIP();
    }

    public synchronized int unitID() {
            return super.unitID();
    }

    public synchronized boolean setUnitID(int unitID) {
            return super.setUnitID(unitID);
    }

    public synchronized DeviceType deviceType() {
            return super.deviceType();
    }

    public synchronized boolean setDeviceType(DeviceType deviceType) {
            return super.setDeviceType(deviceType);
    }

    public synchronized boolean hasWhizbang() {
            return super.hasWhizbang();
    }

    public synchronized boolean setHasWhizbang(boolean hasWhizbang) {
            return super.setHasWhizbang(hasWhizbang);
    }

    public synchronized boolean isBidirectionalUnitsInWatts() {
        return bidirectionalUnitsInWatts;
    }

    public synchronized void setBidirectionalUnitsInWatts(boolean bidirectionalUnitsInWatts) {
            this.bidirectionalUnitsInWatts = bidirectionalUnitsInWatts;
    }


    public synchronized int getHourLogMenuSelection() {
        return hourLogMenuSelection;
    }

    public synchronized void setHourLogMenuSelection(int hourLogMenuSelection) {
        this.hourLogMenuSelection = hourLogMenuSelection;
    }

    public synchronized int getDayLogMenuSelection() {
            return dayLogMenuSelection;
    }

    public synchronized void setDayLogMenuSelection(int dayLogMenuSelection) {
        this.dayLogMenuSelection = dayLogMenuSelection;
    }

    public synchronized String getSID() {
            return SID;
    }

    public synchronized void setSID(String SID) {
            this.SID = SID;

    }

    public synchronized String getPVOutputLogFilename() {
            return logDate;
    }

    public synchronized void setPVOutputLogFilename(String logDate) {
            this.logDate = String.format("PVOutput_%x_%s.log", unitID(), logDate) ;
    }


    public synchronized String uploadDate() {
            return uploadDate;
    }

    public synchronized void setUploadDate(String uploadDate) {
            this.uploadDate = uploadDate;
    }

    public synchronized Boolean uploadToPVOutput() {
            return uploadToPVOutput;
    }

    public synchronized void setUploadToPVOutput(Boolean uploadToPVOutput) {
            this.uploadToPVOutput = uploadToPVOutput;
    }

    public void resetPVOutputLogs() {
        String fname = getPVOutputLogFilename();
        if (fname != null && fname.length() > 0) {
            MonitorApplication.getAppContext().deleteFile(fname);
        }
        synchronized (lock) {
            uploadDate = "";
            logDate = "";
        }
    }
}

