package com.example.zhilo.kidneymonitor2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Initialisation of LogWriter
    private static final String logTag = "MainActivity";
    private LogWriter lw = new LogWriter();

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lw.appendLog(logTag, "Start");
        lw.appendLog(logTag, "MainActivity opened");

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
                    "title_prefs_new",
                    Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this,
                "title_prefs_loaded",
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
            case R.id.bt_Settings://Start or stop service
            {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.bt2:{
                Intent intent = new Intent(this, ParamActivity.class);
                startActivity(intent);
                break;
            }

            default:
                break;
        }
    }

}
