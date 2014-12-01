package ca.farrelltonsolar.classic;

import android.os.Bundle;

public class Readings {

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
        return _readings.getFloat(name.name(), 0);
    }

    public int GetInt(RegisterName name) {
        return _readings.getInt(name.name(), 0);
    }

    public String GetString(RegisterName name) {
        return _readings.getString(name.name());
    }


    public Boolean GetBoolean(RegisterName name) {
        return _readings.getBoolean(name.name(), false);
    }

    public void Set(RegisterName name, Float value) {
        synchronized (this) {
            _readings.putFloat(name.name(), value);
        }
    }

    public void Set(RegisterName name, int value) {
        synchronized (this) {
            _readings.putInt(name.name(), value);
        }
    }

    public void Set(RegisterName name, String value) {
        synchronized (this) {
            _readings.putString(name.name(), value);
        }
    }

    public void Set(RegisterName name, Boolean value) {
        synchronized (this) {
            _readings.putBoolean(name.name(), value);
        }
    }
}
