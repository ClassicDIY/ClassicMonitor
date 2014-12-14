package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    ModbusService modbusService;
    UDPListener UDPListenerService;
    boolean ismodbusServiceBound = false;
    boolean isUDPListenerServiceBound = false;
    ChargeController currentChargeController;
    private static Gson GSON = new Gson();
    ComplexPreferences configuration;
    private ChargeControllers chargeControllers;

    @Override
    protected void onDestroy() {
        if (ismodbusServiceBound) {
            modbusService.disconnect();
            unbindService(modbusServiceConnection);
            Log.d(getClass().getName(), "unbindService modbusServiceConnection");
        }
        if (isUDPListenerServiceBound) {
            UDPListenerService.stopListening();
            unbindService(UDPListenerServiceConnection);
            Log.d(getClass().getName(), "unbindService UDPListenerServiceConnection");
        }
        Log.d(getClass().getName(), "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUnitReceiver, new IntentFilter("ca.farrelltonsolar.classic.UnitName"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCCReceiver, new IntentFilter("ca.farrelltonsolar.classic.AddChargeController"));
        configuration = ComplexPreferences.getComplexPreferences(this, null, Context.MODE_PRIVATE);
        chargeControllers = configuration.getObject("devices", ChargeControllers.class);
        if (chargeControllers == null) { // save empty collection
            chargeControllers = new ChargeControllers();
            configuration.putObject("devices", chargeControllers);
            configuration.commit();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            // Set up the drawer.
            DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
            navigationDrawerFragment.setUp(R.id.navigation_drawer, layout, chargeControllers);
            setupActionBar();
        } else {
            LocalBroadcastManager.getInstance(this).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mToastReceiver, new IntentFilter("ca.farrelltonsolar.classic.Toast"));
        }
        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("currentChargeController");
            if (json != null && json.isEmpty() == false) {
                currentChargeController = GSON.fromJson(json, ChargeController.class);
            }
        }
        bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(getClass().getName(), "onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(getClass().getName(), "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString("currentChargeController", GSON.toJson(currentChargeController));
    }

    private ServiceConnection modbusServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();
            ismodbusServiceBound = true;
            Log.d(getClass().getName(), "ModbusService ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            ismodbusServiceBound = false;
            modbusService = null;
            Log.d(getClass().getName(), "ModbusService ServiceDisconnected");
        }
    };

    private ServiceConnection UDPListenerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            UDPListener.UDPListenerServiceBinder binder = (UDPListener.UDPListenerServiceBinder) service;
            UDPListenerService = binder.getService();
            isUDPListenerServiceBound = true;
            UDPListenerService.listen(chargeControllers);
            Log.d(getClass().getName(), "UDPListener ServiceConnected");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isUDPListenerServiceBound = false;
            UDPListenerService = null;
            Log.d(getClass().getName(), "UDPListener ServiceDisconnected");
        }
    };

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        tabStripAdapter = new TabStripAdapter(getSupportFragmentManager(), this,
                (ViewPager) findViewById(R.id.pager),
                (SlidingTabLayout) findViewById(R.id.sliding_tabs));
        tabStripAdapter.addTab(R.string.GaugeTabTitle, GaugePage.class, null);
        tabStripAdapter.addTab(R.string.CalendarTabTitle, CalendarPage.class, null);
        tabStripAdapter.addTab(R.string.ChartTabTitle, ChartPage.class, null);
        tabStripAdapter.notifyTabsChanged();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    // Our handler for received Intents.
    private BroadcastReceiver mUnitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSupportActionBar().setTitle(intent.getStringExtra("UnitName"));
        }
    };


    // Our handler for received Intents.
    private BroadcastReceiver mToastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        }
    };

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SetReadings(new Readings(intent.getBundleExtra("readings")));
        }
    };

    private void SetReadings(Readings readings) {
        try {
            ListPage view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.Power);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.Power));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.PVVoltage);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.PVVoltage));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.PVCurrent);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.PVCurrent));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.BatteryVolts);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.BatVoltage));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.BatteryCurrent);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.BatCurrent));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.TotalEnergy);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.TotalEnergy));
            }
            view = (ListPage) getSupportFragmentManager().findFragmentById(R.id.EnergyToday);
            if (view != null) {
                view.setValue(readings.GetFloat(RegisterName.EnergyToday));
            }
            int cs = readings.GetInt(RegisterName.ChargeState);
            TextView tview = (TextView) findViewById(R.id.ChargeStateTitle);
            if (tview != null) {

                tview.setText(MyApplication.getChargeStateTitleText(cs));
            }
            tview = (TextView) findViewById(R.id.ChargeState);
            if (tview != null) {
                tview.setText(MyApplication.getChargeStateText(cs));
            }
        } catch (Exception ignore) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (navigationDrawerFragment == null) {
            getMenuInflater().inflate(R.menu.gauge_activity_actions, menu);
            return true;
        }
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.gauge_activity_actions, menu);
            restoreActionBar();
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
    protected void onPause() {
        Log.d(getClass().getName(), "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(getClass().getName(), "onResume");
        super.onResume();
    }

    @Override
    public void onNavigationDrawerItemSelected(ChargeController device) {

        if (ismodbusServiceBound) {
            currentChargeController = device;
            modbusService.Monitor(device);
        } else {
            Toast.makeText(this, "Not bound to service", Toast.LENGTH_SHORT).show();
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mCCReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeController cc = GSON.fromJson(intent.getStringExtra("ChargeController"), ChargeController.class);
            navigationDrawerFragment.AddChargeController(cc);
            chargeControllers.getControllers().add(cc);
            configuration.putObject("devices", chargeControllers);
            configuration.commit();
        }
    };

    public void clearChargeControllerList() {
        modbusService.disconnect();
        chargeControllers.getControllers().clear();
        UDPListenerService.listen(chargeControllers);
    }
}
