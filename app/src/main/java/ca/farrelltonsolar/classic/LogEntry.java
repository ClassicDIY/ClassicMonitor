package ca.farrelltonsolar.classic;

import android.os.Bundle;

public class LogEntry {

    public LogEntry() {
        _logs = new Bundle();
    }

    public LogEntry(Bundle readings) {
        _logs = readings;
    }

    private Bundle _logs;
    public Bundle GetLogs() {
        return _logs;
    }

    public void Set(int category, short[] data) {
        _logs.putShortArray(String.valueOf(category), data);
        return;
    }

    public short[] Get(int category) {
        return _logs.getShortArray(String.valueOf(category));
    }


}
