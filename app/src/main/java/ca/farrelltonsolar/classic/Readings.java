package ca.farrelltonsolar.classic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class Readings {
    final Object lock = new Object();

    public Readings() {
        readings = new Bundle();
    }

    public Readings(Bundle init) {
        readings = init;
    }

    Bundle readings;

    public Bundle getReadings() {
        return readings;
    }

    public Float getFloat(RegisterName name) {
        synchronized (lock) {
            return readings.getFloat(name.name(), 0);
        }
    }

    public int getInt(RegisterName name) {
        synchronized (lock) {
            return readings.getInt(name.name(), 0);
        }
    }

    public String GetString(RegisterName name) {
        synchronized (lock) {
            return readings.getString(name.name());
        }
    }


    public Boolean getBoolean(RegisterName name) {
        synchronized (lock) {
            return readings.getBoolean(name.name(), false);
        }
    }

    public void set(RegisterName name, Float value) {
        synchronized (lock) {
            readings.putFloat(name.name(), value);
        }
    }

    public void set(RegisterName name, int value) {
        synchronized (lock) {
            readings.putInt(name.name(), value);
        }
    }

    public void set(RegisterName name, String value) {
        synchronized (lock) {
            readings.putString(name.name(), value);
        }
    }

    public void set(RegisterName name, Boolean value) {
        synchronized (lock) {
            readings.putBoolean(name.name(), value);
        }
    }

    public void broadcastReadings(Context context, String action) {
        if (!readings.isEmpty()) {
            Intent intent = new Intent(action);
            intent.putExtra("readings", readings);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
