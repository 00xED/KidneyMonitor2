package com.example.zhilo.kidneymonitor2;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogActivity extends AppCompatActivity {

    private TextView mTvLog;
    private CheckBox mCbAutoscroll;

    private Handler scrollDownHandler = new Handler();//scroll down
    private Handler textRefreshHandler = new Handler();//scroll down
    textUpdateTask textupdatetask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mTvLog = (TextView) findViewById(R.id.tv_Log);
        mCbAutoscroll = (CheckBox) findViewById(R.id.cb_Autoscroll);
        mCbAutoscroll.setChecked(true);
        scrollDownHandler.post(scrollDownTask);
        textRefreshHandler.post(textUpdateTask);
    }

    /**
     * Checks if we can read from internal storage
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.bt_ClearLog:
            {
                File logFile = new File(Environment.getExternalStorageDirectory(), Constants.logFile);
                if (logFile.exists()) if (logFile.delete()) {
                    mTvLog.setText("Log file deleted");
                } else {
                    mTvLog.append("Log file NOT deleted");
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Handler that updates textview every second
     */
    Runnable textUpdateTask = new Runnable() {
        @Override
        public void run() {
            textupdatetask = new textUpdateTask();
            textupdatetask.execute();
            textRefreshHandler.postDelayed(textUpdateTask, 1000);//refresh after one second
        }
    };

    /**
     * Handler that updates textview every second
     */
    Runnable scrollDownTask = new Runnable() {
        @Override
        public void run() {
            final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));
            if (mCbAutoscroll.isChecked()) scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            scrollDownHandler.postDelayed(scrollDownTask, 100);//refresh after one second
        }
    };

    class textUpdateTask extends AsyncTask<Void, Void, Void> {

        StringBuilder text;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            text = readLog();
            if(text.length()>20000)
                text = text.delete(0, text.length()-20000);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mTvLog.setText(text);
        }

        /**
         * Reads log file kidneymonitor.log and updates textview
         */
        public StringBuilder readLog() {

                //Get the text file
                File file = new File(Environment.getExternalStorageDirectory(), "kidneymonitor/kidneymonitor.log");

            if (!file.exists()) {
                try {
                    if(!file.createNewFile())
                        Log.d("LogWriter", "can't create new file");
                } catch (IOException e) {
                    Log.d("LogWriter", e.toString());
                    e.printStackTrace();
                }
            }

            //Read text from file
            StringBuilder text = new StringBuilder();
            if (isExternalStorageReadable()) {

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        //mTvLog.append(line);
                        text.append('\n');
                        //mTvLog.append("\n");
                    }

                    br.close();
                } catch (IOException e) {
                    Log.e("LOGACTIVITY", e.toString());
                }
            }
            return text;
            //mTvLog.append(text);
        }

    }
}
