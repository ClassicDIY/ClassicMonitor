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