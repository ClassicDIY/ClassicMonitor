package ca.farrelltonsolar.classic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.io.Serializable;
import java.util.GregorianCalendar;

public class LogEntry implements Serializable {
    private Bundle logs;
    GregorianCalendar logDate;

    public LogEntry() {
        logs = new Bundle();
    }

    public void setLogDate(GregorianCalendar logDate) {
        this.logDate = logDate;
        logs.putSerializable("LogDate", logDate);
    }

    public GregorianCalendar getLogDate() {
        return logDate;
    }

    public boolean isEmpty() {
        return logs.isEmpty();
    }

    public Bundle getLogs() {
        return logs;
    }

    public void set(int category, short[] data) {
        logs.putShortArray(String.valueOf(category), data);
        return;
    }

    public void set(int category, float[] data) {
        logs.putFloatArray(String.valueOf(category), data);
        return;
    }

    public void set(String category, short[] data) {
        logs.putShortArray(category, data);
        return;
    }

    public void set(String category, float[] data) {
        logs.putFloatArray(category, data);
        return;
    }

    public short[] getShortArray(int category) {
        return logs.getShortArray(String.valueOf(category));
    }

    public float[] getFloatArray(int category) {
        return logs.getFloatArray(String.valueOf(category));
    }

    public void broadcastLogs(Context context, String action) {
        if (!logs.isEmpty()) {
            Intent intent = new Intent(action);
            intent.putExtra("logs", this);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
