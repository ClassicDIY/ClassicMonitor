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

import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

/**
 * Created by Graham on 22/12/2014.
 */
public class PVOutputUploader extends TimerTask {
    private static final int kBufferExpansionSize = 1024;
    static String pvOutput = "pvoutput.org";
    String APIKey;

    public PVOutputUploader(String APIKey) {
        this.APIKey = APIKey;
    }

    @Override
    public void run() {
        try {
            int numberOfChargeControllers = MonitorApplication.chargeControllers().count();
            for (int index = 0; index < numberOfChargeControllers; index++) {
                ChargeController cc = MonitorApplication.chargeControllers().get(index);
                if (cc.uploadToPVOutput()) {
                    Log.w(getClass().getName(), String.format("PVOutput uploading to %s on thread: %s", cc,  Thread.currentThread().getName()));
                    doUpload(cc);
                }
            }
        } catch (Exception ex) {
            Log.w(getClass().getName(), String.format("PVOutput upload failed ex: %s, thread: %s", ex, Thread.currentThread().getName()));
        }
    }

    private boolean doUpload(ChargeController controller) throws InterruptedException, IOException {
        String uploadDateString = controller.uploadDate();
        String SID = controller.getSID();
        String fName = controller.getPVOutputLogFilename();
        if (fName != null && fName.length() > 0 && SID != null && SID.length() > 0) {
            DateTime logDate = PVOutputService.LogDate();
            int numberOfDays = Constants.PVOUTPUT_RECORD_LIMIT;
            if (uploadDateString.length() > 0) {
                DateTime uploadDate = DateTime.parse(uploadDateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
                numberOfDays = Days.daysBetween(uploadDate, logDate).getDays();
            }
            numberOfDays = numberOfDays > Constants.PVOUTPUT_RECORD_LIMIT ? Constants.PVOUTPUT_RECORD_LIMIT : numberOfDays; // limit to 20 days as per pvOutput limits
            if (numberOfDays > 0) {
                Log.d(getClass().getName(), String.format("PVOutput uploading: %s for %d days on thread: %s", fName, numberOfDays, Thread.currentThread().getName()));
                DateTime now = DateTime.now();
                String UploadDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(now);
                Bundle logs = load(fName);
                float[] mData = logs.getFloatArray(String.valueOf(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY)); // kWh/day
                boolean uploadDateRecorded = false;
                for (int i = 0; i < numberOfDays; i++) {
                    Socket pvOutputSocket = Connect(pvOutput);
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(pvOutputSocket.getOutputStream()));
                    String dateStamp = DateTimeFormat.forPattern("yyyyMMdd").print(logDate);
                    StringBuilder feed = new StringBuilder("GET /service/r2/addoutput.jsp");
                    feed.append("?key=");
                    feed.append(APIKey);
                    feed.append("&sid=");
                    feed.append(SID);
                    feed.append("&d=");
                    feed.append(dateStamp);
                    feed.append("&g=");
                    String wh = String.valueOf(mData[i] * 100);
                    feed.append(wh);
                    feed.append("\r\n");
                    feed.append("Host: ");
                    feed.append(pvOutput);
                    feed.append("\r\n");
                    feed.append("\r\n");
                    String resp = feed.toString();
                    outputStream.writeBytes(resp);
                    outputStream.flush();
                    pvOutputSocket.close();
                    if (uploadDateRecorded == false) {
                        controller.setUploadDate(UploadDate);
                        uploadDateRecorded = true;
                    }
                    Thread.sleep(Constants.PVOUTPUT_RATE_LIMIT); // rate limit
                    logDate = logDate.minusDays(1);
                }
                return true;
            }
        }
        return false;
    }

    private Socket Connect(String hostname) throws InterruptedException {
        final Socket mySocket = new Socket();
        do {
            try {
                InetAddress ipaddress = InetAddress.getByName(hostname);
                SocketAddress address = new InetSocketAddress(ipaddress, 80);
                mySocket.connect(address, 3500);
            } catch (IOException ex) {
                Log.w(getClass().getName(), String.format("PVOutput trying to connect to %s, failed ex: %s", hostname,  ex));
                Thread.sleep(2 * 60000);
            }
        }
        while (mySocket.isConnected() == false);
        return mySocket;
    }

    private Bundle deserializeBundle(byte[] data) {
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(data));
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException ex) {
            Log.w(getClass().getName(), String.format("deserializeBundle failed ex: %s", ex));
            bundle = null;
        } finally {
            parcel.recycle();
        }
        return bundle;
    }

    public Bundle load(String file) throws IOException {
        byte[] array = new byte[kBufferExpansionSize];
        int bytesRead = 0;
        int totalLength = 0;
        InputStream fin = MonitorApplication.getAppContext().openFileInput(file);
        //InputStream fin = new BufferedInputStream(new FileInputStream(file));
        while ((bytesRead = fin.read(array, totalLength, array.length - totalLength)) != -1) {
            totalLength += bytesRead;
//                if (totalLength == array.length)
//                    array = Arrays.copyOf(array, array.length + kBufferExpansionSize);
        }
        return deserializeBundle(array);

    }
}