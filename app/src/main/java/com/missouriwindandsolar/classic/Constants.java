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

package com.missouriwindandsolar.classic;

public class Constants {

    public static final boolean DEVELOPER_MODE = false;

    // UDP listening port for available classics on the subnet
    public static final int CLASSIC_UDP_PORT = 4626;
    public static final int MODBUS_POLL_TIME = 2000;
    public static final int UDPListener_Maximum_Sleep_Time = 12000;
    public static final int UDPListener_Minimum_Sleep_Time = 100;

    public static final int MODBUS_FILE_MEMORY= 4;
    public static final int MODBUS_FILE_DAILIES_LOG = 5;
    public static final int MODBUS_FILE_MINUTES_LOG = 6;
    public static final int MODBUS_FILE_TIME_DATE_RISE_SET = 7;

    public static final int CLASSIC_KWHOUR_DAILY_CATEGORY = 0;
    public static final int CLASSIC_FLOAT_TIME_DAILY_CATEGORY = 2;
    public static final int CLASSIC_HIGH_POWER_DAILY_CATEGORY = 4;
    public static final int CLASSIC_HIGH_TEMP_DAILY_CATEGORY = 5;
    public static final int CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY = 7;
    public static final int CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY = 8;

    public static final int CLASSIC_POWER_HOURLY_CATEGORY = 0;
    public static final int CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY = 1;
    public static final int CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY = 2;
    public static final int CLASSIC_TIMESTAMP_LOW_HOURLY_CATEGORY = 3;
    public static final int CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY = 4;
    public static final int CLASSIC_CHARGE_STATE_HOURLY_CATEGORY = 5;
    public static final int CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY = 6;
    public static final int CLASSIC_ENERGY_HOURLY_CATEGORY = 7;


    public static final String UploadToPVOutput = "UploadToPVOutput";
    public static final String UseFahrenheit = "UseFahrenheit";
    public static final String AutoDetectClassic = "AutoDetectClassic";
    public static final String ShowPopupMessages = "ShowPopupMessages";
    public static final String SystemViewEnabled = "SystemViewEnabled";
    public static final String APIKey = "APIKey";
    public static final String SID = "SID";

    public static final int PVOUTPUT_RATE_LIMIT = 10000; // every n milliseconds
    public static final int PVOUTPUT_RECORD_LIMIT = 20; // max uploads per session

    // Intents
    public static final String CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS = "com.missouriwindandsolar.classic.DayLogs";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS = "com.missouriwindandsolar.classic.MinuteLogs";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS_SLAVE = "com.missouriwindandsolar.classic.DayLogs.slave";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_READINGS = "com.missouriwindandsolar.classic.Readings";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE = "com.missouriwindandsolar.classic.Readings.slave";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_TOAST = "com.missouriwindandsolar.classic.Toast";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER = "com.missouriwindandsolar.classic.AddChargeController";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER = "com.missouriwindandsolar.classic.RemoveChargeController";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS = "com.missouriwindandsolar.classic.UpdateChargeControllers";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER = "com.missouriwindandsolar.classic.MonitorChargeController";

}
