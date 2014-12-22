package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, IPAddressDialog.OnIPAddressDialogInteractionListener, ViewPager.OnPageChangeListener {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    private String currentUnitName = "";
    private int currentChargeState = -1;
    private static Gson GSON = new Gson();
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, layout);
        setupActionBar();
        registerPreferencesChangeListener();
        Log.d(getClass().getName(), "onCreate");
    }

    private void setupActionBar() {
        SlidingTabLayout stl = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        stl.setDividerColors(Color.RED);
        stl.setSelectedIndicatorColors(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW);
        tabStripAdapter = new TabStripAdapter(getSupportFragmentManager(), this, (ViewPager) findViewById(R.id.pager), stl, this);
        tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
        tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
        tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
        tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
        tabStripAdapter.addTab(R.string.CalendarTabTitle, CalendarPage.class, null);
        tabStripAdapter.addTab(R.string.DayLogTabTitle, DayLogCalendar.class, null);
        tabStripAdapter.addTab(R.string.ChartTabTitle, HourLogChart.class, null);
        tabStripAdapter.notifyTabsChanged();
    }
    private void registerPreferencesChangeListener(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener =new SharedPreferences.OnSharedPreferenceChangeListener(){
            public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key){
//                if (key.equals(Constants.PREF_KEY_GUI_NICKNAME)) {
//                    PeerManager.instance().clear();
//                }
//                else       if (key.equals(Constants.PREF_KEY_NETWORK_USE_MULTICAST) || key.equals(Constants.PREF_KEY_NETWORK_USE_BROADCAST)) {
//                    resetLocalNetworkProcessors();
//                }
                Log.d(getClass().getName(), "OnSharedPreferenceChangeListener " + key);
            }
        };
        settings.registerOnSharedPreferenceChangeListener(listener);
    }


    // Our handler for received Intents.
    private BroadcastReceiver mUnitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentUnitName = intent.getStringExtra("UnitName");
        }
    };

    protected BroadcastReceiver mMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeController cc = (ChargeController) intent.getSerializableExtra("Controller");
            if (cc.hasWhizbang()) {
                tabStripAdapter.insertTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null, 2);
                tabStripAdapter.notifyTabsChanged();
            } else {

                tabStripAdapter.removeTab(StateOfChargeFragment.class);
                tabStripAdapter.notifyTabsChanged();
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
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mUnitReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_UNIT_NAME));
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mMonitorReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mUnitReceiver);
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mMonitorReceiver);
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        Log.d(getClass().getName(), "onStop");
    }

    @Override
    public void onNavigationDrawerItemSelected(int device) {
        MonitorApplication.monitor(device);
    }


    @Override
    public void onAddChargeController(ChargeController cc) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER);
        pkg.putExtra("ChargeController", GSON.toJson(cc));
        broadcaster.sendBroadcast(pkg);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
