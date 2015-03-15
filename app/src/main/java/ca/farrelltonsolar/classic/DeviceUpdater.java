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

import android.util.Log;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.facade.ModbusTCPMaster;
import ca.farrelltonsolar.j2modlite.procimg.Register;

/**
 * Created by Graham on 14/03/2015.
 */

public class DeviceUpdater implements Runnable {

    ChargeControllerInfo chargeControllerInfo;

    public DeviceUpdater(ChargeControllerInfo cc) {
        chargeControllerInfo = new ChargeControllerInfo(cc);
    }

    @Override
    public void run() {
        Runnable r = new CheckReachableThread();
        new Thread(r).start();
    }

    class CheckReachableThread implements Runnable {


        CheckReachableThread() {
        }

        @Override
        public void run() {
            int retryCount = 10;
            do {
                try {
                    InetSocketAddress socketAddress = chargeControllerInfo.getInetSocketAddress();
                    try {
                        ModbusTCPMaster modbusMaster = new ModbusTCPMaster(chargeControllerInfo.getDeviceIp(), chargeControllerInfo.port());
                        Log.i(getClass().getName(), String.format("modbusMaster attempting to connect to %s ", chargeControllerInfo.toString()));
                        modbusMaster.connect();
                        if (modbusMaster.isConnected()) {
                            Log.i(getClass().getName(), String.format("Modbus connected to %s ", chargeControllerInfo.toString()));
                            try {
                                Log.d(getClass().getName(), "Updating name for: " + socketAddress.toString());
                                boolean foundTriStar = false;
                                try {
                                    Register[] registers = modbusMaster.readMultipleRegisters(4100, 4); // try classic first
                                    if (registers == null) {
                                        registers = modbusMaster.readMultipleRegisters(0, 4); // see if its a tristar
                                        if (registers != null && registers.length == 4) {
                                            foundTriStar = registers[0].toShort() != 0;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d(getClass().getName(), "This is probably not a Tristar!");
                                }
                                chargeControllerInfo.setDeviceType(foundTriStar ? DeviceType.TriStar : DeviceType.Classic);
                                boolean foundWhizBangJr = false;
                                try {
                                    Register[] registers = modbusMaster.readMultipleRegisters(4360, 12);
                                    if (registers != null && registers.length == 12) {
                                        Register a = registers[10];
                                        foundWhizBangJr = a.toShort() != 0;
                                    }
                                } catch (Exception e) {
                                    Log.d(getClass().getName(), "No Whizbang found!");
                                }
                                chargeControllerInfo.setHasWhizbang(foundWhizBangJr);
                                Register[] registers = modbusMaster.readMultipleRegisters(4209, 4);
                                if (registers != null && registers.length == 4) {
                                    byte[] v0 = registers[0].toBytes();
                                    byte[] v1 = registers[1].toBytes();
                                    byte[] v2 = registers[2].toBytes();
                                    byte[] v3 = registers[3].toBytes();

                                    byte[] temp = new byte[8];
                                    temp[0] = v0[1];
                                    temp[1] = v0[0];
                                    temp[2] = v1[1];
                                    temp[3] = v1[0];
                                    temp[4] = v2[1];
                                    temp[5] = v2[0];
                                    temp[6] = v3[1];
                                    temp[7] = v3[0];
                                    String unitName = new String(temp);
                                    chargeControllerInfo.setDeviceName(unitName);
                                    Log.i(getClass().getName(), String.format("Modbus device unitName is %s ", unitName));
                                }
                                int unitId = -1;
                                try {
                                    registers = modbusMaster.readMultipleRegisters(4110, 4);
                                    if (registers != null && registers.length == 4) {
                                        unitId = (registers[1].getValue() << 16) + registers[0].getValue();
                                        Log.i(getClass().getName(), String.format("Modbus device unitId is %s ", unitId));
                                    }
                                } catch (Exception unitIdEx) {
                                    Log.d(getClass().getName(), "Failed to get unitId" + unitIdEx);
                                }
                                chargeControllerInfo.setUnitID(unitId);
                                chargeControllerInfo.setIsReachable(true);
                                MonitorApplication.chargeControllers().update(chargeControllerInfo, true);
                                break;
                            } catch (ModbusException e) {
                                Log.d(getClass().getName(), "Failed to get unit info" + e);
                            } finally {
                                modbusMaster.disconnect();
                            }

                        } else {
                            Log.e(getClass().getName(), String.format("Modbus Connection refused to %s ", chargeControllerInfo.toString()));
                        }
                    } catch (Exception e) {
                        Log.e(getClass().getName(), String.format("Failed to connect to %s ex:%s", chargeControllerInfo.toString(), e));
                    }
                } catch (UnknownHostException e) {
                    Log.e(getClass().getName(), String.format("Failed to get address for %s ex:%s", chargeControllerInfo.toString(), e));
                }
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e2) {
                    Log.w(getClass().getName(), String.format("CheckReachableThread sleep Interrupted ex: %s", e2));
                    return;
                }
            } while (--retryCount > 0);
        }
    }
}
