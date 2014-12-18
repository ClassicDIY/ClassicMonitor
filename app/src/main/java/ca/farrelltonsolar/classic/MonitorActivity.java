package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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

public class MonitorActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, IPAddressDialog.OnIPAddressDialogInteractionListener {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    private static Gson GSON = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUnitReceiver, new IntentFilter("ca.farrelltonsolar.classic.UnitName"));
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, layout);
        setupActionBar();
        Log.d(getClass().getName(), "onCreate");
    }

    private void setupActionBar() {
        SlidingTabLayout stl = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        stl.setDividerColors(Color.RED);
        stl.setSelectedIndicatorColors(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW);
        tabStripAdapter = new TabStripAdapter(getSupportFragmentManager(), this, (ViewPager) findViewById(R.id.pager), stl);
        tabStripAdapter.addTab(PowerFragment.TabTitle, PowerFragment.class, null);
        tabStripAdapter.addTab(EnergyFragment.TabTitle, EnergyFragment.class, null);
        tabStripAdapter.addTab(StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
        tabStripAdapter.addTab(TemperatureFragment.TabTitle, TemperatureFragment.class, null);
        tabStripAdapter.addTab(R.string.CalendarTabTitle, CalendarPage.class, null);
        tabStripAdapter.addTab(R.string.ChartTabTitle, ChartPage.class, null);
        tabStripAdapter.notifyTabsChanged();
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
    public void onNavigationDrawerItemSelected(int device) {
        MonitorApplication.monitor(device);
    }


    @Override
    public void onAddChargeController(ChargeController cc) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Intent pkg = new Intent("ca.farrelltonsolar.classic.AddChargeController");
        pkg.putExtra("ChargeController", GSON.toJson(cc));
        broadcaster.sendBroadcast(pkg);
    }


}
