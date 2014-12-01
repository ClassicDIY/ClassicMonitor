package ca.farrelltonsolar.classic;

/**
 * Created by Graham on 01/03/14.
 */
public class Constants {
    // Used to write to the system log from this class.
    public static final String LOG_TAG = "FarrelltonSolar";

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = "ca.farrelltonsolar.classic.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = "ca.farrelltonsolar.classic.STATUS";

    // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = "ca.farrelltonsolar.classic.LOG";
    public static final int MODBUS_RETRIES = 3;
    public static final int MODBUS_POLL_TIME = 1000;

    public static final int CLASSIC_DAILY_LOG = 5;
    public static final int CLASSIC_MINUTE_LOG = 6;

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

    public static final String IP_ADDRESS_PREFERENCE = "IpAddress";
    public static final String LAST_IP_ADDRESS = "LastIpAddress";
    public static final String PORT_PREFERENCE = "PortSetting";
    public static final String SUBNET_SCAN_PREFERENCE = "SubnetScan";
    public static final String START_IP_PREFERENCE = "StartIpAddress";
    public static final String END_IP_COUNT_PREFERENCE = "EndIpAddressCount";
    public static final String BIDIRECTIONALUNIT_PREFERENCE = "BidirectionalUnitInWatts";
    public static final String LOCK_SCALE_PREFERENCE = "LockScale";
    public static final String BatteryVoltScale = "BatteryVoltScale";
    public static final String BatteryCurrentScale = "BatteryCurrentScale";
    public static final String PVVoltScale = "PVVoltScale";
    public static final String PVCurrentScale = "PVCurrentScale";
    public static final String PowerScale = "PowerScale";
    public static final String BiDirectionalPowerScale = "BiDirectionalPowerScale";
    public static final String UploadToPVOutput = "UploadToPVOutput";
    public static final String APIKey = "APIKey";
    public static final String SID = "SID";

    public static final int[] BatteryVoltScales = new int[] {160, 100, 80, 40, 20};
    public static final int[] BatteryCurrentScales = new int[] {250, 200, 150, 100, 80, 40, 20};
    public static final int[] PVVoltScales = new int[] {250, 200, 160, 100, 80, 40};
    public static final int[] PVCurrentScales = new int[] {80, 50, 40, 20, 10};
    public static final int[] PowerScales = new int[] {5000, 4000, 3000, 2000, 1000};
    public static final int[] BiDirectionalPowerScales = new int[] {8000, 5000, 3000, 2000, 1000, 500};

    public static final int PVOUTPUT_RATE_LIMIT = 10000; // every n milliseconds
    public static final int PVOUTPUT_RECORD_LIMIT = 20; // max uploads per session

    public static final int ELAPSED_LIMIT = 5;
    public static final int HTTP_CONNECT_TIMEOUT = 15000;
    public static final int HTTP_SO_TIMEOUT = 15000;
    public static final double DEFAULT_TEMPERATURE = -1000;
    public static final double DEFAULT_VOLTAGE = -1;
    public static final int MAX_BATCH_SIZE = 30;
    public static final int MAX_TOTAL_CONNECTION = 20;
    public static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    public static final int TIMEOUT_CONNECT = 15000;
    public static final int TIMEOUT_READ = 15000;

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
