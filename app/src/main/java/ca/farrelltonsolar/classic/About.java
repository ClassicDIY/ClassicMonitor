package ca.farrelltonsolar.classic;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

/**
 * Created by Graham on 28/12/13.
 */
public class About extends ActionBarActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        WebView engine = (WebView) findViewById(R.id.webView);
        String locale = getResources().getConfiguration().locale.getLanguage();
        String aboutFile = "file:///android_asset/about.html";
        if (locale.compareTo("fr") == 0) {
            aboutFile = "file:///android_asset/about-fr.html";
        }
        else if (locale.compareTo("es") == 0) {
            aboutFile = "file:///android_asset/about-es.html";
        }
        else if (locale.compareTo("it") == 0) {
            aboutFile = "file:///android_asset/about-it.html";
        }
        engine.loadUrl(aboutFile);
        if(!screenIsLarge() )
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private boolean screenIsLarge()
    {
        int screenMask = getResources().getConfiguration().screenLayout;
        if ( ( screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return true;
        }

        if ( (screenMask & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return true;
        }

        return false;

    }
}