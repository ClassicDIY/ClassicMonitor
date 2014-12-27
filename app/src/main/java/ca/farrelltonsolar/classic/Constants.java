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

public class Constants {

    public static final boolean DEVELOPER_MODE = true;

    // UDP listening port for available classics on the subnet
    public static final int CLASSIC_UDP_PORT = 4626;
    public static final int MODBUS_RETRIES = 2;
    public static final int MODBUS_POLL_TIME = 2000;
    public static final int UDPListener_Maximum_Sleep_Time = 6000;
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

    public static final String PORT_PREFERENCE = "PortSetting";

    public static final String BIDIRECTIONALUNIT_PREFERENCE = "BidirectionalUnitInWatts";

    public static final String UploadToPVOutput = "UploadToPVOutput";
    public static final String APIKey = "APIKey";
    public static final String SID = "SID";

    public static final int PVOUTPUT_RATE_LIMIT = 10000; // every n milliseconds
    public static final int PVOUTPUT_RECORD_LIMIT = 20; // max uploads per session

    // Intents
    public static final String CA_FARRELLTONSOLAR_CLASSIC_PVOUTPUT_UPLOAD = "ca.farrelltonsolar.classic.action.pvoutput";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS = "ca.farrelltonsolar.classic.DayLogs";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS = "ca.farrelltonsolar.classic.MinuteLogs";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_READINGS = "ca.farrelltonsolar.classic.Readings";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_TOAST = "ca.farrelltonsolar.classic.Toast";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER = "ca.farrelltonsolar.classic.AddChargeController";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS = "ca.farrelltonsolar.classic.UpdateChargeControllers";
    public static final String CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER = "ca.farrelltonsolar.classic.MonitorChargeController";


    public static final String Calendar_html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n<html>\n<head>\n     <title id='Description'>Calendar</title>\n" +
            "    <link rel='stylesheet' type='text/css' href='file:///android_asset/%s.css' />\n" +
            "    <script type=\"text/javascript\" src=\"file:///android_asset/jquery.min.js\"></script>\n" +
            "    <script type='text/javascript' src='file:///android_asset/fullcalendar.js'></script>\n" +
            "    <script type='text/javascript' src='file:///android_asset/jquery-ui.custom.min.js'></script>\n" +
            "    <script type='text/javascript'>\n" +
            "\n" +
            "        $(document).ready(function () {\n" +
            "\n" +
            "$('#calendar').fullCalendar({\n" +
            "                header: {\n" +
            "                    left: 'prev,next today',\n" +
            "                    center: 'title',\n" +
            "                    right: ''\n" +
            "                },\n" +
            "                aspectRatio: 2.6,\n" +
            "                editable: false,\n" +
            "                selectable: false,\n" +
            "                 events: '/events',\n" +
            "                 monthNames: [%s],\n" +
            "                 monthNamesShort: [%s],\n" +
            "                 dayNames: [%s],\n" +
            "                 dayNamesShort: [%s]\n" +
            "            });\n" +
            "        });\n" +
            "\n" +
            "    </script>\n" +
            "</head>\n" +
            "\n" +
            "\n" +
            "<div id=\"container\" >\n" +
            "    <!--<button onClick=\"showAndroidToast()\">Show Toast</button>-->\n" +
            "    <!--<script type=\"text/javascript\">-->\n" +
            "    <!--function showAndroidToast(){-->\n" +
            "\n" +
            "    <!--MainActivityInterface.showToast(\"Hello\");-->\n" +
            "\n" +
            "    <!--}-->\n" +
            "    <!--</script>-->\n" +
            "    <div id='calendar' ></div>\n" +
            "\n" +
            "</div>\n" +
            "</html>';\n";


}
