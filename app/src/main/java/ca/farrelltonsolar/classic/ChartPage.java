package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Graham on 09/03/14.
 */
public class ChartPage extends Fragment {

    short[] mData;
    WebView mWebView;
    private boolean isReceiverRegistered;
    private static Gson GSON = new Gson();
    private String preparedMinuteLogs;
    private String preparedLabels;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(getClass().getName(), hidden ? "onHiddenChanged true" : "onHiddenChanged false");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View theView = inflater.inflate(R.layout.webview, container, false);
        mWebView = (WebView) theView.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebViewInterface(), "MainActivityInterface");
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl(GetHtmlPage());
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
        if (this.isVisible()) {
            mWebView.reload();
        }
    }

    public class WebViewInterface {

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MonitorApplication.getAppContext(), message, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public String getMinuteLogs() {
            return preparedMinuteLogs;
        }

        @JavascriptInterface
        public String getLabels() {
            return preparedLabels;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterReceiver();
        Log.d(getClass().getName(), "onStop");
    }

    private void unRegisterReceiver() {
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
            Log.d(getClass().getName(), "mReadingsReceiver");
            try {
                Bundle logs = intent.getBundleExtra("logs");
                if (logs != null) {
                    mData = logs.getShortArray(String.valueOf(Constants.CLASSIC_POWER_HOURLY_CATEGORY));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            unRegisterReceiver();

            Log.d(getClass().getName(), "PageLoader");
            new PageLoader().execute();
            Log.d(getClass().getName(), "Chart received logs from classic");
        }
    };

    public void prepareMinuteLogs() {
        if (mData != null && mData.length > 0) {
            short[] reverseData = new short[mData.length];
            int j = 0;
            for (int i = mData.length - 1; i >= 0; i--) {
                reverseData[j++] = mData[i];
            }
            preparedMinuteLogs = GSON.toJson(reverseData);
        }
        return;
    }

    public void prepareLabels() {
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
        preparedLabels = gson.toJson(hours);
        return;
    }

    private class PageLoader extends AsyncTask<String, Void, String> {
        private PageLoader() {
        }

        @Override
        protected String doInBackground(String... params) {
            prepareMinuteLogs();
            prepareLabels();
            Log.d(getClass().getName(), "Chart prep done");
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(getClass().getName(), "Chart loadUrl");
            mWebView.reload();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
