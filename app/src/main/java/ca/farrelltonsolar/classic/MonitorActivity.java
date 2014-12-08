package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;


    ViewPager mViewPager;
    private TabStripAdapter mTabsAdapter;

    String mClassic = "";
    private int mPosition = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mUnitReceiver, new IntentFilter("ca.farrelltonsolar.classic.Unit"));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            // Set up the drawer.
            DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, layout);
            setupActionBar();
        } else {
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mToastReceiver, new IntentFilter("ca.farrelltonsolar.classic.Toast"));
        }
    }

    private void setupActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mTabsAdapter = new TabStripAdapter(getSupportFragmentManager(), this,
                (ViewPager) findViewById(R.id.pager),
                (SlidingTabLayout) findViewById(R.id.sliding_tabs));
        mTabsAdapter.addTab(R.string.GaugeTabTitle, GaugePage.class, null);
        mTabsAdapter.addTab(R.string.CalendarTabTitle, CalendarPage.class, null);
        mTabsAdapter.addTab(R.string.ChartTabTitle, ChartPage.class, null);
        mTabsAdapter.notifyTabsChanged();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//
//        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
//        mSlidingTabLayout.setViewPager(mViewPager);

        // Set up the ViewPager with the sections adapter.
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });

        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this)
//            );
//        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(MyApplication.mUnitName);
    }

    // Our handler for received Intents.
    private BroadcastReceiver mUnitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mClassic = intent.getStringExtra("UnitName");
            getSupportActionBar().setTitle(mClassic + mPosition);
        }
    };


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
        if (mNavigationDrawerFragment == null) {
            getMenuInflater().inflate(R.menu.gauge_activity_actions, menu);
            return true;
        }
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
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
        super.onPause();
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Control", ConnectionState.Paused.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Control", ConnectionState.Connected.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        mPosition = position;
        Toast.makeText(this.getBaseContext(), "Position: " + position, Toast.LENGTH_SHORT).show();
    }

}
