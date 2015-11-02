package com.example.zhilo.kidneymonitor2;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Writes logs to kidneymoonitor.log and to Log.d
 */

public class LogWriter extends Application {

    public void OnCreate() {
        super.onCreate();
    }

    /**
     * Write to debug log file only.
     *
     * @param tag Message tag
     * @param msg Message text
     */
    public void appendLog(String tag, String msg) {

        /**
         * Initialising calendar for writing timestamp
         */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        tag = strDate + "@" + tag;
        Log.d(tag, msg);
        msg = tag + "->" + msg;

        File logFile = new File(Environment.getExternalStorageDirectory(), Constants.logFileDebug);

        final File dir = new File(Environment.getExternalStorageDirectory() + Constants.fileDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs())
                Log.d("LogWriter", "can't create new dir");
        }

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile())
                    Log.d("LogWriter", "can't create new file");
            } catch (IOException e) {
                Log.d("LogWriter", e.toString());
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(msg);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.d("LogWriter", e.toString());
            e.printStackTrace();
        }

    }

    /**
     * Write to debug AND user log files.
     *
     * @param tag       Message tag
     * @param msg       Message tag
     * @param noVerbose True if want to write. False if want to write/
     */
    public void appendLog(String tag, String msg, boolean noVerbose) {

        /**
         * Initialising calendar for writing timestamp
         */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String strDate = sdf.format(c.getTime());

        tag = strDate + "@" + tag;
        Log.d(tag, msg);
        msg = tag + "->" + msg;

        File verboseLogFile = new File(Environment.getExternalStorageDirectory(), Constants.logFileDebug);

        if (!verboseLogFile.exists()) {
            try {
                if (!verboseLogFile.createNewFile())
                    Log.d("LogWriter", "can't create new file");
            } catch (IOException e) {
                Log.d("LogWriter", e.toString());
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(verboseLogFile, true));
            buf.append(msg);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.d("LogWriter", e.toString());
            e.printStackTrace();
        }

        File logFile = new File(Environment.getExternalStorageDirectory(), Constants.logFile);

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile())
                    Log.d("LogWriter", "can't create new file");
            } catch (IOException e) {
                Log.d("LogWriter", e.toString());
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(msg);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.d("LogWriter", e.toString());
            e.printStackTrace();
        }

    }

}