package com.example.zhilo.kidneymonitor2;

import java.util.UUID;

/**
 * Defines several constants used.
 */
public interface Constants {

    /**
     * Constants for LogWriter
     */
    String fileDirectory = "/kidneymonitor2/";
    String logFile = "kidneymonitor2/kidneymonitor2.log";
    String logFileDebug = "kidneymonitor2/kidneymonitor2_debug.log";

    /**
     * Constants for MainActivity
     */
    int REQUEST_ENABLE_BT = 1;

    /**
     * Constants for DeviceListActivity
     */
    int CHOOSE_DEVICE = 1;

    /**
     * Constants for SharedPreferences
     */
    String APP_PREFERENCES = "KIDNEYMON2_SETTINGS";
    String SETTINGS_NAME = "NAME";
    String SETTINGS_ADDRESS = "ADDRESS";
    String SETTINGS_FOREGROUND = "FOREGROUND_SERVICE";
    String SETTINGS_VIBRATION = "VIBRATION";
    String SETTINGS_SOUND = "SOUND";
    String SETTINGS_TESTMODE = "TESTMODE";
    String SETTINGS_AUTOCONNECT = "AUTOCONNECT";

    /**
     * Constants for ConnectionService
     */
    String CONNECTIONSERVICE_ACTION = "ConnectionService_BR_Action";
    String CONNECTIONSERVICE_TASK = "ConnectionService_BR_Task";
    String CONNECTIONSERVICE_ARG = "ConnectionService_BR_Arg";
    String CONNECTIONSERVICE_ACTION_START_BLE_SERVICE = "com.example.zhilo.kidneymonitor2.ConnectionService.START_BLE";
    String CONNECTIONSERVICE_ACTION_STOP_BLE_SERVICE = "com.example.zhilo.kidneymonitor2.ConnectionService.STOP _BLE";
    int FOREGROUND_SERVICE_ID = 237; //Magic number, voodoo number

    /**
     * Constants for BluetoothLeService
     */
    int CONNECT_ATTEMPTS_MAX = 20;
    int RECONNECT_INTERVAL_MS = 5000;
    int STATE_DISCONNECTED = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;
    String ACTION_GATT_CONNECTED =
            "com.example.zhilo.kidneymonitor2.BLE.ACTION_GATT_CONNECTED";//connected to a GATT server.
    String ACTION_GATT_DISCONNECTED =
            "com.example.zhilo.kidneymonitor2.BLE.ACTION_GATT_DISCONNECTED";//disconnected from a GATT server.
    String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.zhilo.kidneymonitor2.BLE.ACTION_GATT_SERVICES_DISCOVERED";//discovered GATT services.
    String ACTION_DATA_AVAILABLE =
            "com.example.zhilo.kidneymonitor2.BLE.ACTION_DATA_AVAILABLE";//received data from the device.  This can be a result of read or notification operations.
    String EXTRA_DATA =
            "com.example.zhilo.kidneymonitor2.BLE.EXTRA_DATA";//received data from the device
    UUID UUID_SPP_DATA = UUID.fromString("a4656700-45bb-4809-886e-180f8a42de85");//UUID for SPP Data characteristic
    UUID UUID_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");//UUID for client characteristic configuration descriptor
}
