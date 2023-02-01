
<h1>Classic Monitor</h1>
<h2>An Android App for the Midnite Solar Charge Controller</h2>

[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FClassicDIY%2FClassic&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

<a href="https://www.buymeacoffee.com/r4K2HIB" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

<p>
<a href='https://play.google.com/store/apps/details?id=ca.classicdiy.classic&hl=en&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=200/></a>
 
[![GitHub tag (latest SemVer pre-release)](https://img.shields.io/github/v/tag/ClassicDIY/Classic?include_prereleases&label=Pre-Release&style=for-the-badge)](https://github.com/ClassicDIY/Classic/releases)
[![GitHub issues](https://img.shields.io/github/issues/ClassicDIY/Classic?style=for-the-badge)](https://github.com/ClassicDIY/Classic/issues)
[![http://graham22.github.io/Classic/classicmonitor/help_en.html](https://img.shields.io/badge/Documentation-Help-blue?style=for-the-badge)](http://graham22.github.io/Classic/classicmonitor/help_en.html)
</p>

<p>
Please refer to the <a href="https://github.com/ClassicDIY/Classic/wiki">Classic wiki</a> and the <a href="https://classicdiy.github.io/Classic/classicmonitor/help_en.html"> online documentation</a> for more information.
</p>

![Alt text](http://ClassicDIY.github.io/Classic/classicmonitor/images_en/StateOfCharge_landscape.png)

<p>
Classic Monitor is a free status monitor for Midnite solar 's, Classic 150, 200, 250 Charge Controller (www.midniteSolar.com). It is a Read Only Program, it does not write to the Classic.
The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or implied.
Classic Monitor is NOT a product of Midnite solar, nor do they support this application!
</p>
<p>
If the app detects a Whizbang Junior current monitor, the State Of Charge will display along with a bi-directional current gauge.
Basic support for the Tristar MPPT charge controller from Morningstar is also included.

</p>

## License
```

 Copyright (c) 2020. ClassicDIY

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

```


Release notes:

-----------------

version 2.6.5

<ul>
<li>Update to API 29</li>
<li>Fixed MAC address display, splash delay</li>
</ul>

-----------------

version 2.6.4

<ul>
<li>Enabled multicast lock to fix AutoDetect classic on newer versions of Android</li>
</ul>

-----------------

version 2.6.0

<ul>
<li>Clear instance state to get around bug with tabs displaying wrong content</li>
</ul>

-----------------

version 2.5.5

<ul>
<li>Update to API 28</li>
<li>Added charge % to live chart</li>
<li>Added Reason for Resting to Messages</li>
</ul>

-----------------

version 2.5.1

<ul>
<li>Updated German text</li>
</ul>

-----------------

version 2.5.0

Added the following information to the message tab;
<ul>
<li>Target Battery Voltage</li>
<li>Ending Amps</li>
<li>Float time</li>
<li>Equalize time</li>
</ul>

-----------------

version 2.4.0
<ul>
<li>Added Live Chart Tab.</li>
<li>Added Share Screen Capture.</li>
<li>Added option to disable 'System View' to support multiple independent charge controllers on separate battery banks.</li>
<li>Added support for multiple PVOutput Systems when System View is disabled.</li>
</ul>

-----------------


version 2.3.0
<ul>
<li>Added Online Help.</li>
<li>Added link to PVOutput.</li>
</ul>

-----------------

version 2.2.0
<ul>
<li>Added support for multiple classics, the new 'System' tab will display the power harvested and consumed by the system when multiple classics are in use. The WhizbangJr is required to enable this feature.</li>
<li>The Energy and Calendar views can now display system/controller values with the selection of a radio button.</li>
</ul>
-----------------

version 2.1.0

<ul>
<li>Added support for URI for remote access</li>
<li>Added capacity page to display more WhizbangJr information</li>
<li>Added German localization resources.</li>
</ul>

-----------------

version 2.0.5

<ul>
<li>Added Fahrenheit scale option</li>
</ul>

-----------------

version 2.0

<ul>
<li>Transfer to code to Github</li>
<li>Added support for multiple classics listed in a slide out navigation drawer.</li>
<li>Re-design of classic detection using UDP broadcasts.</li>
<li>Added temperature gauges.</li>
<li>Implemented calendar, day & hour chart using native code rather than using webview component.</li>
<li>Added info and message tabs.</li>
<li>Implemented new Android sliding tab view pager.</li>
<li>Implemented vertical pager for calendar view.</li>
<li>Re-design gauge component to support latest Android 5.0 API 21.</li>
<li>Added auto scale feature to gauges.</li>
<li>Updated PVOutput uploader to support multiple site IDs.</li>
<li>Implemented Android Services for Modbus, UPDListener and PVOutput uploader.</li>
<li>Updated French, Italian, Spanish localization resources.</li>
</ul>
-----------------

version 1.7

Prepare for deployment to google play


-----------------

version 1.6

Added french localization resources

-----------------

version 1.5

Added chart view.
Implemented upload to PVOutput.org.

-----------------

version 1.4

Added data feed to calendar view.
Close TCP socket when app is minimized, re-open when app is resumed.

-----------------

version 1.3

Refactores code to use BroadcastIntents.
Modbus Master now running as an Android IntentService.
Added Custom modbus read for File transfer.
Gauge scaled by touching the gauge when unlocked.
Added SOC% on main page
Added 'Countertop' view for SOC%, activated by touching SOC% on main page
Added Webview for PVOUtputs and Calendar (data feeds not complete)
Added Aux LEDs to power gauge

-----------------

version 1.2

Added basic support for WhizBangJr, Battery Gauge is bi-directional if whizbangJr is detected.
Added placeholder for WhizbangJr AH data

-----------------

version 1.1

Added tabbed views.
Added Placeholder for Calendar and Chart pages
Fixed J2Mod IsConnected, no longer sending extra blank byte over TCP.

-----------------

version 0.2

Fixed crash at first startup.
Default scan range when no gateway detected.
catch J2MOD Exception and reconnect.
Added support for API level 10+
Added Gauge scale settings 
Added IP Scan range
Now uses J2ModLite.
