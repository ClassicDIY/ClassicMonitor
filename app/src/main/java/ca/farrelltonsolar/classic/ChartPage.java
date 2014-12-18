package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Graham on 09/03/14.
 */
public class ChartPage extends Fragment {

    short[] mData;
    private String URL;
    WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View theView = inflater.inflate(R.layout.webview, container, false);
        mWebView = (WebView) theView.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewInterface(), "MainActivityInterface");
        mWebView.setWebChromeClient(new WebChromeClient());
        URL = GetHtmlPage();
        mWebView.loadUrl(URL);
        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS));
        return theView;
    }

    private String GetHtmlPage() {
        String rVal;
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            rVal = "file:///android_asset/chart-xlarge.html";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            rVal = "file:///android_asset/chart-large.html";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            rVal = "file:///android_asset/chart-normal.html";
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            rVal = "file:///android_asset/chart-small.html";
        } else {
            rVal = "file:///android_asset/chart-normal.html";
        }
        return rVal;

    }

    private void Refresh() {
        WebView webView = (WebView) getView().findViewById(R.id.webView);
        webView.loadUrl(URL);
    }

    public class WebViewInterface {

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MonitorApplication.getAppContext(), message, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public String getMinuteLogs() {
            String rVal = "";
            if (mData != null && mData.length > 0) {
                Gson gson = new Gson();
                short[] reverseData = new short[mData.length];
                int j = 0;
                for (int i = mData.length - 1; i >= 0; i--) {
                    reverseData[j++] = mData[i];
                }
                rVal = gson.toJson(reverseData);
            }
            return rVal;
        }

        @JavascriptInterface
        public String getLabels() {
            String rVal = "";
            Calendar cal = new GregorianCalendar();
            cal.roll(Calendar.HOUR_OF_DAY, true);
            int hour;
            int[] hours = new int[24];
            for (int i = 0; i < 24; i++) {
                hour = cal.get(Calendar.HOUR_OF_DAY);
                hours[i] = hour;
                cal.roll(Calendar.HOUR_OF_DAY, true);
            }
            Gson gson = new Gson();
            rVal = gson.toJson(hours);
            return rVal;
        }

//        private EventObject[] LoadEventData(Date sd, Date ed) {
//            int range = getDifferenceDays(sd, ed);
//            Calendar calendar = Calendar.getInstance();
//            int fromYesterday = getDifferenceDays(sd, calendar.getTime()) - 2;
//            EventObject[] days = new EventObject[range];
//            try {
//                if (fromYesterday < mData.length) {
//                    calendar.setTime(sd);
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    for (int i = 0; i < range; i++) {
//                        String s = sdf.format(calendar.getTime());
//                        if (fromYesterday >= 0) {
//                            String t = String.valueOf(mData[fromYesterday] / 10.0f) + " kWh";
//                            if (mFloatData[fromYesterday] > 0) {
//                                t += "\n " + MyApplication.getAppContext().getString(R.string.CalendarFloat);
//                            }
//                            days[i] = new EventObject(t, s);
//                            fromYesterday--;
//                        }
//                        else {
//                            days[i] = new EventObject("", s);
//                        }
//                        calendar.add(Calendar.DATE, 1);
//                    }
//                }
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//            return days;
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            RequestChartData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.getUserVisibleHint()) {
            RequestChartData();
        }
    }

    private void RequestChartData() {
//        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
//        modbusInitIntent.putExtra("Page", Function.MinuteLogs.ordinal());
//        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    // Our handler for received Intents.
    private BroadcastReceiver mToastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        }
    };

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //SetReadings(new Readings(intent.getBundleExtra("readings")));

            try {
                Bundle logs = intent.getBundleExtra("logs");
                if (logs != null) {
                    mData = logs.getShortArray(String.valueOf(Constants.CLASSIC_POWER_HOURLY_CATEGORY));
                }
                Refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
