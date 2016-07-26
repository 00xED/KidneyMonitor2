package com.example.zhilo.kidneymonitor2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Initialisation of LogWriter
    private static final String logTag = "MainActivity";
    private LogWriter lw = new LogWriter();

    private BluetoothAdapter mBluetoothAdapter;

    private TextView tvStatus, tvProcedure, tvDisinfection, tvSorbtime, tvBatt, tvLastConnected;
    private TextView tvCaptionStatus, tvCaptionProcedure, tvCaptionDisinfection, tvCaptionSorbentTime, tvCaptionBattery;
    private ImageView ivStatus, ivProcedure, ivDisinfection, ivBatt, ivSorbtime, ivProcedureDropdown;
    private Button btPause, btProcedure, btLog, btSettings, btMessage, btTest;

    //Handler for automatic refreshing of screen values
    Handler RefreshHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        lw.appendLog(logTag, "Start");
        lw.appendLog(logTag, "MainActivity opened");

        /**
         * Initialising TextViews for main screen
         */
        Typeface tfPlayBold = Typeface.createFromAsset(getAssets(), "fonts/Play-Bold.ttf");
        tvStatus = (TextView) findViewById(R.id.tv_AccumValue);
        tvStatus.setTypeface(tfPlayBold);
        tvProcedure = (TextView) findViewById(R.id.tv_ProcedureValue);
        tvProcedure.setTypeface(tfPlayBold);
        tvDisinfection = (TextView) findViewById(R.id.tv_DisinfectionValue);
        tvDisinfection.setTypeface(tfPlayBold);
        tvSorbtime = (TextView) findViewById(R.id.tv_SorbentTimeValue);
        tvSorbtime.setTypeface(tfPlayBold);
        tvBatt = (TextView) findViewById(R.id.tv_BatteryValue);
        tvBatt.setTypeface(tfPlayBold);
        tvLastConnected = (TextView) findViewById(R.id.tv_LastConnected);
        tvLastConnected.setPaintFlags(tvLastConnected.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvLastConnected.setTypeface(tfPlayBold);

        ivBatt = (ImageView) findViewById(R.id.iv_Battery);
        ivStatus = (ImageView) findViewById(R.id.iv_Accum);
        ivProcedure = (ImageView) findViewById(R.id.iv_Procedure);
        ivProcedureDropdown = (ImageView) findViewById(R.id.iv_ProcedureDropdown);
        ivDisinfection = (ImageView) findViewById(R.id.iv_Disinfection);
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

        btTest = (Button) findViewById(R.id.bt_Test);

        tvCaptionStatus = (TextView) findViewById(R.id.tv_Accum);
        tvCaptionStatus.setTypeface(tfPlayBold);
        tvCaptionProcedure = (TextView) findViewById(R.id.tv_Procedure);
        tvCaptionProcedure.setTypeface(tfPlayBold);
        tvCaptionDisinfection = (TextView) findViewById(R.id.tv_Disinfection);
        tvCaptionDisinfection.setTypeface(tfPlayBold);
        tvCaptionSorbentTime = (TextView) findViewById(R.id.tv_SorbentTime);
        tvCaptionSorbentTime.setTypeface(tfPlayBold);
        tvCaptionBattery = (TextView) findViewById(R.id.tv_Battery);
        tvCaptionBattery.setTypeface(tfPlayBold);

        RefreshHandler.post(RefreshTask);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getResources().getText(R.string.ble_not_supported).toString(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getResources().getText(R.string.error_bluetooth_not_supported).toString(), Toast.LENGTH_SHORT).show();
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

            case R.id.iv_StatusDropdown: {
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_Accum: {
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_Accum: {
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_AccumValue: {
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_ProcedureDropdown: {
                Intent intent = new Intent(this, ProceduresActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_Procedure: {
                Intent intent = new Intent(this, ProceduresActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_Procedure: {
                Intent intent = new Intent(this, ProceduresActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_ProcedureValue: {
                Intent intent = new Intent(this, ProceduresActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.bt_Procedure: {
                Intent intent = new Intent(this, ProceduresActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.iv_SorbentTime: {
                SorbTimeResetConfirmation();
                break;
            }

            case R.id.tv_SorbentTime: {
                SorbTimeResetConfirmation();
                break;
            }

            case R.id.tv_SorbentTimeValue: {
                SorbTimeResetConfirmation();
                break;
            }

            case R.id.iv_SorbentTimeDropdown: {
                SorbTimeResetConfirmation();
                break;
            }

            case R.id.tv_LastConnected: {
                enableAutoconnect();
                break;
            }

            case R.id.bt_Pause: {
                /*SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Loading preferences
                if (ProcedureSettings.getInstance().getProcedure() == Constants.PARAMETER_UNKNOWN)
                    break;
                else if (sPref.getBoolean(Constants.SETTINGS_TESTMODE, false))
                    PauseConfirmationTest();
                else
                    PauseConfirmation();*/
                ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_READY);
                if (ProcedureSettings.getInstance().getProcedure() == Constants.PARAMETER_UNKNOWN)
                    break;
                else if (ProcedureSettings.getInstance().getProcedure() == Constants.PROCEDURE_READY)
                    PauseConfirmation();
                else if (ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DIALYSIS)
                    PauseConfirmation();
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.pause_procedure)
                            .setItems(R.array.pause_dialog_items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    int selectedProcedure = -1;
                                    switch (which) {
                                        case 0: {//pause
                                            PauseConfirmationTest();
                                            break;
                                        }
                                        case 1: {//accum change
                                            selectedProcedure = Constants.PROCEDURE_CHANGE_ACCUM;
                                            break;
                                        }
                                        case 2: {//magistral' change
                                            selectedProcedure = Constants.PROCEDURE_CHANGE_SORB;
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                    if(selectedProcedure != -1){
                                        Intent intent = new Intent(MainActivity.this, InstructionActivity.class);
                                        Bundle parameters = new Bundle();
                                        parameters.putInt("procedure", selectedProcedure); //Your id
                                        intent.putExtras(parameters); //Put your id to your next Intent
                                        startActivity(intent);
                                    }
                                }
                            });
                    builder.create();
                    break;
                }
            }

            case R.id.bt_Test: {
                ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_DIALYSIS);
                if (ProcedureSettings.getInstance().getProcedure() == Constants.PARAMETER_UNKNOWN)
                    break;
                else if (ProcedureSettings.getInstance().getProcedure() == Constants.PROCEDURE_READY)
                    PauseConfirmation();
                else if (ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DIALYSIS)
                    PauseConfirmation();
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.pause_procedure)
                            .setItems(R.array.pause_dialog_items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    int selectedProcedure = -1;
                                    switch (which) {
                                        case 0: {//pause
                                            PauseConfirmationTest();
                                            break;
                                        }
                                        case 1: {//accum change
                                            selectedProcedure = Constants.PROCEDURE_CHANGE_ACCUM;
                                            break;
                                        }
                                        case 2: {//magistral' change
                                            selectedProcedure = Constants.PROCEDURE_CHANGE_SORB;
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                    if(selectedProcedure != -1){
                                        Intent intent = new Intent(MainActivity.this, InstructionActivity.class);
                                        Bundle parameters = new Bundle();
                                        parameters.putInt("procedure", selectedProcedure); //Your id
                                        intent.putExtras(parameters); //Put your id to your next Intent
                                        startActivity(intent);
                                    }
                                }
                            });
                    builder.show();
                    break;
                }

                break;
            }

            default:
                break;
        }
    }

    void SorbTimeResetConfirmation() {
        final Context context = MainActivity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(getResources().getText(R.string.reset_sorbent).toString());
        ad.setMessage(getResources().getText(R.string.reset_sorbent).toString());

        ad.setPositiveButton(getResources().getText(R.string.yes).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ProcedureSettings.getInstance().setSorbtime(-1);
            }
        });
        ad.setNegativeButton(getResources().getText(R.string.no).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {

            }
        });
        ad.show();
    }

    void PauseConfirmationTest() {
        final Context context = MainActivity.this;
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        if (ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_READY) {
            ad.setTitle(getResources().getText(R.string.stop_confirmation).toString());
            ad.setMessage(getResources().getText(R.string.stop_confirmation).toString());
        } else {
            ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
            ad.setMessage(getResources().getText(R.string.resume_confirmation).toString());
        }
        ad.setPositiveButton(getResources().getText(R.string.yes).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                if (ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_READY)
                    intentValues.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_READY);
                else
                    intentValues.putExtra(Constants.CONNECTIONSERVICE_ARG, ProcedureSettings.getInstance().getProcedure_previous());
                sendBroadcast(intentValues);
            }
        });
        ad.setNegativeButton(getResources().getText(R.string.no).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {

            }
        });
        ad.show();
    }

    void PauseConfirmation() {
        final Context context = MainActivity.this;
        Intent intent = new Intent(Constants.CONNECTIONSERVICE_ACTION);
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        if (ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_READY) {
            ad.setTitle(getResources().getText(R.string.stop_confirmation).toString());
            ad.setMessage(getResources().getText(R.string.stop_confirmation).toString());
            intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
            intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_READY);
        } else {
            switch (ProcedureSettings.getInstance().getProcedure_previous()) {
                case Constants.PROCEDURE_DIALYSIS: {
                    ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
                    ad.setMessage(getResources().getText(R.string.resume_confirmation).toString() +
                            getResources().getText(R.string.procedure_dialysis).toString() + "?");
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_DIALYSIS);
                    break;
                }

                case Constants.PROCEDURE_DISINFECTION: {
                    ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
                    ad.setMessage(getResources().getText(R.string.resume_confirmation).toString() +
                            getResources().getText(R.string.procedure_disinfection).toString() + "?");
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_DISINFECTION);
                    break;
                }

                case Constants.PROCEDURE_FILL: {
                    ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
                    ad.setMessage(getResources().getText(R.string.resume_confirmation).toString() +
                            getResources().getText(R.string.procedure_filling).toString() + "?");
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_FILL);
                    break;
                }

                case Constants.PROCEDURE_FLUSH: {
                    ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
                    ad.setMessage(getResources().getText(R.string.resume_confirmation).toString() +
                            getResources().getText(R.string.procedure_flush).toString() + "?");
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_FLUSH);
                    break;
                }

                case Constants.PROCEDURE_SHUTDOWN: {
                    ad.setTitle(getResources().getText(R.string.resume_confirmation).toString());
                    ad.setMessage(getResources().getText(R.string.resume_confirmation).toString() +
                            getResources().getText(R.string.procedure_shutdown).toString() + "?");
                    intent.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE);
                    intent.putExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PROCEDURE_FLUSH);
                    break;
                }

                default:
                    break;
            }
        }

        final Intent intentf = intent;
        ad.setPositiveButton(getResources().getText(R.string.yes).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                sendBroadcast(intentf);
            }
        });
        ad.setNegativeButton(getResources().getText(R.string.no).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {

            }
        });
        ad.show();
    }

    void enableAutoconnect() {
        SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Loading preferences
        SharedPreferences.Editor ed = sPref.edit(); //Setting for preference editing
        ed.putBoolean(Constants.SETTINGS_AUTOCONNECT, true);
        ed.apply();
    }

    Runnable RefreshTask = new Runnable() {
        @Override
        public void run() {
            int currentStatus = ProcedureSettings.getInstance().getStatus();
            int currentProcedure = ProcedureSettings.getInstance().getProcedure();
            int currentBattery = ProcedureSettings.getInstance().getBattery();
            long currentLastConnected = ProcedureSettings.getInstance().getLast_connection();
            //TODO: sorbtime status receive
            long currentSorbTime = ProcedureSettings.getInstance().getSorbtime();
            //TODO: disinfection status receive
            int currentDisinfection = Constants.PARAMETER_UNKNOWN;

            //current state
            switch (currentStatus) {
                case Constants.STATUS_ON: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_on).toString());
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.STATUS_SENDING: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_sending).toString());
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.STATUS_ERROR: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_error).toString());
                    ivStatus.setImageResource(R.drawable.ic_danger);
                    break;
                }

                case Constants.STATUS_CRITICAL_ERROR: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_critical_error).toString());
                    ivStatus.setImageResource(R.drawable.ic_danger);
                    break;
                }

                case Constants.STATUS_NORMAL: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_normal).toString());
                    ivStatus.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.STATUS_OFF: {
                    tvStatus.
                            setText(getResources().getText(R.string.state_off).toString());
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
                case Constants.PROCEDURE_FILL: {
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

            //current procedure

            switch (currentDisinfection) {
                case Constants.DISINFECTION_OK: {
                    tvDisinfection.
                            setText(getResources().getText(R.string.disinfection_ok).toString());
                    ivDisinfection.setImageResource(R.drawable.ic_tick);
                    break;
                }

                case Constants.DISINFECTION_NEED: {
                    tvDisinfection.
                            setText(getResources().getText(R.string.disinfection_need).toString());
                    ivDisinfection.setImageResource(R.drawable.ic_danger);
                    break;
                }

                default: {
                    tvDisinfection.
                            setText(getResources().getText(R.string.unknown).toString());
                    ivDisinfection.setImageResource(R.drawable.ic_question_mark);
                    break;
                }
            }

            //TODO: sorbtime
            {
            /*    if (currentSorbTime == -1) {
                    tvSorbtime.setText(getResources().getText(R.string.unknown).toString());
                    ivSorbtime.setImageResource(R.drawable.ic_question_mark);
                } else {
                    long remaining_time = Constants.SORBENT_CHANGE_MS - (System.currentTimeMillis() - currentSorbTime);
                    int hours = (int) TimeUnit.MILLISECONDS.toHours(remaining_time);
                    remaining_time -= TimeUnit.HOURS.toMillis(hours);
                    int mins = (int) TimeUnit.MILLISECONDS.toMinutes(remaining_time);
                    remaining_time -= TimeUnit.MINUTES.toMillis(mins);
                    int sec = (int) TimeUnit.MILLISECONDS.toSeconds(remaining_time);
                    tvSorbtime.setText(hours +
                            getResources().getText(R.string.sorbtime_hours).toString() +
                            mins +
                            getResources().getText(R.string.sorbtime_mins).toString() + sec);

                    ivSorbtime.setImageResource(R.drawable.ic_clock_green);
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

            //Enable or disable views when not available
            if (ProcedureSettings.getInstance().getProcedure() == Constants.PARAMETER_UNKNOWN) {
                btPause.setEnabled(false);
                btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause_disabled));
                btProcedure.setEnabled(false);
                btProcedure.setBackground(getResources().getDrawable(R.drawable.bt_procedure_disabled));
                ivProcedure.setEnabled(false);
                ivProcedureDropdown.setEnabled(false);
                tvProcedure.setEnabled(false);
                tvCaptionProcedure.setEnabled(false);
            } else {
                btPause.setEnabled(true);
                if (ProcedureSettings.getInstance().getProcedure() == Constants.PROCEDURE_READY)
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_resume));
                else
                    btPause.setBackground(getResources().getDrawable(R.drawable.ib_pause));
                btProcedure.setEnabled(true);
                btProcedure.setBackground(getResources().getDrawable(R.drawable.bt_procedure));
                ivProcedure.setEnabled(true);
                ivProcedureDropdown.setEnabled(true);
                tvProcedure.setEnabled(true);
                tvCaptionProcedure.setEnabled(true);
            }


            RefreshHandler.postDelayed(RefreshTask, Constants.PARAM_REFRESH_INTERVAL_MS);//refresh after RECONNECT_INTERVAL_MS milliseconds
        }
    };

}
