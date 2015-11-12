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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    private int currentChargeState = -1;
    private boolean isReceiverRegistered;
    private SlidingTabLayout stl;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, layout);
        stl = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        stl.setDividerColors(Color.RED);
        stl.setSelectedIndicatorColors(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW);
        viewPager = (ViewPager) findViewById(R.id.pager);
        setupActionBar();
        Log.d(getClass().getName(), "onCreate");
    }

    private void setupActionBar() {

        tabStripAdapter = new TabStripAdapter(getFragmentManager(), this, viewPager, stl, null);
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null && cc.deviceType() == DeviceType.Classic) {
            if (cc.hasWhizbang()) {
                if (MonitorApplication.chargeControllers().count() > 1) {
                    tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
                    tabStripAdapter.addTab(SystemFragment.TabTitle, SystemFragment.class, null);
                    tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
                    tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
                    tabStripAdapter.addTab(CapacityFragment.TabTitle, CapacityFragment.class, null);
                }
                else {
                    tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
                    tabStripAdapter.addTab(LoadFragment.TabTitle, LoadFragment.class, null);
                    tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
                    tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
                    tabStripAdapter.addTab(CapacityFragment.TabTitle, CapacityFragment.class, null);
                }
            }
            else {
                tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
                tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            }
            tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            addDayLogCalendar();
            tabStripAdapter.addTab(R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab(R.string.HourChartTabTitle, HourLogChart.class, null);
            tabStripAdapter.addTab(R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab(R.string.MessagesTabTitle, MessageFragment.class, null);
            tabStripAdapter.addTab(R.string.About, About.class, null);
        } else if (cc != null && cc.deviceType() == DeviceType.Kid) {
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            if (cc.hasWhizbang()) {
                tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            }
            tabStripAdapter.addTab(R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab(R.string.About, About.class, null);
        }
        else if (cc != null && cc.deviceType() == DeviceType.TriStar) {
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            tabStripAdapter.addTab(R.string.About, About.class, null);
        }
        else {
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            addDayLogCalendar();
            tabStripAdapter.addTab(R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab(R.string.HourChartTabTitle, HourLogChart.class, null);
            tabStripAdapter.addTab(R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab(R.string.MessagesTabTitle, MessageFragment.class, null);
            tabStripAdapter.addTab(R.string.About, About.class, null);
        }
        tabStripAdapter.notifyTabsChanged();
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem pvOutput = menu.findItem(R.id.action_pvOutput);
        if(pvOutput != null)
        {
            pvOutput.setVisible(MonitorApplication.chargeControllers().uploadToPVOutput());
        }
        return true;
    }

    private void addDayLogCalendar() {
        if (Build.VERSION.SDK_INT >= 17) {
            tabStripAdapter.addTab(R.string.DayLogTabTitle, MonthCalendarPager.class, null);
        }
        else {
            tabStripAdapter.addTab(R.string.DayLogTabTitle, DayLogCalendar.class, null);

        }
    }

    protected BroadcastReceiver mMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
            boolean differentController = intent.getBooleanExtra("DifferentController", false);
            if (differentController) {
                MonitorActivity.this.finish();
                System.gc();
                MonitorActivity.this.startActivity(getIntent());
            }
            }
            catch (Throwable ex) {
                Log.e(getClass().getName(), "mMonitorReceiver failed ");
            }
        }
    };

    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle readings = intent.getBundleExtra("readings");
                int chargeState = readings.getInt(RegisterName.ChargeState.name());
                if (currentChargeState != chargeState) {
                    currentChargeState = chargeState;
                    String state = MonitorApplication.getChargeStateTitleText(chargeState);
                    String currentUnitName = "";
                    ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
                    if (cc != null) {
                        currentUnitName = cc.deviceName();
                    }
                    if (state == null || state.isEmpty()) {
                        getSupportActionBar().setTitle(currentUnitName);
                    } else {
                        getSupportActionBar().setTitle(String.format("%s - (%s)", currentUnitName, MonitorApplication.getChargeStateTitleText(chargeState)));
                        if (MonitorApplication.chargeControllers().showPopupMessages()) {
                            Toast.makeText(context, MonitorApplication.getChargeStateText(chargeState), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            catch (Throwable ex) {
                Log.e(getClass().getName(), "mReadingsReceiver failed ");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (navigationDrawerFragment == null || !navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.shared_activity_menu, menu);
            currentChargeState = -1; // reload title
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean handled = false;
        switch (id) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                handled = true;
                break;
            case R.id.action_help:
                String helpContext = navigationDrawerFragment.isDrawerOpen() ? "NavigationBar" : tabStripAdapter.getItem(viewPager.getCurrentItem()).getClass().getSimpleName();
                helpContext = String.format("http://skyetracker.com/classicmonitor/%s/help.html#%s", Locale.getDefault().getLanguage(), helpContext);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpContext)));
                handled = true;
                break;
            case R.id.action_pvOutput:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pvoutput.org/")));
                handled = true;
                break;
        }
        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMonitorReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(this).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            LocalBroadcastManager.getInstance(this).registerReceiver(receiveAToast, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_TOAST));
            isReceiverRegistered = true;
        }
    }

    private BroadcastReceiver receiveAToast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MonitorApplication.chargeControllers().showPopupMessages()) {
                try {
                    String message = intent.getStringExtra("message");
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                } catch (Throwable ex) {
                    Log.e(getClass().getName(), "receiveAToast failed ");
                }
            }
        }
    };



    @Override
    protected void onPause() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMonitorReceiver);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mReadingsReceiver);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveAToast);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        super.onPause();
    }

}
