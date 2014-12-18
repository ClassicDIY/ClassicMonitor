package ca.farrelltonsolar.classic;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Graham on 26/12/13.
 */
public class MonitorApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static Context context;

    static Map<Integer, String> _chargeStates = new HashMap<Integer, String>();
    static Map<Integer, String> _chargeStateTitles = new HashMap<Integer, String>();
    static ModbusService modbusService;
    static UDPListener UDPListenerService;
    static boolean ismodbusServiceBound = false;
    static boolean isUDPListenerServiceBound = false;
    private static ChargeControllers chargeControllers;
    private static Gson GSON = new Gson();
    ComplexPreferences configuration;

    private static LogSaver mLogSaver = new LogSaver();

    @Override
    public void onTerminate() {
        super.onTerminate();
        mLogSaver.Terminate();
    }

    public void onCreate() {
        super.onCreate();

        MonitorApplication.context = getApplicationContext();
        chargeControllers = new ChargeControllers(context);
        InitializeChargeStateLookup();
        InitializeChargeStateTitleLookup();
        mLogSaver.Start();
        //mLogSaver.ResetLogs();
        Log.d(getClass().getName(), "InitializeModbus complete");
        this.registerActivityLifecycleCallbacks(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(addChargeControllerReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER));
        configuration = ComplexPreferences.getComplexPreferences(this, null, Context.MODE_PRIVATE);
        chargeControllers = configuration.getObject("devices", ChargeControllers.class);
        if (chargeControllers == null) { // save empty collection
            chargeControllers = new ChargeControllers(context);
        }
    }

    public static Context getAppContext() {
        return MonitorApplication.context;
    }

    public static ChargeControllers chargeControllers() {
        return chargeControllers;
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

    private ServiceConnection modbusServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            ModbusService.ModbusServiceBinder binder = (ModbusService.ModbusServiceBinder) service;
            modbusService = binder.getService();
            ismodbusServiceBound = true;
            ChargeController cc = chargeControllers.getCurrentChargeController();
            if (cc != null) { // device selected before service was bound
                modbusService.Monitor(cc, false);
            }
            Log.d(getClass().getName(), cc != null ? String.format("ModbusService ServiceConnected, monitoring device %s", cc) : "ModbusService ServiceConnected");
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

    // Our handler for received Intents.
    private BroadcastReceiver addChargeControllerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChargeController cc = GSON.fromJson(intent.getStringExtra("ChargeController"), ChargeController.class);
            Log.d(getClass().getName(), String.format("adding new controller to list (%s)", cc.toString()));
            chargeControllers.add(cc);
            UDPListenerService.stopListening();
            UDPListenerService.listen(chargeControllers);
        }
    };

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(getClass().getName(), "onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(getClass().getName(), "onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(getClass().getName(), "onActivityResumed");
        bindService(new Intent(this, ModbusService.class), modbusServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, UDPListener.class), UDPListenerServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(getClass().getName(), "onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(getClass().getName(), "onActivityStopped");
        if (ismodbusServiceBound) {
            modbusService.disconnect(false);
            unbindService(modbusServiceConnection);
            Log.d(getClass().getName(), "unbindService modbusServiceConnection");
        }
        if (isUDPListenerServiceBound) {
            UDPListenerService.stopListening();
            unbindService(UDPListenerServiceConnection);
            Log.d(getClass().getName(), "unbindService UDPListenerServiceConnection");
        }
    }

    private class ModbusDisconnector extends Thread {
        @Override
        public void run() {
            modbusService.disconnect(false);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(getClass().getName(), "onActivitySaveInstanceState");
        configuration.putObject("devices", chargeControllers);
        configuration.commit();
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(getClass().getName(), "onActivityDestroyed");
    }

    public static void clearChargeControllerList(boolean all) {
        modbusService.disconnect(true);
        if (all) {
            chargeControllers.clear();
        } else {
            chargeControllers.clearDynamic();
        }
        UDPListenerService.listen(chargeControllers);
    }

    public static void monitor(int device) {
        if (device < 0 || device >= chargeControllers.count()) {
            return;
        }
        if (chargeControllers.setCurrent(device)) {
            if (ismodbusServiceBound) {
                modbusService.Monitor(chargeControllers.getCurrentChargeController(), true);
            }
        }
    }
}