package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    GaugePage mGaugePage;
    CalendarPage mCalendar;
    ChartPage mChart;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGaugePage = new GaugePage();
            mCalendar = new CalendarPage();
            mChart = new ChartPage();

            // Set up the action bar.
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            // When swiping between different sections, select the corresponding
            // tab. We can also use ActionBar.Tab#select() to do this if we have
            // a reference to the Tab.
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });

            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(mSectionsPagerAdapter.getPageTitle(i))
                                .setTabListener(this)
                );
            }
        } else {
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mToastReceiver, new IntentFilter("ca.farrelltonsolar.classic.Toast"));
        }
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mUnitReceiver, new IntentFilter("ca.farrelltonsolar.classic.Unit"));

    }
    // Our handler for received Intents.
    private BroadcastReceiver mUnitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String unitName = intent.getStringExtra("UnitName");
            setTitle(unitName);
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
        }
        catch (Exception ignore) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
        setTitle(MyApplication.mUnitName);
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Control", ConnectionState.Connected.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment rVal = null;
            switch (position) {
                case 0:
                    rVal = mGaugePage;
                    break;
                case 1:
                    rVal = mCalendar;
                    break;
                case 2:
                    rVal = mChart;
                    break;
            }
            return rVal;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.GaugeTabTitle).toUpperCase(l);
                case 1:
                    return getString(R.string.CalendarTabTitle).toUpperCase(l);
                case 2:
                    return getString(R.string.ChartTabTitle).toUpperCase(l);
            }
            return null;
        }
    }
}
