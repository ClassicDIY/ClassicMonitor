/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.zip.GZIPOutputStream;

public class PVOutputService extends IntentService {

    PVOutputUploader uploader;
    private Timer pollTimer;

    public PVOutputService() {
        super("PVOutputService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            handleActionPVOutputUpload();
        }
    }

    // Our handler for received Intents.
    private BroadcastReceiver mDayLogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LogEntry logs = (LogEntry) intent.getSerializableExtra("logs");
                DateTime logDate = LogDate();
                if (logDate != null) {
                    DateTime rightNow = DateTime.now().withTimeAtStartOfDay();
                    int daysSinceLastLog = Days.daysBetween(logDate, rightNow).getDays();
                    if (daysSinceLastLog > 0) { // last log file was created before today? update it
                        saveLogs(logs);
                    }
                } else {
                    saveLogs(logs); // never saved before!
                }
            } catch (Exception ex) {
                Log.w(getClass().getName(), String.format("SaveLogs failed ex: %s", ex));
            }
        }

        private void saveLogs(LogEntry logs) {



            float[] highWatts = logs.getFloatArray(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY);
            Bundle toSave = new Bundle();
            toSave.putFloatArray(String.valueOf(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY), highWatts);

            ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
            if (cc != null) {
                cc.resetPVOutputLogs();
                cc.setPVOutputLogFilename(getLogDate());
                save(toSave, cc.getPVOutputLogFilename());
                Log.d(getClass().getName(), String.format("PVOutput save logs for upload for %s starting on thread: %s", cc.getPVOutputLogFilename(), Thread.currentThread().getName()));
            }
        }
    };

    public static DateTime LogDate() {
        DateTime logDate = null;
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null) {
            String fName = cc.getPVOutputLogFilename();
            if (fName != null && fName.length() > 0) {
                try {
                    String logDateSubstring = fName.substring(18, 28);
                    logDate = DateTime.parse(logDateSubstring, DateTimeFormat.forPattern("yyyy-MM-dd"));
                } catch (Exception ex) {
                    Log.w("PVOutputService", String.format("LogDate parse filename failed ex: %s", ex));
                }
            }
        }

        return logDate;
    }

    /**
     * Try to upload logs to PVOutput.
     */
    private void handleActionPVOutputUpload() {
        if (MonitorApplication.chargeControllers().getCurrentChargeController().uploadToPVOutput()) {
            LocalBroadcastManager.getInstance(PVOutputService.this).registerReceiver(mDayLogReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
            String APIKey = MonitorApplication.chargeControllers().aPIKey();
            if (APIKey.length() > 0) {
                pollTimer = new Timer();
                uploader = new PVOutputUploader(APIKey);
                pollTimer.schedule(uploader, 2000, 10000);
            }
        }
    }

    private String getLogDate() {
        DateTime today = DateTime.now().withTimeAtStartOfDay();
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(today);
    }

    public void save(final Bundle bundle, String file) {
        try {
            FileOutputStream fOut = MonitorApplication.getAppContext().openFileOutput(file, Context.MODE_PRIVATE);
            fOut.write(serializeBundle(bundle));
            fOut.close();
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("save failed ex: %s", ex));
        }
    }

    private byte[] serializeBundle(final Bundle bundle) {
        byte[] rval = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            rval = bos.toByteArray();
        } catch (IOException ex) {
            Log.w(getClass().getName(), String.format("serializeBundle failed ex: %s", ex));

        } finally {
            parcel.recycle();
        }
        return rval;
    }
}
