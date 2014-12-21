package ca.farrelltonsolar.classic;

import android.os.Bundle;

import java.util.GregorianCalendar;

public class LogEntry {
    private Bundle _logs;
    GregorianCalendar logDate;
    int  logInterval;

    public LogEntry() {
        _logs = new Bundle();
    }

    public void setLogDate(GregorianCalendar logDate) {
        this.logDate = logDate;
        _logs.putSerializable("LogDate", logDate);
    }

    public GregorianCalendar getLogDate() {
        return logDate;
    }

    public Bundle get_logs() {
        return _logs;
    }

    public Bundle GetLogs() {
        return _logs;
    }

    public void Set(int category, short[] data) {
        _logs.putShortArray(String.valueOf(category), data);
        return;
    }

    public void Set(String category, short[] data) {
        _logs.putShortArray(category, data);
        return;
    }

    public void Set(String category, float[] data) {
        _logs.putFloatArray(category, data);
        return;
    }

    public short[] Get(int category) {
        return _logs.getShortArray(String.valueOf(category));
    }


}
