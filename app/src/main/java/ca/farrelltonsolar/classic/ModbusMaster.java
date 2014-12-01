package ca.farrelltonsolar.classic;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import ca.farrelltonsolar.j2modlite.Modbus;
import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.ModbusIOException;
import ca.farrelltonsolar.j2modlite.ModbusSlaveException;
import ca.farrelltonsolar.j2modlite.facade.ModbusTCPMaster;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersResponse;
import ca.farrelltonsolar.j2modlite.procimg.Register;

// Classic modbus table
//            new Register { Address = 4115, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4116, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4117, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4118, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4119, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = address => U16(address)},
//            new Register { Address = 4120, Label = "Battery charge state", UnitOfMeasure = "", Conversion = address => ChargeState(address)},
//            new Register { Address = 4121, Label = "Average PV inout current", UnitOfMeasure = "Amps", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4122, Label = "PV VOC", UnitOfMeasure = "Volts", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4125, Label = "Daily amp hours", UnitOfMeasure = "Amp hours", Conversion = address => U16_OneDec(address)},
//            new Register { Address = 4126, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = address => U32_OneDec(address)},
//            new Register { Address = 4128, Label = "Total Amp hours", UnitOfMeasure = "Amp hours", Conversion = address => U32_OneDec(address)},
//            new Register { Address = 4130, Label = "Info flag", UnitOfMeasure = "", Conversion = address => Info(address)}

//            new Tupple { Description = "(Off) No power, waiting for power source, battery voltage over set point.", Value = 0 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 3 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 4 },
//            new Tupple { Description = "(Float) Battery is full and regulating battery voltage at float set point.", Value = 5 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 6 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 7 },
//            new Tupple { Description = "(Error) Input voltage is above maximum classic operating voltage.", Value = 10 },
//            new Tupple { Description = "(Equalizing) Max power point tracking. Seeking equalize set point voltage.", Value = 18 }


// TriStar modbus table
//            new Register { Address = 1, Label = "V Scale", UnitOfMeasure = "", Conversion = address => U32(address)},
//            new Register { Address = 3, Label = "A Scale", UnitOfMeasure = "", Conversion = address => U32(address)},
//            new Register { Address = 25, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = address => VScale(address)},
//            new Register { Address = 28, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = address => VScale(address)},
//            new Register { Address = 29, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = address => IScale(address)},
//            new Register { Address = 30, Label = "Average PV current", UnitOfMeasure = "Amps", Conversion = address => IScale(address)},
//            new Register { Address = 45, Label = "Info flag", UnitOfMeasure = "", Conversion = address => Info(address)},
//            new Register { Address = 51, Label = "Battery charge state", UnitOfMeasure = "", Conversion = address => ChargeState(address)},
//            new Register { Address = 58, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = address => U16(address)},
//            new Register { Address = 59, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = address => PScale(address)},
//            new Register { Address = 69, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = address => WHr(address)}

//            new Tupple { Description = "(Start) System startup.", Value = 0 },
//            new Tupple { Description = "(Night check) No power, detecting nightfall.", Value = 1 },
//            new Tupple { Description = "(Disconnected) No power.", Value = 2 },
//            new Tupple { Description = "(Night) No power, waiting for power source.", Value = 3 },
//            new Tupple { Description = "(Fault) Detected fault.", Value = 4 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 5 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 6 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 7 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 8 },
//            new Tupple { Description = "(Slave) State set by master charge controller.", Value = 9 }



public class ModbusMaster extends IntentService {

    private BroadcastNotifier _broadcaster;

    private PortScanner _portScanner;
    private ModbusTCPMaster _modbusMaster;

    private InetAddress _inetAddress = null; //the slave's address
    private int _port = Modbus.DEFAULT_PORT;
    private int _pollTime = Constants.MODBUS_POLL_TIME;
    private int _reference = 4100; //the reference; offset where to start reading from
    private Readings _readings = new Readings();
    private LogEntry _dayLogEntry = new LogEntry();
    private LogEntry _minuteLogEntry = new LogEntry();

    private boolean _scanLocalSubnet = true;
    private boolean _foundWhizBangJr = false;
    private boolean _foundTriStar = false;
    private ConnectionState _connectionState = ConnectionState.NotFound;
    private Function _function = Function.Registers;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ModbusMaster() {
        super("ModbusMaster");
        _portScanner = new PortScanner();
        _broadcaster = new BroadcastNotifier();
    }

    private ConnectionState GetConnectionState() {
        ConnectionState rVal;
        synchronized (this) {
            rVal = _connectionState;
        }
        return rVal;
    }

    public void SetConnectionState(ConnectionState state) {
        synchronized (this) {
            _connectionState = state;
        }
    }

    private Function GetFunction() {
        Function rVal;
        synchronized (this) {
            rVal = _function;
        }
        return rVal;
    }

    public void SetFunction(Function state) {
        synchronized (this) {
            _function = state;
        }
    }

    public void SetIPAddress(String IPAddress) {

        try {
            synchronized (this) {
                InetSocketAddress address = new InetSocketAddress(IPAddress, _port);
                _inetAddress = address.getAddress();
            }
            SetConnectionState(ConnectionState.NotFound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetPort(int p) {
        try {
            _port = p;
            InetSocketAddress address = new InetSocketAddress(_inetAddress.getHostAddress(), _port);
            _inetAddress = address.getAddress();
            SetConnectionState(ConnectionState.NotFound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetSubnetScan(boolean val) {
        _scanLocalSubnet = val;
    }

    public String Slave() {
        ConnectionState state = GetConnectionState();
        if (state == ConnectionState.Connected) {
            return String.format(MyApplication.getAppContext().getString(R.string.FoundClassic), _inetAddress.toString(), _port);
        } else if (state == ConnectionState.Initialized) {
            if (_scanLocalSubnet) {
                return String.format(MyApplication.getAppContext().getString(R.string.ClassicNotFound), _port);
            } else {
                return String.format(MyApplication.getAppContext().getString(R.string.ClassicNotConnecting), _inetAddress.toString(), _port);
            }
        } else {
            return "";
        }
    }

    //    Our handler for received Intents.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ca.farrelltonsolar.classic.ModbusControl".equalsIgnoreCase(intent.getAction())) {
                int val = intent.getIntExtra("Control", -1);
                if (val >= 0 && val < ConnectionState.values().length) {
                    ConnectionState state = ConnectionState.values()[val];
                    if (GetConnectionState() != ConnectionState.Stopped) {
                        SetConnectionState(state);
                        Log.d(Constants.LOG_TAG, String.format("ModbusControl: %s", state.name()));
                    } else {
                        Log.d(Constants.LOG_TAG, String.format("ModbusControl is stopped, cannot be set to: %s", state.name()));
                    }
                } else {
                    int page = intent.getIntExtra("Page", -1);
                    if (page >= 0 && page < Function.values().length) {
                        Function function = Function.values()[page];
                        Bundle args = intent.getBundleExtra("Args");
                        SetFunction(function);
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        SetConnectionState(ConnectionState.Stopped);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //android.os.Debug.waitForDebugger();
        Log.d(Constants.LOG_TAG, String.format("Modbus onHandleIntent action: %s", intent.getAction()));
        if ("ca.farrelltonsolar.classic.ModbusSetup".equalsIgnoreCase(intent.getAction())) {
            if (GetConnectionState() == ConnectionState.NotFound) {
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).registerReceiver(mMessageReceiver, new IntentFilter("ca.farrelltonsolar.classic.ModbusControl"));
                LoadSettings();
                DefaultReadings();
                if (_scanLocalSubnet) {
                    Log.d(Constants.LOG_TAG, "Scanning local subnet");
                    boolean classicFound = false;
                    while (!classicFound) {
                        try {
                            classicFound = _portScanner.ScanLocalSubnet();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                        if (!classicFound) {
                            DefaultReadings();
                            LoadSettings();
                            if (!_scanLocalSubnet) {
                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
                                settings.edit().putString(Constants.LAST_IP_ADDRESS, "").commit();
                                SetConnectionState(ConnectionState.Initialized);
                                break;
                            }
                        }
                    }
                    _inetAddress = _portScanner.GetFoundInetAddress();
                    SetConnectionState(ConnectionState.Initialized);
                } else {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
                    settings.edit().putString(Constants.LAST_IP_ADDRESS, "").commit();
                    SetConnectionState(ConnectionState.Initialized);
                }
                boolean modbusSetup = false;
                boolean running = true;
                boolean connected = false;
                DefaultReadings();
                while (running) {
                    try {
                        if (SetupModbus()) {
                            if (modbusSetup == false) {
                                String message = String.format(MyApplication.getAppContext().getString(R.string.FoundClassic), _inetAddress.toString(), _port);
                                BroadcastToast(message);
                            }
                            modbusSetup = true;
                            connected = true;
                            synchronized (this) {
                                _connectionState = ConnectionState.Connected;
                            }
                            if (LookForTriStar() == false) {
                                LookForWhizBangJr();
                                GetUnitName();
                            }
                        } else {
                            Thread.sleep(2000);
                            if (modbusSetup) {
                                DefaultReadings();
                            }
                        }
                    } catch (ModbusIOException e) {
                        e.printStackTrace();
                    } catch (ModbusSlaveException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (GetConnectionState() == ConnectionState.Stopped) {
                        running = false;
                        if (connected) {
                            _modbusMaster.disconnect();
                            connected = false;
                        }
                    }
                    while (connected) {
                        try {
                            if (Function.Registers.equals(GetFunction())) {
                                GetModbusReadings();
                                if (_dayLogEntry.GetLogs().isEmpty()) {
                                    LoadDayLogs();
                                }
                                if (_minuteLogEntry.GetLogs().isEmpty()) {
                                    LoadMinuteLogs();
                                }
                            }
                            else if (Function.DayLogs.equals(GetFunction())) {
                                BroadcastLogs("ca.farrelltonsolar.classic.DayLogs", _dayLogEntry);
                                SetFunction(Function.Registers); // back to registers when loaded
                            }
                            else if (Function.MinuteLogs.equals(GetFunction())) {
                                BroadcastLogs("ca.farrelltonsolar.classic.MinuteLogs", _minuteLogEntry);
                                SetFunction(Function.Registers); // back to registers when loaded
                            }
                        } catch (ModbusException e) {
                            e.printStackTrace();
                            connected = false;
                            _modbusMaster.disconnect();
                            synchronized (this) {
                                _connectionState = ConnectionState.Initialized;
                            }
                        } finally {
                            try {
                                Thread.sleep(_pollTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                _modbusMaster.disconnect();
                                synchronized (this) {
                                    _connectionState = ConnectionState.Initialized;
                                }
                            }
                        }
                        if (GetConnectionState() == ConnectionState.Stopped) {
                            running = false;
                            connected = false;
                            _modbusMaster.disconnect();
                            Log.d(Constants.LOG_TAG, "Modbus disconnected");
                        }
                        if (GetConnectionState() == ConnectionState.Paused) {
                            DefaultReadings();
                            while (GetConnectionState() == ConnectionState.Paused) {
                                try {
                                    if (_modbusMaster.isConnected()) {
                                        _modbusMaster.disconnect();
                                    }
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    _modbusMaster.disconnect();
                                    synchronized (this) {
                                        _connectionState = ConnectionState.Initialized;
                                    }
                                    return;
                                }
                            }
                            if (_modbusMaster.isConnected() == false) {
                                try {
                                    _modbusMaster.connect();
                                } catch (Exception e) {
                                    connected = false;
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                Log.d(Constants.LOG_TAG, "ca.farrelltonsolar.classic.Modbus terminating");
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).unregisterReceiver(mMessageReceiver);
            }
            SetConnectionState(ConnectionState.NotFound);
        }
    }

    public void LoadSettings() {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
//        settings.edit().clear().commit();
            try {
                String address = settings.getString(Constants.IP_ADDRESS_PREFERENCE, "");
                SetIPAddress(address);
                String port = settings.getString(Constants.PORT_PREFERENCE, "502");
                SetPort(Integer.valueOf(port));
                SetSubnetScan(settings.getBoolean(Constants.SUBNET_SCAN_PREFERENCE, true));
                if (_scanLocalSubnet) {
                    _portScanner.SetStartIP(settings.getString(Constants.START_IP_PREFERENCE, ""));
                    String countString = settings.getString(Constants.END_IP_COUNT_PREFERENCE, "255");
                    _portScanner.SetScanCount(Integer.valueOf(countString));
                }
            } catch (Exception e) {
                Log.d(Constants.LOG_TAG, "Reset settings and use defaults");
                settings.edit().clear().commit();
                SetPort(502);
                SetSubnetScan(true);
            }
        } catch (Exception unused) {
        }

    }

    private void DefaultReadings() {
        _readings.Set(RegisterName.Power, 0.0f);
        _readings.Set(RegisterName.BatVoltage, 0.0f);
        _readings.Set(RegisterName.BatCurrent, 0.0f);
        _readings.Set(RegisterName.PVVoltage, 0.0f);
        _readings.Set(RegisterName.PVCurrent, 0.0f);
        _readings.Set(RegisterName.EnergyToday, 0.0f);
        _readings.Set(RegisterName.TotalEnergy, 0.0f);
        _readings.Set(RegisterName.ChargeState, -1);
        _readings.Set(RegisterName.ConnectionState, 0);
        _readings.Set(RegisterName.SOC, 0);
        _readings.Set(RegisterName.Aux1, false);
        _readings.Set(RegisterName.Aux2, false);
        BroadcastReadings();
    }

    private boolean SetupModbus() throws ModbusIOException, ModbusSlaveException {
        boolean rVal = false;
        try {
            _modbusMaster = new ModbusTCPMaster(_inetAddress, _port, 1);
            _modbusMaster.setRetries(Constants.MODBUS_RETRIES);
            _modbusMaster.connect();
            rVal = _modbusMaster.isConnected();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            Log.w(Constants.LOG_TAG, String.format("Could not connect to %s", _inetAddress.toString()));
        }
        return rVal;
    }

    protected float WHr(float val)
    {
        val /= 1000;
        return val;
    }

    protected float PScale(float val)
    {
        val = val * v_pu * i_pu;
        val /= 131072;
        return val;
    }
    protected float VScale(float val)
    {
        val = val * v_pu;
        val /= 32768;
        return val;
    }
    protected float IScale(float val)
    {
        val = val * i_pu;
        val /= 32768;
        return val;
    }

    private void GetModbusReadings() throws ModbusException {
        try {
            if (_foundTriStar){
                ReadMultipleRegistersResponse regRes = _modbusMaster.readMultipleRegisters(0, 80);
                if (regRes != null) {

                    _readings.Set(RegisterName.BatVoltage, VScale(regRes.getRegisterValue(OffsetFor(25))));
                    _readings.Set(RegisterName.PVVoltage, VScale(regRes.getRegisterValue(OffsetFor(28))));
                    _readings.Set(RegisterName.BatCurrent, IScale(regRes.getRegisterValue(OffsetFor(29))));
                    _readings.Set(RegisterName.PVCurrent, IScale(regRes.getRegisterValue(OffsetFor(30))));

                    _readings.Set(RegisterName.Power, PScale(regRes.getRegisterValue(OffsetFor(59))));
                    _readings.Set(RegisterName.EnergyToday, WHr(regRes.getRegisterValue(OffsetFor(69))));
                    _readings.Set(RegisterName.TotalEnergy, regRes.getRegisterValue(OffsetFor(58)));

                }
            } else {
                ReadMultipleRegistersResponse regRes = _modbusMaster.readMultipleRegisters(_reference, 36);
                if (regRes != null) {
                    if (!_foundWhizBangJr) {
                        Register a = regRes.getRegister(OffsetFor(4117));
                        _readings.Set(RegisterName.BatCurrent, a.toShort() / 10.0f);
                    }
                    _readings.Set(RegisterName.Power, (float) regRes.getRegisterValue(OffsetFor(4119)));
                    _readings.Set(RegisterName.BatVoltage, regRes.getRegisterValue(OffsetFor(4115)) / 10.0f);
                    _readings.Set(RegisterName.PVVoltage, regRes.getRegisterValue(OffsetFor(4116)) / 10.0f);
                    _readings.Set(RegisterName.PVCurrent, regRes.getRegisterValue(OffsetFor(4121)) / 10.0f);
                    _readings.Set(RegisterName.EnergyToday, regRes.getRegisterValue(OffsetFor(4118)) / 10.0f);
                    _readings.Set(RegisterName.TotalEnergy, regRes.getRegisterValue(OffsetFor(4126)) / 10.0f);
                    _readings.Set(RegisterName.ChargeState, MSBFor(regRes.getRegisterValue(OffsetFor(4120))));
                    int infoFlag = regRes.getRegisterValue(OffsetFor(4130));
                    _readings.Set(RegisterName.Aux1, (infoFlag & 0x4000) != 0);
                    _readings.Set(RegisterName.Aux2, (infoFlag & 0x8000) != 0);
                } else {
                    Log.d(Constants.LOG_TAG, String.format("Modbus readMultipleRegisters returned null"));
                    throw new ModbusException("Failed to read data from modbus");
                }
                if (_foundWhizBangJr) {
                    ReadMultipleRegistersResponse regRes2 = _modbusMaster.readMultipleRegisters(4360, 16);
                    if (regRes2 != null) {
                        Register a = regRes2.getRegister(10);
                        _readings.Set(RegisterName.BatCurrent, a.toShort() / 10.0f);
                        Register soc = regRes2.getRegister(12);
                        short socVal = soc.toShort();
                        _readings.Set(RegisterName.SOC, socVal);
                    }
                }
                _readings.Set(RegisterName.BiDirectional, _foundWhizBangJr);
            }
            BroadcastReadings();
            //Log.d(Constants.LOG_TAG, String.format(String.format("Modbus BroadcastReadings on thread: %d", Thread.currentThread().getId())));
        } catch (ModbusException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception all) {
            all.printStackTrace();
            throw new ModbusException(all.getMessage());
        }
    }

    private void BroadcastToast(String message) {
        Intent intent2 = new Intent("ca.farrelltonsolar.classic.Toast");
        intent2.setClass(MyApplication.getAppContext(), GaugePage.class);
        intent2.putExtra("message", message);
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent2);
    }

    private void BroadcastReadings() {
        Intent intent = new Intent("ca.farrelltonsolar.classic.GaugePage");
        intent.setClass(MyApplication.getAppContext(), GaugePage.class);
        intent.putExtra("readings", _readings.GetReadings());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
    }

    private void LookForWhizBangJr() {
        try {
            ReadMultipleRegistersResponse regRes = _modbusMaster.readMultipleRegisters(4360, 12);
            if (regRes != null) {
                Register a = regRes.getRegister(10);
                _foundWhizBangJr = a.toShort() != 0;
            }
        } catch (ModbusException ignore) {

        }
    }

    private boolean LookForTriStar() {
        _foundTriStar = false;
        try {
            ReadMultipleRegistersResponse regRes = _modbusMaster.readMultipleRegisters(0, 4);
            if (regRes != null) {

                _foundTriStar = regRes.getRegister(0).toShort() != 0;
                if (_foundTriStar) {
                    float hi = (float) regRes.getRegister(0).toShort();
                    float lo = (float) regRes.getRegister(1).toShort();
                    lo = lo / 65536;
                    v_pu = hi + lo;

                    hi = (float) regRes.getRegister(2).toShort();
                    lo = (float) regRes.getRegister(3).toShort();
                    lo = lo / 65536;
                    i_pu = hi + lo;
                    _reference = 0;
                }
            }
        } catch (ModbusException ignore) {

        }
        return _foundTriStar;
    }

    private float v_pu;
    private float i_pu;

    private void GetUnitName() {
        try {
            ReadMultipleRegistersResponse regRes = _modbusMaster.readMultipleRegisters(4209, 4);
            if (regRes != null) {
                byte[] v0 = regRes.getRegister(0).toBytes();
                byte[] v1 = regRes.getRegister(1).toBytes();
                byte[] v2 =regRes.getRegister(2).toBytes();
                byte[] v3 = regRes.getRegister(3).toBytes();

                byte[] temp = new byte[8];
                temp[0] = v0[1];
                temp[1] = v0[0];
                temp[2] = v1[1];
                temp[3] = v1[0];
                temp[4] = v2[1];
                temp[5] = v2[0];
                temp[6] = v3[1];
                temp[7] = v3[0];
                String unitName = new String(temp);
                unitName = unitName.trim();
                if (unitName.length() > 0) {
                    Intent intent = new Intent("ca.farrelltonsolar.classic.Unit");
                    intent.setClass(MyApplication.getAppContext(), MainActivity.class);
                    intent.putExtra("UnitName", unitName);
                    LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
                }
            }

        } catch (ModbusException ignore) {

        }
    }

    private void LoadDayLogs() throws ModbusException {
        int day = 0;
        short[] mWattHourLog = new short[512];
        short[] mFloatLog = new short[512];
        while (day < 365) {
            ReadFileTransferResponse regRes = _modbusMaster.readFileTransfer(day, Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, Constants.CLASSIC_DAILY_LOG);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    int j = count-1;
                    for (int i = 0; i < count; i++, j--) {
                        mWattHourLog[j + day] = registerToShort(regRes.getRegister(i).toBytes());
                    }
                    day += count;
                }
            } else {

                Log.d(Constants.LOG_TAG, String.format("Modbus readCustom returned null"));
                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }
        day = 0;
        while (day < 365) {
            ReadFileTransferResponse regRes = _modbusMaster.readFileTransfer(day, Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, Constants.CLASSIC_DAILY_LOG);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    int j = count-1;
                    for (int i = 0; i < count; i++, j--) {
                        mFloatLog[j + day] = registerToShort(regRes.getRegister(i).toBytes());
                    }
                    day += count;
                }
            } else {

                Log.d(Constants.LOG_TAG, String.format("Modbus readCustom returned null"));
                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }
        _dayLogEntry.Set(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, mWattHourLog);
        _dayLogEntry.Set(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, mFloatLog);
    }

    private void LoadMinuteLogs() throws ModbusException {
        int minute = 0;
        short[] mWattLog = new short[1024];
        int d = 0;
        while (minute < 1024) {
            ReadFileTransferResponse regRes = _modbusMaster.readFileTransfer(minute, Constants.CLASSIC_POWER_HOURLY_CATEGORY, Constants.CLASSIC_MINUTE_LOG);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    if (count >= 1024) {
                        break;
                    }
                    int j = count - 1;
                    for (int i = 0; i < count; i++, j--) {
                        mWattLog[i + minute] = registerToShort(regRes.getRegister(j).toBytes());
                    }
                    minute += count;
                }
            } else {

                Log.d(Constants.LOG_TAG, String.format("Modbus readCustom returned null"));
                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }
        short[] mHourlyWattLog = new short[24];

        // calculate the hourly power average, assume each entry is at a 5 minute interval (/12)
        int v = 12;
        int w = 0;
        int sum = 0;
        for (int k = 0; k < mWattLog.length; k++) {
            if (v > 0) {
                sum += mWattLog[k];
                v--;
            }
            else {
                mHourlyWattLog[w++] = (short) (sum / 12);
                sum = 0;
                v = 12;
                if (w >= mHourlyWattLog.length) {
                    break;
                }
            }
        }
        _minuteLogEntry.Set(Constants.CLASSIC_POWER_HOURLY_CATEGORY, mHourlyWattLog);
        //LoadMinuteTimestamps();
    }


//    private void LoadMinuteTimestamps() throws ModbusException {
//        int minute = 0;
//        short[] mMinuteTimestampLog = new short[512];
//        short[] mHourTimestampLog = new short[512];
//        while (minute < 512) {
//            ReadFileTransferResponse regRes = _modbusMaster.readFileTransfer(minute, Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, Constants.CLASSIC_MINUTE_LOG);
//            if (regRes != null) {
//                int count = regRes.getWordCount();
//                if (count > 0) {
//                    if (count >= 512) {
//                        break;
//                    }
//                    int j = count - 1;
//                    for (int i = 0; i < count; i++, j--) {
//                        short val = registerToShort(regRes.getRegister(j).toBytes());
//                        short min = (short) (val & 0x003f);
//                        short hour = (short) (val  >> 6);
//                        mMinuteTimestampLog[i + minute] = min;
//                        mHourTimestampLog[i + minute] = hour;
//                    }
//                    minute += count;
//                }
//            } else {
//
//                Log.d(Constants.LOG_TAG, String.format("Modbus readCustom returned null"));
//                throw new ModbusException("Failed to read File Transfer data from modbus");
//            }
//        }
//        _minuteLogEntry.Set(Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, mMinuteTimestampLog);
//    }

    private static short registerToShort(byte[] bytes) {
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xff));
    }

    private void BroadcastLogs(String action, LogEntry logEntry) {
        Intent intent = new Intent(action);
        intent.setClass(MyApplication.getAppContext(), CalendarPage.class);
        intent.putExtra("logs", logEntry.GetLogs());
        LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
    }

    private int OffsetFor(int address) {
        return address - _reference - 1; // origin 0
    }

    private int MSBFor(int val) {
        return val >> 8;
    }

}

