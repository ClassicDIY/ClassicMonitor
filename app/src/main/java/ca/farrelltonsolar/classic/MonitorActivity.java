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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    private String currentUnitName = "";
    private int currentChargeState = -1;
    private boolean isReceiverRegistered;
    private SlidingTabLayout stl;
    private ViewPager viewPager;
    static boolean ismodbusServiceBound = false;
    ModbusService modbusService;

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
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

        tabStripAdapter = new TabStripAdapter(getSupportFragmentManager(), this, viewPager, stl, null);
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null && cc.deviceType() == DeviceType.Classic) {
            currentUnitName = cc.deviceName();
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            if (cc.hasWhizbang()) {
                tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            }
            tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            tabStripAdapter.addTab(R.string.DayLogTabTitle, DayLogCalendar.class, null);
            tabStripAdapter.addTab(R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab(R.string.HourChartTabTitle, HourLogChart.class, null);
            tabStripAdapter.addTab(R.string.InfoTabTitle, InfoFragment.class, null);
        } else if (cc != null && cc.deviceType() == DeviceType.TriStar) {
            currentUnitName = cc.deviceName();
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
        } else {
            currentUnitName = "Demo";
            tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
            tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            tabStripAdapter.addTab(R.string.DayLogTabTitle, DayLogCalendar.class, null);
            tabStripAdapter.addTab(R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab(R.string.HourChartTabTitle, HourLogChart.class, null);
            tabStripAdapter.addTab(R.string.InfoTabTitle, InfoFragment.class, null);
        }
        tabStripAdapter.notifyTabsChanged();
    }

    protected BroadcastReceiver mMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean differentController = intent.getBooleanExtra("DifferentController", false);
            if (differentController) {
                MonitorActivity.this.finish();
                MonitorActivity.this.startActivity(getIntent());
            }
        }
    };

    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle readings = intent.getBundleExtra("readings");
            int chargeState = readings.getInt(RegisterName.ChargeState.name());
            if (currentChargeState != chargeState) {
                currentChargeState = chargeState;
                String state = MonitorApplication.getChargeStateTitleText(chargeState);
                if (state == null || state.isEmpty()) {
                    getSupportActionBar().setTitle(currentUnitName);
                } else {
                    getSupportActionBar().setTitle(String.format("%s - (%s)", currentUnitName, MonitorApplication.getChargeStateTitleText(chargeState)));
                    Toast.makeText(context, MonitorApplication.getChargeStateText(chargeState), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (navigationDrawerFragment == null || !navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.shared_activity_menu, menu);
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
            case R.id.action_about:
                startActivityForResult(new Intent(this, About.class), 0);
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
            LocalBroadcastManager.getInstance(this).registerReceiver(updateChargeControllersReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS));
            isReceiverRegistered = true;
        }
    }

    private BroadcastReceiver updateChargeControllersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController(); // cc got removed?
            if (cc == null) {
                if (modbusService != null && modbusService.isInService()) {
                    modbusService.stopMonitoringChargeController();
                    MonitorActivity.this.finish();
                    MonitorActivity.this.startActivity(getIntent());
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
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        Log.d(getClass().getName(), String.format("onStart thread is %s", Thread.currentThread().getName()));
        super.onStart();
        bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(getClass().getName(), String.format("onStop thread is %s", Thread.currentThread().getName()));
        unbindService(modbusServiceConnection);
        super.onStop();
    }

    private ServiceConnection modbusServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(getClass().getName(), "ModbusService ServiceConnected");
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();

            modbusService.monitorChargeController(MonitorApplication.chargeControllers().getCurrentChargeController());
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(getClass().getName(), "ModbusService ServiceDisconnected");
            modbusService = null;
        }
    };
}
