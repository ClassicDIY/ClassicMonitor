package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import ca.farrelltonsolar.uicomponents.BaseGauge;
import ca.farrelltonsolar.uicomponents.SOCGauge;
import ca.farrelltonsolar.uicomponents.SolarGauge;

/**
 * Created by Graham on 01/03/14.
 */
public class GaugePage extends Fragment {

    boolean _bidirectionalGaugeScaleSet = false;
    boolean _bidirectionalUnitsInWatts = false;
    boolean _lockScale = false;
    int _powerScaleIndex;
    int _pvVoltageScaleIndex;
    int _pvCurrentScaleIndex;
    int _batteryVoltageScaleIndex;
    int _batteryCurrentScaleIndex;
    int _biDirectionalScaleIndex;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.gauge_page, container, false);
        LoadSettings();
        BaseGauge gauge;

        gauge = (BaseGauge) theView.findViewById(R.id.Power);
        if (gauge != null) {
            gauge.setScaleEnd(Constants.PowerScales[_powerScaleIndex]);
            if (_lockScale == false) {
                gauge.setOnClickListener(GetClickListener(container));
            }
        }
        gauge = (BaseGauge) theView.findViewById(R.id.PVCurrent);
        if (gauge != null) {
            gauge.setScaleEnd(Constants.PVCurrentScales[_pvCurrentScaleIndex]);
            if (_lockScale == false) {
                gauge.setOnClickListener(GetClickListener(container));
            }
        }
        gauge = (BaseGauge) theView.findViewById(R.id.PVVoltage);
        if (gauge != null) {
            gauge.setScaleEnd(Constants.PVVoltScales[_pvVoltageScaleIndex]);
            if (_lockScale == false) {
                gauge.setOnClickListener(GetClickListener(container));
            }
        }
        gauge = (BaseGauge) theView.findViewById(R.id.BatVoltage);
        if (gauge != null) {
            gauge.setScaleEnd(Constants.BatteryVoltScales[_batteryVoltageScaleIndex]);
            if (_lockScale == false) {
                gauge.setOnClickListener(GetClickListener(container));
            }
        }
        gauge = (BaseGauge) theView.findViewById(R.id.BatCurrent);
        if (gauge != null) {
            gauge.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
            if (_lockScale == false) {
                gauge.setOnClickListener(GetClickListener(container));
            }
        }

        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mReadingsReceiver, new IntentFilter("ca.farrelltonsolar.classic.GaugePage"));
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mToastReceiver, new IntentFilter("ca.farrelltonsolar.classic.Toast"));

        return theView;
    }

    private void LoadSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        _bidirectionalUnitsInWatts = settings.getBoolean(Constants.BIDIRECTIONALUNIT_PREFERENCE, false);
        _lockScale = settings.getBoolean(Constants.LOCK_SCALE_PREFERENCE, false);
        _powerScaleIndex = settings.getInt(Constants.PowerScale, 0);
        if (_powerScaleIndex >= Constants.PowerScales.length) {
            _powerScaleIndex = 0;
        }
        _pvVoltageScaleIndex = settings.getInt(Constants.PVVoltScale, 0);
        if (_pvVoltageScaleIndex >= Constants.PVVoltScales.length) {
            _pvVoltageScaleIndex = 0;
        }
        _pvCurrentScaleIndex = settings.getInt(Constants.PVCurrentScale, 0);
        if (_pvCurrentScaleIndex >= Constants.PVCurrentScales.length) {
            _pvCurrentScaleIndex = 0;
        }
        _batteryVoltageScaleIndex = settings.getInt(Constants.BatteryVoltScale, 0);
        if (_batteryVoltageScaleIndex >= Constants.BatteryVoltScales.length) {
            _batteryVoltageScaleIndex = 0;
        }
        _biDirectionalScaleIndex = settings.getInt(Constants.BiDirectionalPowerScale, 0);
        if (_biDirectionalScaleIndex >= Constants.BiDirectionalPowerScales.length) {
            _biDirectionalScaleIndex = 0;
        }
        _batteryCurrentScaleIndex = settings.getInt(Constants.BatteryCurrentScale, 0);
        if (_batteryCurrentScaleIndex >= Constants.BatteryCurrentScales.length) {
            _batteryCurrentScaleIndex = 0;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitializeReadings(view);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            RequestGaugeData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.getUserVisibleHint()) {
            RequestGaugeData();
        }
    }

    private void RequestGaugeData() {
        Intent modbusInitIntent = new Intent("ca.farrelltonsolar.classic.ModbusControl", null, MyApplication.getAppContext(), ModbusMaster.class);
        modbusInitIntent.putExtra("Page", Function.Registers.ordinal());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(modbusInitIntent);
    }

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

            SolarGauge powerGauge = (SolarGauge) this.getView().findViewById(R.id.Power);
            powerGauge.setTargetValue(readings.GetFloat(RegisterName.Power));
            powerGauge.setLeftLed(readings.GetBoolean(RegisterName.Aux1));
            powerGauge.setRightLed(readings.GetBoolean(RegisterName.Aux2));

            BaseGauge gauge = (BaseGauge) this.getView().findViewById(R.id.PVVoltage);
            gauge.setTargetValue(readings.GetFloat(RegisterName.PVVoltage));
            gauge = (BaseGauge) this.getView().findViewById(R.id.PVCurrent);
            gauge.setTargetValue(readings.GetFloat(RegisterName.PVCurrent));
            gauge = (BaseGauge) this.getView().findViewById(R.id.BatVoltage);
            float batteryVolts = readings.GetFloat(RegisterName.BatVoltage);
            gauge.setTargetValue(batteryVolts);
            gauge = (BaseGauge) this.getView().findViewById(R.id.BatCurrent);
            float batteryCurrent = readings.GetFloat(RegisterName.BatCurrent);
            boolean biDirectional = readings.GetBoolean(RegisterName.BiDirectional);
            if (biDirectional) {
                SOCGauge soc = (SOCGauge) this.getView().findViewById(R.id.SOC);
                if (soc.getVisibility() != View.VISIBLE) {
                    soc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        //On click function
                        public void onClick(View view) {
                            startActivity(new Intent(view.getContext(), BigSOC.class));
                        }
                    });
                    soc.setVisibility(View.VISIBLE);
                }
                if (_bidirectionalGaugeScaleSet == false) {
                    if (_bidirectionalUnitsInWatts) {
                        gauge.setScaleEnd(Constants.BiDirectionalPowerScales[_biDirectionalScaleIndex]);
                    } else {
                        gauge.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
                    }
                    _bidirectionalGaugeScaleSet = true;
                }
                if (_bidirectionalUnitsInWatts) {
                    gauge.setTargetValue(batteryCurrent * batteryVolts);
                } else {
                    gauge.setTargetValue(batteryCurrent);
                }
                int socVal = readings.GetInt(RegisterName.SOC);
                soc.setValue(socVal); //  convert x/100 to y/360
            } else {
                gauge.setTargetValue(readings.GetFloat(RegisterName.BatCurrent));
            }
            SetBidirectional(biDirectional);
            TextView tv = (TextView) this.getView().findViewById(R.id.EnergyTotalValue);
            tv.setText(String.valueOf(readings.GetFloat(RegisterName.TotalEnergy)));
            tv = (TextView) this.getView().findViewById(R.id.EnergyTodayValue);
            tv.setText(String.valueOf(readings.GetFloat(RegisterName.EnergyToday)));
            tv = (TextView) this.getView().findViewById(R.id.ChargeStateTitle);
            int cs = readings.GetInt(RegisterName.ChargeState);
            tv.setText(MyApplication.getChargeStateTitleText(cs));
            tv = (TextView) this.getView().findViewById(R.id.ChargeState);
            tv.setText(MyApplication.getChargeStateText(cs));
        } catch (Exception ignore) {

        }
    }

    private void InitializeReadings(View view) {
        BaseGauge powerGauge = (BaseGauge) view.findViewById(R.id.Power);
        powerGauge.setTargetValue(0.0f);
        BaseGauge gauge;
        gauge = (BaseGauge) view.findViewById(R.id.PVVoltage);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.PVCurrent);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.BatVoltage);
        gauge.setTargetValue(0.0f);
        gauge = (BaseGauge) view.findViewById(R.id.BatCurrent);
        gauge.setTargetValue(0.0f);
        TextView tv = (TextView) this.getView().findViewById(R.id.ChargeState);
        tv.setText(getString(R.string.NoConnection));
    }

    private void SetBidirectional(boolean val) {
        BaseGauge gauge = (BaseGauge) this.getView().findViewById(R.id.BatCurrent);
        if (gauge.getBiDirectional() != val) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
            if (_bidirectionalUnitsInWatts) {
                gauge.setScaleEnd(Constants.BiDirectionalPowerScales[_biDirectionalScaleIndex]);
                gauge.setTitle(MyApplication.getAppContext().getString(R.string.BatPowerTitle));
                gauge.setUnit("W");
            } else {
                gauge.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
                gauge.setTitle(MyApplication.getAppContext().getString(R.string.BatCurrentTitle));
                gauge.setUnit("A");
            }
            gauge.setBiDirectional(val);
        }
    }

    private View.OnClickListener GetClickListener(final ViewGroup container) {
        return new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (_lockScale == false) {
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

                        @Override
                        public void onAnimationEnd(Animation arg0) {
                            BaseGauge gauge;
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
                            switch (v.getId()) {
                                case R.id.Power:
                                    _powerScaleIndex++;
                                    if (_powerScaleIndex >= Constants.PowerScales.length) {
                                        _powerScaleIndex = 0;
                                    }
                                    BaseGauge powerGauge = (BaseGauge) v;
                                    powerGauge.setScaleEnd(Constants.PowerScales[_powerScaleIndex]);
                                    settings.edit().putInt(Constants.PowerScale, _powerScaleIndex).commit();
                                    break;
                                case R.id.PVVoltage:
                                    gauge = (BaseGauge) v;
                                    _pvVoltageScaleIndex++;
                                    if (_pvVoltageScaleIndex >= Constants.PVVoltScales.length) {
                                        _pvVoltageScaleIndex = 0;
                                    }
                                    gauge.setScaleEnd(Constants.PVVoltScales[_pvVoltageScaleIndex]);
                                    settings.edit().putInt(Constants.PVVoltScale, _pvVoltageScaleIndex).commit();
                                    break;
                                case R.id.PVCurrent:
                                    gauge = (BaseGauge) v;
                                    _pvCurrentScaleIndex++;
                                    if (_pvCurrentScaleIndex >= Constants.PVCurrentScales.length) {
                                        _pvCurrentScaleIndex = 0;
                                    }
                                    gauge.setScaleEnd(Constants.PVCurrentScales[_pvCurrentScaleIndex]);
                                    settings.edit().putInt(Constants.PVCurrentScale, _pvCurrentScaleIndex).commit();
                                    break;
                                case R.id.BatVoltage:
                                    gauge = (BaseGauge) v;
                                    _batteryVoltageScaleIndex++;
                                    if (_batteryVoltageScaleIndex >= Constants.BatteryVoltScales.length) {
                                        _batteryVoltageScaleIndex = 0;
                                    }
                                    gauge.setScaleEnd(Constants.BatteryVoltScales[_batteryVoltageScaleIndex]);
                                    settings.edit().putInt(Constants.BatteryVoltScale, _batteryVoltageScaleIndex).commit();
                                    break;
                                case R.id.BatCurrent:
                                    gauge = (BaseGauge) v;
                                    boolean isBidirectional = gauge.getBiDirectional();
                                    if (isBidirectional && _bidirectionalUnitsInWatts) {
                                        _biDirectionalScaleIndex++;
                                        if (_biDirectionalScaleIndex >= Constants.BiDirectionalPowerScales.length) {
                                            _biDirectionalScaleIndex = 0;
                                        }
                                        gauge.setScaleEnd(Constants.BiDirectionalPowerScales[_biDirectionalScaleIndex]);
                                        settings.edit().putInt(Constants.BiDirectionalPowerScale, _biDirectionalScaleIndex).commit();
                                    } else {
                                        _batteryCurrentScaleIndex++;
                                        if (_batteryCurrentScaleIndex >= Constants.BatteryCurrentScales.length) {
                                            _batteryCurrentScaleIndex = 0;
                                        }
                                        gauge.setScaleEnd(Constants.BatteryCurrentScales[_batteryCurrentScaleIndex]);
                                        settings.edit().putInt(Constants.BatteryCurrentScale, _batteryCurrentScaleIndex).commit();
                                    }
                                    break;
                            }
                            Animation animation2 = new AlphaAnimation(0.0f, 1.0f);
                            animation2.setDuration(500);
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
