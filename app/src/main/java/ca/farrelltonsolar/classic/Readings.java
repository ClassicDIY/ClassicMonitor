package ca.farrelltonsolar.classic;

import android.os.Bundle;

public class Readings {
    final Object lock = new Object();

    public Readings() {
        readings = new Bundle();
    }

    public Readings(Bundle init) {
        readings = init;
    }

    Bundle readings;

    public Bundle GetReadings() {
        return readings;
    }

    public Float GetFloat(RegisterName name) {
        synchronized (lock) {
            return readings.getFloat(name.name(), 0);
        }
    }

    public int GetInt(RegisterName name) {
        synchronized (lock) {
            return readings.getInt(name.name(), 0);
        }
    }

    public String GetString(RegisterName name) {
        synchronized (lock) {
            return readings.getString(name.name());
        }
    }


    public Boolean GetBoolean(RegisterName name) {
        synchronized (lock) {
            return readings.getBoolean(name.name(), false);
        }
    }

    public void Set(RegisterName name, Float value) {
        synchronized (lock) {
            readings.putFloat(name.name(), value);
        }
    }

    public void Set(RegisterName name, int value) {
        synchronized (lock) {
            readings.putInt(name.name(), value);
        }
    }

    public void Set(RegisterName name, String value) {
        synchronized (lock) {
            readings.putString(name.name(), value);
        }
    }

    public void Set(RegisterName name, Boolean value) {
        synchronized (lock) {
            readings.putBoolean(name.name(), value);
        }
    }
}
