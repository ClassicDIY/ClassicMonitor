package ca.farrelltonsolar.classic;

/**
 * Created by Graham on 01/03/14.
 */
public class Constants {

    // UDP listening port for available classics on the subnet
    public static final int CLASSIC_UDP_PORT = 4626;
    public static final int MODBUS_RETRIES = 2;
    public static final int MODBUS_POLL_TIME = 2000;

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

    public static final String PORT_PREFERENCE = "PortSetting";

    public static final String BIDIRECTIONALUNIT_PREFERENCE = "BidirectionalUnitInWatts";

    public static final String UploadToPVOutput = "UploadToPVOutput";
    public static final String APIKey = "APIKey";
    public static final String SID = "SID";

    public static final int PVOUTPUT_RATE_LIMIT = 10000; // every n milliseconds
    public static final int PVOUTPUT_RECORD_LIMIT = 20; // max uploads per session

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
