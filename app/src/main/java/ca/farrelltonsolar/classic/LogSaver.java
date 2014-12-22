package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class LogSaver {


    private Thread mLogger;
    static String pvOutput = "pvoutput.org";

    public void Terminate() {
        if (mLogger != null) {
            try {
                mLogger.interrupt();
                mLogger.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ResetLogs() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        String fName = settings.getString("LogDate", "");
        if (fName.length() > 0) {
            MonitorApplication.getAppContext().deleteFile(fName);
        }
        settings.edit().remove("LogDate").commit();
        settings.edit().remove("UploadDate").commit();
    }

    // Our handler for received Intents.
    private BroadcastReceiver mDayLogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogEntry logs = (LogEntry)intent.getSerializableExtra("logs");
            Date logDate = LogDate();
            if (logDate != null) {
                Calendar rightNow = Calendar.getInstance();
                int currentDayOfYear = rightNow.get(Calendar.DAY_OF_YEAR);
                Calendar logCalendar = Calendar.getInstance();
                logCalendar.setTime(LogDate());
                int logDayOfYear = logCalendar.get(Calendar.DAY_OF_YEAR);
                if (currentDayOfYear > logDayOfYear) { // last log file was created before today? update it
                    try {

                        if (logs != null) {
                            SaveLogs(logs);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                SaveLogs(logs); // never saved before!
            }
        }
    };

    private void SaveLogs(LogEntry logs) {
        String updatedLogFilename = GetFileName();
        save(logs.getLogs(), updatedLogFilename);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        String fName = settings.getString("LogDate", "");
        if (fName.length() > 0) {
            MonitorApplication.getAppContext().deleteFile(fName);
        }
        settings.edit().putString("LogDate", updatedLogFilename).commit();
    }

    private Date LogDate() {
        Date logDate = null;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        String fName = settings.getString("LogDate", "");
        if (fName.length() > 0) {
            try {
                logDate = new SimpleDateFormat("yyyy-MM-dd'.log'").parse(fName);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return logDate;
    }

    public void Start() {

        LocalBroadcastManager.getInstance(MonitorApplication.getAppContext()).registerReceiver(mDayLogReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS));
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
        boolean doUpload = settings.getBoolean(Constants.UploadToPVOutput, false);
        if (doUpload) {
            mLogger = new Thread(new Runnable() {
                public void run() {
                    boolean running = true;
                    int sleepTime = 10000;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MonitorApplication.getAppContext());
                    String uploadDateString = settings.getString("UploadDate", "");
                    String APIKey = settings.getString(Constants.APIKey, "");
                    String SID = settings.getString(Constants.SID, "");
//                    String APIKey = "9fd61eb03a68c88573d0a081ec3e30e16fcbc121";
//                    String SID = "28034";
                    if (APIKey.length() > 0 && SID.length() > 0) {
                        try {
                            do {
                                String fName = settings.getString("LogDate", "");
                                if (fName.length() > 0) {
                                    Date logDate = new SimpleDateFormat("yyyy-MM-dd'.log'").parse(fName);
                                    int numberOfDays = Constants.PVOUTPUT_RECORD_LIMIT;
                                    if (uploadDateString.length() > 0) {
                                        Date uploadDate = new SimpleDateFormat("yyyy-MM-dd").parse(uploadDateString);
                                        numberOfDays = getDifferenceDays(uploadDate, logDate);
                                    }
                                    numberOfDays = numberOfDays > Constants.PVOUTPUT_RECORD_LIMIT ? Constants.PVOUTPUT_RECORD_LIMIT : numberOfDays; // limit to 20 days as per pvOutput limits
                                    if (numberOfDays > 0) {
                                        Calendar calendar = Calendar.getInstance();
                                        String UploadDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                                        calendar.setTime(logDate);
                                        Bundle logs = load(fName);
                                        short[] mData = logs.getShortArray("0"); // kWh/day
                                        boolean uploadDateRecorded = false;
                                        for (int i = 0; i < numberOfDays; i++) {
                                            Socket pvOutputSocket = Connect(pvOutput);
                                            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(pvOutputSocket.getOutputStream()));
                                            String dateStamp = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
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
                                                settings.edit().putString("UploadDate", UploadDate).commit(); // update UploadDate
                                                uploadDateRecorded = true;
                                            }
                                            Thread.sleep(Constants.PVOUTPUT_RATE_LIMIT); // rate limit
                                            calendar.roll(Calendar.DATE, false);
                                        }
                                        break;
                                    }
                                }
                                Thread.sleep(sleepTime);
                            } while (running);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            mLogger.start();
        }
    }


    private Socket Connect(String hostname) throws InterruptedException {
        final Socket mySocket = new Socket();
        do {
            try {
                InetAddress ipaddress = InetAddress.getByName(hostname);
                SocketAddress address = new InetSocketAddress(ipaddress, 80);

                mySocket.connect(address, 3500);
            } catch (IOException e) {
                e.printStackTrace();
                Thread.sleep(2 * 60000);
            }
        }
        while (mySocket.isConnected() == false);
        return mySocket;
    }

    private int getDifferenceDays(Date d1, Date d2) {
        int daysdiff = 0;
        long diff = d2.getTime() - d1.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        daysdiff = (int) diffDays;
        return daysdiff;
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
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            parcel.recycle();
        }
        return rval;
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
        } catch (IOException e) {
            e.printStackTrace();
            bundle = null;
        } finally {
            parcel.recycle();
        }
        return bundle;
    }

    private void RequestLogs() {
        Calendar cal = new GregorianCalendar();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        cal.add(Calendar.MONTH, -1);
        cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private String GetFileName() {
        Calendar cal = new GregorianCalendar();
        String file = new SimpleDateFormat("yyyy-MM-dd'.log'").format(cal.getTime());
        return file;
    }

    private static final int kBufferExpansionSize = 1024;

    public void save(final Bundle bundle, String file) {
        try {
            FileOutputStream fOut = MonitorApplication.getAppContext().openFileOutput(file, Context.MODE_PRIVATE);
            fOut.write(serializeBundle(bundle));
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bundle load(String file) {
        Bundle rval = null;
        try {
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
            rval = deserializeBundle(array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rval;
    }

}
   
