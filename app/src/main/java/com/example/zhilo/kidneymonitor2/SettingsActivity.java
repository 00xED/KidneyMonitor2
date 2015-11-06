package com.example.zhilo.kidneymonitor2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    //Initialisation of LogWriter
    private static final String logTag = "SettingsActivity";
    LogWriter lw = new LogWriter();

    private Button btStopService;

    private TextView tvCurrentDeviceName, tvCurrentDeviceAddress;
    private Switch swForegroundService, swVibrate, swSound, swTestMode, swAutoconnect;

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Load preferences

        tvCurrentDeviceName = (TextView) findViewById(R.id.tv_ValueCurrentDeviceName);
        tvCurrentDeviceName.setText(sPref.getString(Constants.SETTINGS_NAME,
                "none"));

        tvCurrentDeviceAddress = (TextView) findViewById(R.id.tv_ValueCurrentDeviceAddress);
        tvCurrentDeviceAddress.setText(sPref.getString(Constants.SETTINGS_ADDRESS,
                "efault_address"));

        /**
         * Setting checkboxes states
         */
        swForegroundService = (Switch) findViewById(R.id.sw_ForegroundService);
        if (sPref.getBoolean(Constants.SETTINGS_FOREGROUND, false))
            swForegroundService.setChecked(true);
        else
            swForegroundService.setChecked(false);


        swVibrate = (Switch) findViewById(R.id.sw_Vibration);
        if (sPref.getBoolean(Constants.SETTINGS_VIBRATION, false))
            swVibrate.setChecked(true);
        else
            swVibrate.setChecked(false);

        swSound = (Switch) findViewById(R.id.sw_Sound);
        if (sPref.getBoolean(Constants.SETTINGS_SOUND, false))
            swSound.setChecked(true);
        else
            swSound.setChecked(false);

        swTestMode = (Switch) findViewById(R.id.sw_TestMode);
        if (sPref.getBoolean(Constants.SETTINGS_TESTMODE, false))
            swTestMode.setChecked(true);
        else
            swTestMode.setChecked(false);

        swAutoconnect = (Switch) findViewById(R.id.sw_Autoconnect);
        if (sPref.getBoolean(Constants.SETTINGS_AUTOCONNECT, false))
            swAutoconnect.setChecked(true);
        else
            swAutoconnect.setChecked(false);

        btStopService = (Button) findViewById(R.id.bt_StopService);
        if (ConnectionService.isServiceRunning)
            btStopService.setText(
                    "title_service_stop");
        else
            btStopService.setText(
                    "title_service_start");
    }

    public void OnClick(View v) {
        sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Loading preferences;
        SharedPreferences.Editor ed = sPref.edit(); //Setting for preference editing
        switch (v.getId()) {
            case R.id.bt_Scan://Start device choosing activity DeviceListActivity
            {
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(intent, Constants.CHOOSE_DEVICE);
                break;
            }

            case R.id.bt_SetDefaults://Reset all preferences
            {
                ed.clear();
                ed.apply();
                tvCurrentDeviceName.setText("value_current_device_none");
                tvCurrentDeviceAddress.setText("value_current_device_default_address");
                Toast.makeText(this,
                        "title_prefs_cleared",
                        Toast.LENGTH_SHORT).show();
                lw.appendLog(logTag, "Deleting settings and logs");
                File logFile = new File(Environment.getExternalStorageDirectory(), Constants.logFile);
                if (logFile.exists()) if (logFile.delete()) {
                    lw.appendLog(logTag, "Log file deleted");
                } else {
                    lw.appendLog(logTag, "Log file NOT deleted");
                }

                File verboseLogFile = new File(Environment.getExternalStorageDirectory(), Constants.logFileDebug);
                if (verboseLogFile.exists()) if (verboseLogFile.delete()) {
                    lw.appendLog(logTag, "Debug log file deleted");
                } else {
                    lw.appendLog(logTag, "Debug log file NOT deleted");
                }

                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }

            case R.id.sw_ForegroundService://Save setting for foreground service
            {
                if (swForegroundService.isChecked())
                    ed.putBoolean(Constants.SETTINGS_FOREGROUND, true);
                else
                    ed.putBoolean(Constants.SETTINGS_FOREGROUND, false);
                ed.apply();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("notif_restart")
                        .setCancelable(false)
                        .setNegativeButton("ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            }

            case R.id.sw_Vibration://Save setting for vibration on notification
            {
                if (swVibrate.isChecked())
                    ed.putBoolean(Constants.SETTINGS_VIBRATION, true);
                else
                    ed.putBoolean(Constants.SETTINGS_VIBRATION, false);
                ed.apply();
                break;
            }

            case R.id.sw_Sound://Save setting for sound on notification
            {
                if (swSound.isChecked())
                    ed.putBoolean(Constants.SETTINGS_SOUND, true);
                else
                    ed.putBoolean(Constants.SETTINGS_SOUND, false);
                ed.apply();
                break;
            }

            case R.id.sw_TestMode: {
                if (swTestMode.isChecked())
                    ed.putBoolean(Constants.SETTINGS_TESTMODE, true);
                else
                    ed.putBoolean(Constants.SETTINGS_TESTMODE, false);
                ed.apply();
                break;
            }

            case R.id.sw_Autoconnect: {
                if (swAutoconnect.isChecked())
                    ed.putBoolean(Constants.SETTINGS_AUTOCONNECT, true);
                else
                    ed.putBoolean(Constants.SETTINGS_AUTOCONNECT, false);
                ed.apply();
                break;
            }

            case R.id.bt_StopService://Start or stop service
            {
                if (ConnectionService.isServiceRunning) {
                    lw.appendLog(logTag, "Starting service");
                    stopService(new Intent(this, ConnectionService.class));
                    btStopService.setText(
                            "title_service_start");
                } else {
                    lw.appendLog(logTag, "Stopping service");
                    startService(new Intent(this, ConnectionService.class));
                    //ConnectionService.isServiceRunning=true;
                    btStopService.setText(
                            "title_service_stop");
                }
                break;
            }

            default:
                break;
        }
    }

    /**
     * Handling result of DeviceListActivity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences.Editor ed = sPref.edit(); //Setting for preference editing
        if (requestCode == Constants.CHOOSE_DEVICE) {//If DeviceListActivity responsed
            if (resultCode == RESULT_OK) {//And user has choosen device to connect with
                //Getting chosen device data from DeviceListActivity
                String name = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_NAME);
                String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                //Setting values for textviews
                tvCurrentDeviceName.setText(name);
                tvCurrentDeviceAddress.setText(address);

                //Saving values
                ed.putString(Constants.SETTINGS_NAME, name);
                ed.putString(Constants.SETTINGS_ADDRESS, address);
                ed.apply();
                Toast.makeText(this,
                        "title_prefs_saved",
                        Toast.LENGTH_SHORT).show();

                lw.appendLog(logTag, "Bluetooth device chosen:" + name + "@" + address);

                Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_BLE_SERVICE);
                sendBroadcast(intentValues);
            } else {
                lw.appendLog(logTag, "Bluetooth device NOT CHOSEN");
            }
        }
    }

}
