package ca.farrelltonsolar.classic;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.farrelltonsolar.j2modlite.Modbus;

/**
 * Created by Graham on 01/03/14.
 */
public class PortScanner {


    private static String _startIP = "";
    private static int _scanCount;
    private boolean _foundClassic;
    private InetAddress _inetAddress = null; //the slave's address
    private int _port = Modbus.DEFAULT_PORT;

    public boolean ClassicFound() {
        synchronized (this) {
            return _foundClassic;
        }
    }

    private void SetClassicFound() {
        synchronized (this) {
            _foundClassic = true;
        }
    }

    public void SetStartIP(String ipAddress) {
        _startIP = ipAddress;
    }

    public InetAddress GetFoundInetAddress() {
        return _inetAddress;
    }

    public void SetScanCount(int count) {
        _scanCount = count;
    }

    public void SetupDefaultScan() throws UnknownHostException {
        _foundClassic = false;
        WifiManager wifii = (WifiManager) MyApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        if (wifii != null) {
            int gateway = wifii.getDhcpInfo().gateway;
            String _baseIP;
            if (gateway != 0) {
                InetAddress gatewayAddress = GetInetAddress(gateway);
                _baseIP = gatewayAddress.getHostAddress();
                Log.d(Constants.LOG_TAG, String.format("Gateway address: %s", _baseIP));
                _baseIP = _baseIP.substring(0, _baseIP.lastIndexOf("."));
                _baseIP += ".";
            } else { // default for testing on emulator
                _baseIP = "192.168.0.";
            }
            Context ctx = MyApplication.getAppContext();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            _startIP = _baseIP + "0";
            _scanCount = 255;
            settings.edit().putString(Constants.START_IP_PREFERENCE, _startIP).commit();
            settings.edit().putString(Constants.END_IP_COUNT_PREFERENCE, String.valueOf(_scanCount)).commit();
        }
    }

    private InetAddress GetInetAddress(int address) throws UnknownHostException {
        byte[] ipAddress = BigInteger.valueOf(address).toByteArray();
        byte[] swapBytes = new byte[ipAddress.length];
        for (int i = 0; i < ipAddress.length; i++) {
            swapBytes[i] = ipAddress[ipAddress.length - i - 1];
        }
        return InetAddress.getByAddress(swapBytes);
    }

    public boolean ScanLocalSubnet() throws UnknownHostException, InterruptedException {
        // check last address used
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        String lastIp = settings.getString(Constants.LAST_IP_ADDRESS, "");
        if (lastIp != "") {
            TestIP(lastIp);
            if (ClassicFound()) {
                return true;
            }
            else {
                settings.edit().putString(Constants.LAST_IP_ADDRESS, "").commit();
            }
        }

        // Loop to scan each address on the local subnet
        if (_startIP.equals("")) {
            SetupDefaultScan();
        }
        if (_startIP.equals("")) {
            Thread.sleep(2000);
            return false;
        }
        String baseIP = _startIP.substring(0, _startIP.lastIndexOf(".") + 1);
        String endIP = _startIP.substring(_startIP.lastIndexOf(".") + 1);
        Integer firstAddress = Integer.valueOf(endIP);
        Integer lastAddress = firstAddress + _scanCount;
        if (lastAddress > 255) {
            lastAddress = 255;
        }
        Log.d(Constants.LOG_TAG, String.format("Scanning subnet from address: %s to address %s", baseIP + firstAddress, baseIP + lastAddress));
        _foundClassic = false;
        ExecutorService service = Executors.newFixedThreadPool(20);
        for (int i = firstAddress; i <= lastAddress; i++) {
            service.submit(new Task(baseIP + i));
            if (ClassicFound()) {
                break;
            }
        }
        service.shutdown();
        while (!service.isTerminated()) {
            if (ClassicFound()) {
                service.shutdownNow();
            }
        }
        if (!ClassicFound()) {
            Log.d(Constants.LOG_TAG, "Did not find the classic on the local subnet");
            Thread.sleep(10000);
        } else {
            String foundAddress = _inetAddress.getHostAddress();
            Log.d(Constants.LOG_TAG, String.format("Found classic at address: %s", foundAddress));
            settings.edit().putString(Constants.LAST_IP_ADDRESS, foundAddress).commit();
        }
        return ClassicFound();
    }

    final class Task implements Runnable {
        private String _ipAddress;

        public Task(String ip) {
            this._ipAddress = ip;
        }

        @Override
        public void run() {
            if (!ClassicFound()) {
                TestIP(_ipAddress);
            }
        }
    }

    private final void TestIP(String ip) {
        final Socket mySocket = new Socket();
        try {
            SocketAddress address = new InetSocketAddress(ip, _port);
            mySocket.setReuseAddress(true);
            mySocket.setReceiveBufferSize(32);
            mySocket.setSoLinger(true, 0);
            mySocket.setSendBufferSize(16);
            mySocket.setTcpNoDelay(true);
            mySocket.connect(address, 500);
            if (mySocket.isConnected()) {
                SetClassicFound();
                _inetAddress = mySocket.getInetAddress();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mySocket.close();

            } catch (IOException ignored) {

            }
        }
    }
}
