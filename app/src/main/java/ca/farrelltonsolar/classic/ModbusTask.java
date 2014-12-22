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
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.facade.ModbusTCPMaster;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersResponse;
import ca.farrelltonsolar.j2modlite.procimg.Register;

// Classic modbus table
//            new Register { Address = 4115, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4116, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4117, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4118, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4119, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = address => U16(address)},
//            new Register { Address = 4120, Label = "Battery charge state", UnitOfMeasure = "", Conversion = address => ChargeState(address)},
//            new Register { Address = 4121, Label = "Average PV inout current", UnitOfMeasure = "Amps", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4122, Label = "PV VOC", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4125, Label = "Daily amp hours", UnitOfMeasure = "Amp hours", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4126, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = address => U32_OneDec(address)},
//            new Register { Address = 4128, Label = "Total Amp hours", UnitOfMeasure = "Amp hours", Conversion = address => U32_OneDec(address)},
//            new Register { Address = 4130, Label = "Info flag", UnitOfMeasure = "", Conversion = address => Info(address)}
//            new Register { Address = 4132, Label = "BATTemperature", UnitOfMeasure = "", Conversion = address => U16_OneDec(address)}
//            new Register { Address = 4133, Label = "FETTemperature", UnitOfMeasure = "", Conversion = address => U16_OneDec(address)}
//            new Register { Address = 4134, Label = "PCBTemperature", UnitOfMeasure = "", Conversion = address => U16_OneDec(address)}

//            new Tupple { Description = "(Off) No power, waiting for power source, battery voltage over set point.", Value = 0 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 3 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 4 },
//            new Tupple { Description = "(Float) Battery is full and regulating battery voltage at float set point.", Value = 5 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 6 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 7 },
//            new Tupple { Description = "(Error) Input voltage is above maximum classic operating voltage.", Value = 10 },
//            new Tupple { Description = "(Equalizing) Max power point tracking. Seeking equalize set point voltage.", Value = 18 }


// TriStar modbus table
//            new Register { Address = 1, Label = "V Scale", UnitOfMeasure = "", Conversion = address => U32(address)},
//            new Register { Address = 3, Label = "A Scale", UnitOfMeasure = "", Conversion = address => U32(address)},
//            new Register { Address = 25, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = address => VScale(address)},
//            new Register { Address = 28, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = address => VScale(address)},
//            new Register { Address = 29, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = address => IScale(address)},
//            new Register { Address = 30, Label = "Average PV current", UnitOfMeasure = "Amps", Conversion = address => IScale(address)},
//            new Register { Address = 45, Label = "Info flag", UnitOfMeasure = "", Conversion = address => Info(address)},
//            new Register { Address = 51, Label = "Battery charge state", UnitOfMeasure = "", Conversion = address => ChargeState(address)},
//            new Register { Address = 58, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = address => U16(address)},
//            new Register { Address = 59, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = address => PScale(address)},
//            new Register { Address = 69, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = address => WHr(address)}

//            new Tupple { Description = "(Start) System startup.", Value = 0 },
//            new Tupple { Description = "(Night check) No power, detecting nightfall.", Value = 1 },
//            new Tupple { Description = "(Disconnected) No power.", Value = 2 },
//            new Tupple { Description = "(Night) No power, waiting for power source.", Value = 3 },
//            new Tupple { Description = "(Fault) Detected fault.", Value = 4 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 5 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 6 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 7 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 8 },
//            new Tupple { Description = "(Slave) State set by master charge controller.", Value = 9 }

/**
 * Created by Graham on 12/12/2014.
 */
public class ModbusTask extends TimerTask {

    final Object lock = new Object();

    public ModbusTask(ChargeController cc, Context ctx) {
        chargeController = cc;
        context = ctx;
        readings = new Readings();
        dayLogEntry = new LogEntry();
        minuteLogEntry = new LogEntry();
    }

    private Context context;
    private ModbusTCPMaster modbusMaster;
    private ChargeController chargeController;
    private int reference = 4100; //the reference; offset where to start reading from
    private Readings readings;
    private LogEntry dayLogEntry;
    private LogEntry minuteLogEntry;
    private float v_pu;
    private float i_pu;
    private boolean foundWhizBangJr = false;
    private boolean foundTriStar = false;
    private boolean initialReadingLoaded = false;
    private boolean disconnecting = false;

    public boolean connect() throws UnknownHostException {
        boolean rVal = false;
        InetAddress inetAddress = InetAddress.getByName(chargeController.deviceIpAddress());
        Log.d(getClass().getName(), String.format("Connecting to %s", inetAddress.toString()));
        try {
            disconnect();
            modbusMaster = new ModbusTCPMaster(inetAddress, chargeController.port(), 1);
            modbusMaster.setRetries(Constants.MODBUS_RETRIES);
            modbusMaster.connect();
            if (modbusMaster.isConnected()) {
                disconnecting = false;
                rVal = true;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            Log.w(getClass().getName(), String.format("Could not connect to %s, ex: %s", chargeController.toString(), e1));
            disconnecting = false;
            modbusMaster = null;
        }
        return rVal;
    }

    public void disconnect() {
        disconnecting = true;
        if (isConnected()) {
            synchronized (lock) {
                if (modbusMaster != null) {
                    modbusMaster.disconnect();
                }
                modbusMaster = null;
            }
            Log.d(getClass().getName(), String.format("Disconnected from %s", chargeController.toString()));
        }
    }

    public boolean isConnected() {
        boolean rVal = false;
        if (modbusMaster != null) {
            rVal = modbusMaster.isConnected();
        }
        return rVal;
    }

    @Override
    public void run() {

//        Log.d(getClass().getName(), "begin run");
        try {
            synchronized (lock) {
                if (disconnecting) {
                    return;
                }
                boolean connected = isConnected();
                if (connected == false) {
                    connected = connect();
                }
                if (connected) {
                    if (initialReadingLoaded == false) {
                        initialReadingLoaded = true;
                        if (LookForTriStar() == DeviceType.Classic) {
                            LookForWhizBangJr();
                            BroadcastUnitName();
                        }
                    }
                    GetModbusReadings();
                    if (!foundTriStar) {
                        if (dayLogEntry.isEmpty()) {
                            try {
                                LoadDayLogs();
                            } catch (Exception ex) {
                                Log.w(getClass().getName(), String.format("Failed to load Day logs due to exception ex: %s", ex));
                                dayLogEntry = new LogEntry();
                            }

                        }
                        if (minuteLogEntry.isEmpty()) {
                            try {
                                LoadMinuteLogs();
                            } catch (Exception ex) {
                                Log.w(getClass().getName(), String.format("Failed to load Minute logs due to exception ex: %s", ex));
                                minuteLogEntry = new LogEntry();
                            }
                        }
                    }
                }
            }
            dayLogEntry.broadcastLogs(context, Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS);
            minuteLogEntry.broadcastLogs(context, Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS);

        } catch (Exception e1) {
            if (isConnected()) {
                if (modbusMaster != null) {
                    modbusMaster.disconnect();
                }
                modbusMaster = null;
                Log.w(getClass().getName(), String.format("Could not get readings, disconnected from %s due to exception ex: %s", chargeController.toString(), e1));
            } else {
                Log.w(getClass().getName(), String.format("Could not get readings from %s, was disconnected ex: %s", chargeController.toString(), e1));
            }
        }
//        Log.d(getClass().getName(), "end run");

    }

    public void clearReadings() {
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
        readings.broadcastReadings(context, Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS);
    }

    private void GetModbusReadings() throws ModbusException {
        try {
            if (foundTriStar) {
                ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(0, 80);
                if (regRes != null) {

                    readings.set(RegisterName.BatVoltage, VScale(regRes.getRegisterValue(OffsetFor(25))));
                    readings.set(RegisterName.PVVoltage, VScale(regRes.getRegisterValue(OffsetFor(28))));
                    readings.set(RegisterName.BatCurrent, IScale(regRes.getRegisterValue(OffsetFor(29))));
                    readings.set(RegisterName.PVCurrent, IScale(regRes.getRegisterValue(OffsetFor(30))));

                    readings.set(RegisterName.Power, PScale(regRes.getRegisterValue(OffsetFor(59))));
                    readings.set(RegisterName.EnergyToday, WHr(regRes.getRegisterValue(OffsetFor(69))));
                    readings.set(RegisterName.TotalEnergy, regRes.getRegisterValue(OffsetFor(58)));

                }
            } else {
                ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(reference, 36);
                if (regRes != null) {
                    if (!foundWhizBangJr) {
                        Register a = regRes.getRegister(OffsetFor(4117));
                        readings.set(RegisterName.BatCurrent, a.toShort() / 10.0f);
                    }
                    readings.set(RegisterName.Power, (float) regRes.getRegisterValue(OffsetFor(4119)));
                    readings.set(RegisterName.BatVoltage, regRes.getRegisterValue(OffsetFor(4115)) / 10.0f);
                    readings.set(RegisterName.PVVoltage, regRes.getRegisterValue(OffsetFor(4116)) / 10.0f);
                    readings.set(RegisterName.PVCurrent, regRes.getRegisterValue(OffsetFor(4121)) / 10.0f);
                    readings.set(RegisterName.EnergyToday, regRes.getRegisterValue(OffsetFor(4118)) / 10.0f);
                    readings.set(RegisterName.TotalEnergy, ((regRes.getRegisterValue(OffsetFor(4127)) << 16) + regRes.getRegisterValue(OffsetFor(4126))) / 10.0f);
                    readings.set(RegisterName.ChargeState, MSBFor(regRes.getRegisterValue(OffsetFor(4120))));
                    readings.set(RegisterName.BatTemperature, regRes.getRegisterValue(OffsetFor(4132)) / 10.0f);
                    readings.set(RegisterName.FETTemperature, regRes.getRegisterValue(OffsetFor(4133)) / 10.0f);
                    readings.set(RegisterName.PCBTemperature, regRes.getRegisterValue(OffsetFor(4134)) / 10.0f);
                    int infoFlag = regRes.getRegisterValue(OffsetFor(4130));
                    readings.set(RegisterName.Aux1, (infoFlag & 0x4000) != 0);
                    readings.set(RegisterName.Aux2, (infoFlag & 0x8000) != 0);
                } else {
                    Log.w(getClass().getName(), String.format("Modbus readMultipleRegisters returned null"));
                    throw new ModbusException("Failed to read data from modbus");
                }
                if (foundWhizBangJr) {
                    ReadMultipleRegistersResponse regRes2 = modbusMaster.readMultipleRegisters(4360, 16);
                    if (regRes2 != null) {
                        Register a = regRes2.getRegister(10);
                        readings.set(RegisterName.BatCurrent, a.toShort() / 10.0f);
                        Register soc = regRes2.getRegister(12);
                        short socVal = soc.toShort();
                        readings.set(RegisterName.SOC, socVal);
                    }
                }
                readings.set(RegisterName.BiDirectional, foundWhizBangJr);
            }
            readings.broadcastReadings(context, Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS);

        } catch (ModbusException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception all) {
            all.printStackTrace();
            throw new ModbusException(all.getMessage());
        }
    }

    private void BroadcastToast(String message) {
        Intent intent2 = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_TOAST);
        intent2.setClass(context, ReadingFramentBase.class);
        intent2.putExtra("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
    }

    public Bundle getChargeControllerInformation() throws ModbusException {
        Bundle result = new Bundle();
        DeviceType deviceType = LookForTriStar();
        result.putSerializable("DeviceType", deviceType);
        if (deviceType == DeviceType.Classic) {
            result.putString("UnitName", getUnitName());
            result.putInt("UnitID", getUnitID());
            result.putBoolean("FoundWhizbang", LookForWhizBangJr());
        } else {
            result.putString("UnitName", "Tristar");
            result.putInt("UnitID", 0);
        }
        return result;
    }

    private String getUnitName() throws ModbusException {
        ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(4209, 4);
        if (regRes != null) {
            byte[] v0 = regRes.getRegister(0).toBytes();
            byte[] v1 = regRes.getRegister(1).toBytes();
            byte[] v2 = regRes.getRegister(2).toBytes();
            byte[] v3 = regRes.getRegister(3).toBytes();

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
            return unitName.trim();
        }
        return "";
    }

    private boolean LookForWhizBangJr() throws ModbusException {
        foundWhizBangJr = false;
        ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(4360, 12);
        if (regRes != null) {
            Register a = regRes.getRegister(10);
            foundWhizBangJr = a.toShort() != 0;
        }
        return foundWhizBangJr;
    }

    private DeviceType LookForTriStar() {
        foundTriStar = false;
        try {
            ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(0, 4);
            if (regRes != null) {
                foundTriStar = regRes.getRegister(0).toShort() != 0;
                if (foundTriStar) {
                    float hi = (float) regRes.getRegister(0).toShort();
                    float lo = (float) regRes.getRegister(1).toShort();
                    lo = lo / 65536;
                    v_pu = hi + lo;

                    hi = (float) regRes.getRegister(2).toShort();
                    lo = (float) regRes.getRegister(3).toShort();
                    lo = lo / 65536;
                    i_pu = hi + lo;
                    reference = 0;
                }
            }
        } catch (ModbusException e) {
            Log.w(getClass().getName(), "This is probably not a Tristar!");
        }

        return foundTriStar ? DeviceType.TriStar : DeviceType.Classic;
    }

    private int getUnitID() throws ModbusException {
        int unitId = -1;
        ReadMultipleRegistersResponse regRes = modbusMaster.readMultipleRegisters(4110, 4);
        if (regRes != null) {
            unitId = (regRes.getRegisterValue(1) << 16) + regRes.getRegisterValue(0);
        }
        return unitId;
    }

    private void BroadcastUnitName() throws ModbusException {
        Intent intent = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_UNIT_NAME);
        intent.putExtra("UnitName", getUnitName());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void LoadDayLogs() throws ModbusException {
        dayLogEntry.set(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
        dayLogEntry.set(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
        try {
            dayLogEntry.set(Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY, ReadLogs(365, Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
        }
        catch (Exception ex){
            Log.w(getClass().getName(), String.format("LoadDayLogs failed, but did get power & float series ex: %s", ex));
        }

    }

    private void LoadMinuteLogs() throws ModbusException {
        try {
            int requiredEntries = ReadMinuteLogTimestamps(); // sum of minutes log up to 24 hours
            minuteLogEntry.set(Constants.CLASSIC_POWER_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_POWER_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 1));
            minuteLogEntry.set(Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_ENERGY_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_ENERGY_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 1));
            minuteLogEntry.set(Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 256));
            Log.d(getClass().getName(), "Completed reading minute logs");
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("LoadMinuteLogs failed ex: %s", ex));
        }
    }

    private float[] ReadLogs(int requiredEntries, int category, int device, int factor) throws ModbusException {
        int index = 0;
        float[] buffer = new float[requiredEntries];
        while (index < requiredEntries) {
            ReadFileTransferResponse regRes = modbusMaster.readFileTransfer(index, category, device);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    int j = count - 1;
                    for (int i = 0; i < count; i++, j--) {
                        if (i + index > requiredEntries - 1) {
                            break;
                        }
                        buffer[i + index] = registerToShort(regRes.getRegister(j).toBytes()) / factor;
                    }
                    index += count;
                }
            } else {
                Log.w(getClass().getName(), String.format("Modbus ReadLogs failed to get category: %d", category));
                break;
//                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }

        return buffer;
    }

    private int ReadMinuteLogTimestamps() throws ModbusException {
        final int bufferSize = 1440; // assume max of one entry per minute for 20 hrs
        int requiredEntries = 0;
        int index = 0;
        short lasMinute;
        short currentMinute = -1;
        short minuteSum = 0;
        int minuteStamp = 0;
        int hourStamp = 0;
        short[] buffer = new short[bufferSize];
        while (index < bufferSize) {
            ReadFileTransferResponse regRes = modbusMaster.readFileTransfer(index, Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    int j = count - 1;
                    for (int i = 0; i < count; i++, j--) {
                        lasMinute = currentMinute;
                        if (i + index > bufferSize - 1) {
                            break;
                        }
                        short val = registerToShort(regRes.getRegister(j).toBytes());
                        short min = (short) (val & 0x003f);
                        short hour = (short) ((val >> 6) & 0x001f);
                        currentMinute = (short) (min + hour * 60);

                        if (lasMinute != -1) {
                            if (currentMinute > lasMinute) {
                                lasMinute += 1440; // roll over midnight
                            }
                            minuteSum += lasMinute - currentMinute;
                            buffer[i + index] = minuteSum;
                            if (minuteSum > 1440) { //minutes in 24 hours
                                requiredEntries = i + index; // output buffer size required
                                index = bufferSize; //exit while
                                break; //exit for
                            }
                        } else {
                            minuteStamp = min;
                            hourStamp = hour;
                        }
                    }
                    index += count;
                }
            } else {
                Log.w(getClass().getName(), String.format("Modbus ReadLogs failed to get timestamps"));
                break;
//                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }
        short[] output = new short[requiredEntries];
        System.arraycopy(buffer, 0, output, 0, requiredEntries);
        minuteLogEntry.set(Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, output);
        ReadFileTransferResponse regRes = modbusMaster.readFileTransfer(0, Constants.CLASSIC_TIMESTAMP_LOW_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG);
        if (regRes != null) {
            int count = regRes.getWordCount();
            if (count > 0) {
                short val = registerToShort(regRes.getRegister(count - 1).toBytes());
                short month = (short) (val & 0x000f);
                short day = (short) ((val >> 4) & 0x001f);
                short year = (short) ((val >> 9) & 0x007e);
                GregorianCalendar logDate = new GregorianCalendar(year + 2000, month - 1, day, hourStamp, minuteStamp);
                minuteLogEntry.setLogDate(logDate);
            }
        }
        return requiredEntries;
    }

    private static short registerToShort(byte[] bytes) {
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xff));
    }



    // Tristar code...
    private int OffsetFor(int address) {
        return address - reference - 1; // origin 0
    }

    private int MSBFor(int val) {
        return val >> 8;
    }


    private float WHr(float val) {
        val /= 1000;
        return val;
    }

    private float PScale(float val) {
        val = val * v_pu * i_pu;
        val /= 131072;
        return val;
    }

    private float VScale(float val) {
        val = val * v_pu;
        val /= 32768;
        return val;
    }

    private float IScale(float val) {
        val = val * i_pu;
        val /= 32768;
        return val;
    }
    // END Tristar code...
}
