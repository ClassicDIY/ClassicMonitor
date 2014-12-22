package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import org.joda.time.DateTime;

/**
 * Created by Graham on 21/12/2014.
 */
public class DayLogCalendar extends Fragment {
    private DateTime currentMonth;
    private DateTime month;
    private CalendarAdapter adapter;
    private View theView;
    private boolean isReceiverRegistered;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        theView = inflater.inflate(R.layout.day_log_calendar, container, false);
        month = DateTime.now().withTimeAtStartOfDay().withDayOfMonth(1);
        currentMonth = month;
        adapter = new CalendarAdapter(this.getActivity(), month);
        GridView gridview = (GridView) theView.findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        TextView title  = (TextView) theView.findViewById(R.id.title);
        title.setText(month.toString("MMMM yyyy"));
        TextView previous  = (TextView) theView.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                month = month.minusMonths(1);
                refreshCalendar();
            }
        });

        TextView next  = (TextView) theView.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (month.compareTo(currentMonth) < 0 ) {
                    month = month.plusMonths(1);
                    refreshCalendar();
                }

            }
        });
        return theView;
    }

    public void refreshCalendar()
    {
        TextView title  = (TextView) theView.findViewById(R.id.title);

        adapter.refreshDays(month);
        adapter.notifyDataSetChanged();
        title.setText(month.toString("MMMM yyyy"));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterReceiver();
        Log.d(getClass().getName(), "onStop");
    }
    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                LogEntry logs = (LogEntry)intent.getSerializableExtra("logs");
                if (logs != null) {
                    unRegisterReceiver();
                    adapter.setPowerSeries(logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY));
                    adapter.setFloatSeries(logs.getFloatArray(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY));
                    adapter.setHighPowerSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY));
                    adapter.setHighTempSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY));
                    adapter.setHighPVVoltSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY));
                    adapter.setHighBatVoltSeries(logs.getFloatArray(Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY));
                    adapter.notifyDataSetChanged();
                    Log.d(getClass().getName(), String.format("Day calendar received logs from classic %s", Thread.currentThread().getName()));
                }
            } catch (Exception e) {
                Log.w(getClass().getName(), String.format("Day calendar failed to load logs %s ex: %s", Thread.currentThread().getName(), e));
            }

        }
    };
}
