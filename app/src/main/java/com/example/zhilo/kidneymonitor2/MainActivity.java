package com.example.zhilo.kidneymonitor2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    //Initialisation of LogWriter
    private static final String logTag = "MainActivity";
    private LogWriter lw = new LogWriter();

    private BluetoothAdapter mBluetoothAdapter;

    private TextView tvStatus, tvProcedure, tvParams, tvSorbtime, tvBatt, tvLastConnected;
    private TextView tvCaptionStatus, tvCaptionProcedure, tvCaptionParams, tvCaptionSorbentTime, tvCaptionBattery;
    private ImageView ivStatus, ivProcedure, ivParams, ivBatt, ivSorbtime;
    private Button btPause, btProcedure, btLog, btSettings, btMessage;

    //Handler for automatic refreshing of screen values
    Handler RefreshHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lw.appendLog(logTag, "Start");
        lw.appendLog(logTag, "MainActivity opened");

        /**
         * Initialising TextViews for main screen
         */
        Typeface tfPlayBold = Typeface.createFromAsset(getAssets(), "fonts/Play-Bold.ttf");
        tvStatus = (TextView) findViewById(R.id.tv_StatusValue);
        tvStatus.setTypeface(tfPlayBold);
        tvProcedure = (TextView) findViewById(R.id.tv_ProcedureValue);
        tvProcedure.setTypeface(tfPlayBold);
        tvParams = (TextView) findViewById(R.id.tv_ParamsValue);
        tvParams.setTypeface(tfPlayBold);
        tvSorbtime = (TextView) findViewById(R.id.tv_SorbentTimeValue);
        tvSorbtime.setTypeface(tfPlayBold);
        tvBatt = (TextView) findViewById(R.id.tv_BatteryValue);
        tvBatt.setTypeface(tfPlayBold);
        tvLastConnected = (TextView) findViewById(R.id.tv_LastConnected);
        tvLastConnected.setTypeface(tfPlayBold);

        ivBatt = (ImageView) findViewById(R.id.iv_Battery);
        ivStatus = (ImageView) findViewById(R.id.iv_Status);
        ivProcedure = (ImageView) findViewById(R.id.iv_Procedure);
        ivParams = (ImageView) findViewById(R.id.iv_Params);
        ivSorbtime = (ImageView) findViewById(R.id.iv_SorbentTime);
        btPause = (Button) findViewById(R.id.bt_Pause);
        btPause.setTypeface(tfPlayBold);
        btProcedure = (Button) findViewById(R.id.bt_Procedure);
        btProcedure.setTypeface(tfPlayBold);
        btLog = (Button) findViewById(R.id.bt_Log);
        btLog.setTypeface(tfPlayBold);
        btSettings = (Button) findViewById(R.id.bt_Settings);
        btSettings.setTypeface(tfPlayBold);
        btMessage = (Button) findViewById(R.id.bt_Message);
        btMessage.setTypeface(tfPlayBold);

        tvCaptionStatus = (TextView) findViewById(R.id.tv_Status);
        tvCaptionStatus.setTypeface(tfPlayBold);
        tvCaptionProcedure = (TextView) findViewById(R.id.tv_Procedure);
        tvCaptionProcedure.setTypeface(tfPlayBold);
        tvCaptionParams = (TextView) findViewById(R.id.tv_Params);
        tvCaptionParams.setTypeface(tfPlayBold);
        tvCaptionSorbentTime = (TextView) findViewById(R.id.tv_SorbentTime);
        tvCaptionSorbentTime.setTypeface(tfPlayBold);
        tvCaptionBattery = (TextView) findViewById(R.id.tv_Battery);
        tvCaptionBattery.setTypeface(tfPlayBold);

        RefreshHandler.post(RefreshTask);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            lw.appendLog(logTag, "BLE not supported, closing app");
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            lw.appendLog(logTag, "Bluetooth not supported, closing app");
            finish();
            return;
        }

        /**
         * Load preferences; If saved device address is default - open preferences to find device
         */

        SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Load preferences
        if (!sPref.contains(Constants.SETTINGS_ADDRESS)) { //If address is default start PrefActivity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            Toast.makeText(this,
                    getResources().getText(R.string.prefs_new).toString(),
                            Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this,
                getResources().getText(R.string.prefs_loaded).toString(),
                Toast.LENGTH_SHORT).show();

        //If service is not running - start it
        if (!ConnectionService.isServiceRunning)
            startService(new Intent(this, ConnectionService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Constants.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void OnClick(View v) {

        switch (v.getId()) {
            case R.id.bt_Settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.bt_Log: {
                Intent intent = new Intent(this, LogActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_ParamsDropdown:{
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_Params:{
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_Params:{
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_ParamsValue:{
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            default:
                break;
        }
    }

    Runnable RefreshTask = new Runnable() {
        @Override
        public void run() {
            int currentStatus = ProcedureSettings.getInstance().getStatus();
            int currentProcedure = ProcedureSettings.getInstance().getProcedure();
            int currentParams = ProcedureSettings.getInstance().getParams();
            int currentBattery = ProcedureSettings.getInstance().getBattery();
            long currentLastConnected = ProcedureSettings.getInstance().getLast_connection();
            //current state
            switch (currentStatus) {
                case Constants.STATUS_ON: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_on).toString());
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.STATUS_OFF: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_off).toString());
                    ivStatus.setImageResource(R.drawable.ic_danger);
                    break;
                }

                default: {
                    tvStatus.
                            setText(getResources().getText(R.string.unknown).toString());
                    ivStatus.setImageResource(R.drawable.ic_question_mark);
                    break;
                }
            }

            //current procedure
            switch (currentProcedure) {
                case Constants.PROCEDURE_FILLING: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_filling).toString());
                    ivProcedure.setImageResource(R.drawable.ic_fill);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                    break;
                }

                case Constants.PROCEDURE_DIALYSIS: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_dialysis).toString());
                    ivProcedure.setImageResource(R.drawable.ic_dialysis);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                    break;
                }

                case Constants.PROCEDURE_SHUTDOWN: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_shutdown).toString());
                    ivProcedure.setImageResource(R.drawable.ic_shutdown);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                    break;
                }

                case Constants.PROCEDURE_DISINFECTION: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_disinfection).toString());
                    ivProcedure.setImageResource(R.drawable.ic_disinfection);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                    break;
                }

                case Constants.PROCEDURE_READY: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_ready).toString());
                    ivProcedure.setImageResource(R.drawable.ic_tick);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_resume));
                    break;
                }

                case Constants.PROCEDURE_FLUSH: {
                    tvProcedure.
                            setText(getResources().getText(R.string.procedure_flush).toString());

                    ivProcedure.setImageResource(R.drawable.ic_flush);
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                    break;
                }

                default: {
                    tvProcedure.
                            setText(getResources().getText(R.string.unknown).toString());
                    ivProcedure.setImageResource(R.drawable.ic_question_mark);
                    break;
                }
            }

            //procedure parameters
            switch (currentParams) {
                case Constants.PARAMS_NORMAL: {
                    tvParams.
                            setText(getResources().getText(R.string.procedure_params_normal).toString());
                    ivParams.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.PARAMS_DANGER: {
                    tvParams.
                            setText(getResources().getText(R.string.procedure_params_danger).toString());
                    ivParams.setImageResource(R.drawable.ic_danger);
                    break;
                }

                default: {
                    tvParams.
                            setText(getResources().getText(R.string.unknown).toString());
                    ivParams.setImageResource(R.drawable.ic_question_mark);
                    break;
                }
            }


            //sorbtime
            {
                SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Loading preferences
                /*long remaining_time = sPref.getLong(Constants.TIME_REMAINING, -1);

                if (remaining_time == -1 || !ConnectionService.STATE.equals(ConnectionService.STATE_ON))//If received value is default then set to unknown
                {
                    tvSorbtime.setText(getResources().getText(R.string.value_time_sorbent_unknown).toString());
                    ivSorbtime.setImageResource(R.drawable.ic_time_grey);
                } else    //Convert received time in seconds to hours and minutes
                {
                    int hours = (int) TimeUnit.MILLISECONDS.toHours(remaining_time);
                    remaining_time -= TimeUnit.HOURS.toMillis(hours);
                    int mins = (int) TimeUnit.MILLISECONDS.toMinutes(remaining_time);
                    remaining_time -= TimeUnit.MINUTES.toMillis(mins);
                    int sec = (int) TimeUnit.MILLISECONDS.toSeconds(remaining_time);
                    tvSorbtime.setText(hours +
                            getResources().getText(R.string.value_sorbtime_hours).toString() +
                            mins +
                            getResources().getText(R.string.value_sorbtime_mins).toString() + sec);

                    ivSorbtime.setImageResource(R.drawable.ic_time_green);
                }*/
            }

            //Battery value
            {
                if (currentBattery == -1)//If received value is default then set battery to unknown
                {
                    tvBatt.setText(getResources().getText(R.string.unknown).toString());
                    ivBatt.setImageResource(R.drawable.ic_question_mark);
                } else {//Otherwise set value and image
                    tvBatt.setText(currentBattery + "%");
                    if (currentBattery >= 75)
                        ivBatt.setImageResource(R.drawable.ic_battery_100);
                    else if (currentBattery >= 50)
                        ivBatt.setImageResource(R.drawable.ic_battery_75);
                    else if (currentBattery >= 25)
                        ivBatt.setImageResource(R.drawable.ic_battery_50);
                    else if (currentBattery >= 10)
                        ivBatt.setImageResource(R.drawable.ic_battery_25);
                    else ivBatt.setImageResource(R.drawable.ic_battery_empty);
                }
            }

            //Last time device connected
            {
                if (currentLastConnected != -1) {
                    tvLastConnected.setPaintFlags(tvLastConnected.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm");
                    Date resultDate = new Date(currentLastConnected);
                    String strDate = sdf.format(resultDate);
                    tvLastConnected.setText(getResources().getText(R.string.last_connected).toString() + strDate);
                }
            }


            RefreshHandler.postDelayed(RefreshTask, Constants.PARAM_REFRESH_INTERVAL_MS);//refresh after RECONNECT_INTERVAL_MS milliseconds
        }
    };

}
