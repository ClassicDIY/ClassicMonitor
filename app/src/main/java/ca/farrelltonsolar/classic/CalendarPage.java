package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Graham on 09/03/14.
 */
public class CalendarPage extends Fragment {


    short[] mData;
    short[] mFloatData;
    private String mPageData;
    WebView mWebView;
    private boolean isReceiverRegistered;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View theView = inflater.inflate(R.layout.webview, container, false);
        mWebView = (WebView) theView.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewInterface(), "MainActivityInterface");
        mWebView.setWebChromeClient(new WebChromeClient());
        String MonthNames = MonitorApplication.getAppContext().getString(R.string.MonthNames);
        String MonthNamesShort = MonitorApplication.getAppContext().getString(R.string.MonthNamesShort);
        String DayNames = MonitorApplication.getAppContext().getString(R.string.DayNames);
        String DayNamesShort = MonitorApplication.getAppContext().getString(R.string.DayNamesShort);
        mPageData = String.format(Constants.Calendar_html, GetCSS(), MonthNames, MonthNamesShort, DayNames, DayNamesShort);

        return theView;
    }

    private String GetCSS() {
        String rVal;
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            rVal = "fullcalendar-xlarge";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            rVal = "fullcalendar-large";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            rVal = "fullcalendar-normal";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            rVal = "fullcalendar-small";
        } else {
            rVal = "fullcalendar-normal";
        }
        return rVal;
    }

    private void Refresh() {
        if (this.isVisible()) {
            mWebView.loadDataWithBaseURL("file:///android_asset/", mPageData, "text/html", "utf-8", null);
        }
    }

    public class WebViewInterface {

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MonitorApplication.getAppContext(), message, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public String getChartData(String args) {
            String rval = "";
            Gson gson = new Gson();
            DateRange dr = gson.fromJson(args, DateRange.class);
            Date sd = FromUnixTime(dr.start);
            Date ed = FromUnixTime(dr.end);
            if (mData != null && mData.length > 0) {
                if (mFloatData != null && mFloatData.length > 0) {
                    EventObject[] res = LoadEventData(sd, ed);
                    rval = gson.toJson(res);
                }
            }
            return rval;
        }

        private EventObject[] LoadEventData(Date sd, Date ed) {
            int range = getDifferenceDays(sd, ed);
            Calendar calendar = Calendar.getInstance();
            int fromYesterday = getDifferenceDays(sd, calendar.getTime()) - 2;
            EventObject[] days = new EventObject[range];
            try {
                if (fromYesterday < mData.length) {
                    calendar.setTime(sd);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    for (int i = 0; i < range; i++) {
                        String s = sdf.format(calendar.getTime());
                        if (fromYesterday >= 0) {
                            String t = String.valueOf(mData[fromYesterday] / 10.0f) + " kWh";
                            if (mFloatData[fromYesterday] > 0) {
                                t += "\n " + MonitorApplication.getAppContext().getString(R.string.CalendarFloat);
                            }
                            days[i] = new EventObject(t, s);
                            fromYesterday--;
                        } else {
                            days[i] = new EventObject("", s);
                        }
                        calendar.add(Calendar.DATE, 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return days;
        }

        public Date FromUnixTime(long unixTime) {
            Date dateTime = new Date(unixTime * 1000);
            return dateTime;
        }

        public int getDifferenceDays(Date d1, Date d2) {
            int daysdiff = 0;
            long diff = d2.getTime() - d1.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000) + 1;
            daysdiff = (int) diffDays;
            return daysdiff;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver();
        Log.d(getClass().getName(), "onStop");
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //SetReadings(new Readings(intent.getBundleExtra("readings")));

            try {
                Bundle logs = intent.getBundleExtra("logs");
                if (logs != null) {
                    mData = logs.getShortArray(String.valueOf(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY));
                    mFloatData = logs.getShortArray(String.valueOf(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            unregisterReceiver();
            Log.d(getClass().getName(), "Calendar received logs from classic");
            Refresh();
        }
    };

    private class DateRange {
        public int start;
        public int end;
    }

    public class EventObject {
        public EventObject(String t, String s) {
            title = t;
            start = s;
        }

        public String title = "";
        public String start = "";
    }
}
