package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import ca.farrelltonsolar.uicomponents.SOCGauge;
import ca.farrelltonsolar.uicomponents.SolarGauge;

/**
 * Created by Graham on 02/03/14.
 */
public class BigSOC extends ActionBarActivity {

    boolean _bidirectionalUnitsInWatts = false;
    boolean _lockScale = false;
    int _biDirectionalScaleIndex;
    int _batteryCurrentScaleIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bigsoc);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));
        SolarGauge gaugeView = (SolarGauge) findViewById(R.id.BatCurrent);
        LoadSettings();
        if (_bidirectionalUnitsInWatts) {
            gaugeView.setScaleEnd(Constants.BiDirectionalPowerScales[_biDirectionalScaleIndex]);
            gaugeView.setTitle(MyApplication.getAppContext().getString(R.string.BatPowerTitle));
            gaugeView.setUnit("W");
        } else {
            gaugeView.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
            gaugeView.setTitle(MyApplication.getAppContext().getString(R.string.BatCurrentTitle));
            gaugeView.setUnit("A");
        }
        if (_lockScale == false) {
            gaugeView.setOnClickListener(GetClickListener());
        }
        gaugeView.setBiDirectional(true);

    }

    private void LoadSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        _bidirectionalUnitsInWatts = settings.getBoolean(Constants.BIDIRECTIONALUNIT_PREFERENCE, false);
        _lockScale = settings.getBoolean(Constants.LOCK_SCALE_PREFERENCE, false);
        _biDirectionalScaleIndex = settings.getInt(Constants.BiDirectionalPowerScale, 0);
        if (_biDirectionalScaleIndex >= Constants.BiDirectionalPowerScales.length) {
            _biDirectionalScaleIndex = 0;
        }
        _batteryCurrentScaleIndex = settings.getInt(Constants.BatteryCurrentScale, 0);
        if (_batteryCurrentScaleIndex >= Constants.BatteryCurrentScales.length) {
            _batteryCurrentScaleIndex = 0;
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            SetReadings(new Readings(intent.getBundleExtra("readings")));
        }
    };

    private void SetReadings(Readings readings) {
        SolarGauge gaugeView = (SolarGauge) findViewById(R.id.BatCurrent);
        float batteryCurrent = readings.GetFloat(RegisterName.BatCurrent);
        if (_bidirectionalUnitsInWatts) {
            float batteryVolts = readings.GetFloat(RegisterName.BatVoltage);
            gaugeView.setTargetValue(batteryCurrent * batteryVolts);
        } else {
            gaugeView.setTargetValue(batteryCurrent);
        }
        int socVal = readings.GetInt(RegisterName.SOC);
        SOCGauge soc = (SOCGauge) findViewById(R.id.SOC);
        soc.setValue(socVal);
    }

    @Override
    protected void onPause() {
        super.onStop();
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, getApplicationContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Control", ConnectionState.Paused.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        StartModbus();
    }

    private void StartModbus() {
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, getApplicationContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Control", ConnectionState.Connected.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

    private View.OnClickListener GetClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (_lockScale == false) {
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(300);
                    animation.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation arg0) {
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
                            SolarGauge gaugeView = (SolarGauge) v;
                            if (_bidirectionalUnitsInWatts) {
                                _biDirectionalScaleIndex++;
                                if (_biDirectionalScaleIndex >= Constants.BiDirectionalPowerScales.length) {
                                    _biDirectionalScaleIndex = 0;
                                }
                                gaugeView.setScaleEnd(Constants.BiDirectionalPowerScales[_biDirectionalScaleIndex]);
                                settings.edit().putInt(Constants.BiDirectionalPowerScale, _biDirectionalScaleIndex).commit();
                            } else {
                                _batteryCurrentScaleIndex++;
                                if (_batteryCurrentScaleIndex >= Constants.BatteryCurrentScales.length) {
                                    _batteryCurrentScaleIndex = 0;
                                }
                                gaugeView.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
                                settings.edit().putInt(Constants.BatteryCurrentScale, _batteryCurrentScaleIndex).commit();
                            }
                            Animation animation2 = new AlphaAnimation(0.0f, 1.0f);
                            animation2.setDuration(1000);
                            v.startAnimation(animation2);
                        }

                        @Override
                        public void onAnimationRepeat(Animation arg0) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onAnimationStart(Animation arg0) {
                            // TODO Auto-generated method stub

                        }

                    });
                    v.startAnimation(animation);
                }
            }
        };
    }
}
