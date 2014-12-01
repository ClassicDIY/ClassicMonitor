package ca.farrelltonsolar.classic;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Graham on 26/12/13.
 */
public class MyApplication extends Application {
    private static Context context;
    public static String mUnitName = "";

    static Map<Integer, String> _chargeStates = new HashMap<Integer, String>();
    static Map<Integer, String> _chargeStateTitles = new HashMap<Integer, String>();

    private static LogSaver mLogSaver = new LogSaver();

    @Override
    public void onTerminate() {
        super.onTerminate();
        mLogSaver.Terminate();
    }

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
        InitializeChargeStateLookup();
        InitializeChargeStateTitleLookup();
        Log.d(Constants.LOG_TAG, "InitializeModbus");
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusSetup", null, context, ModbusMaster.class);
        this.startService(modbusInitIntent);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mUnitReceiver, new IntentFilter("ca.farrelltonsolar.classic.Unit"));
        mLogSaver.Start();
        //mLogSaver.ResetLogs();
        Log.d(Constants.LOG_TAG, "InitializeModbus complete");
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static String getChargeStateText(int cs) {
        if (_chargeStates.containsKey(cs)) {
            return _chargeStates.get(cs);
        }
        return "";
    }

    public static String getChargeStateTitleText(int cs) {
        if (_chargeStateTitles.containsKey(cs)) {
            return _chargeStateTitles.get(cs);
        }
        return "";
    }

    private void InitializeChargeStateLookup() {
        _chargeStates.put(-1, getString(R.string.NoConnection));
        _chargeStates.put(0, getString(R.string.ChargeStateOff));
        _chargeStates.put(3, getString(R.string.Absorb));
        _chargeStates.put(4, getString(R.string.Bulk));
        _chargeStates.put(5, getString(R.string.Float));
        _chargeStates.put(6, getString(R.string.Tracking));
        _chargeStates.put(7, getString(R.string.Equalize));
        _chargeStates.put(10, getString(R.string.Error));
        _chargeStates.put(18, getString(R.string.SeekingEqualize));
    }
    private void InitializeChargeStateTitleLookup() {
        _chargeStateTitles.put(-1, "");
        _chargeStateTitles.put(0, getString(R.string.ChargeStateOffTitle));
        _chargeStateTitles.put(3, getString(R.string.AbsorbTitle));
        _chargeStateTitles.put(4, getString(R.string.BulkTitle));
        _chargeStateTitles.put(5, getString(R.string.FloatTitle));
        _chargeStateTitles.put(6, getString(R.string.TrackingTitle));
        _chargeStateTitles.put(7, getString(R.string.EqualizeTitle));
        _chargeStateTitles.put(10, getString(R.string.ErrorTitle));
        _chargeStateTitles.put(18, getString(R.string.SeekingEqualizeTitle));
    }

    // Our handler for received Intents.
    private BroadcastReceiver mUnitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            mUnitName = intent.getStringExtra("UnitName");
        }
    };
}