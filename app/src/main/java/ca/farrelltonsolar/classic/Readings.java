package ca.farrelltonsolar.classic;

import android.os.Bundle;

public class Readings {
    final Object lock = new Object();

    public Readings() {
        _readings = new Bundle();
    }

    public Readings(Bundle init) {
        _readings = init;
    }

    Bundle _readings;

    public Bundle GetReadings() {
        return _readings;
    }

    public Float GetFloat(RegisterName name) {
        synchronized (lock) {
            return _readings.getFloat(name.name(), 0);
        }
    }

    public int GetInt(RegisterName name) {
        synchronized (lock) {
            return _readings.getInt(name.name(), 0);
        }
    }

    public String GetString(RegisterName name) {
        synchronized (lock) {
            return _readings.getString(name.name());
        }
    }


    public Boolean GetBoolean(RegisterName name) {
        synchronized (lock) {
            return _readings.getBoolean(name.name(), false);
        }
    }

    public void Set(RegisterName name, Float value) {
        synchronized (lock) {
            _readings.putFloat(name.name(), value);
        }
    }

    public void Set(RegisterName name, int value) {
        synchronized (lock) {
            _readings.putInt(name.name(), value);
        }
    }

    public void Set(RegisterName name, String value) {
        synchronized (lock) {
            _readings.putString(name.name(), value);
        }
    }

    public void Set(RegisterName name, Boolean value) {
        synchronized (lock) {
            _readings.putBoolean(name.name(), value);
        }
    }
}
