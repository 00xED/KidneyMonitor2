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
     * Constants for ProcedureSettings
     */
    String procedureSettingsFile = "kidneymonitor2/settings.txt";

    int PROCEDURE_FILLING = 0;
    int PROCEDURE_DIALYSIS = 1;
    int PROCEDURE_SHUTDOWN = 2;
    int PROCEDURE_DISINFECTION = 3;
    int PROCEDURE_READY = 4;
    int PROCEDURE_FLUSH = 5;
    int PARAMETER_UNKNOWN = -1;

    int PARAMS_NORMAL = 0;
    int PARAMS_DANGER = 1;

    int FUNCT_CORRECT = 0;
    int FUNCT_FAULT = 1;
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

    byte PACK_START = (byte) 0x55;
    int PACK_START_INDEX = 0;
    int COM1_INDEX = 1;
    int COM2_INDEX = 2;
    int DATA_INDEX = 3;
    int DATA_LENGTH = 4;
    byte PACK_END = (byte) 0xAA;
    int PACK_END_INDEX = 7;

    /**
     * Bytes for output commands
     */
    byte bSENDDPRESS = (byte) 0x10;//Receiving command to set dialysis pressures
    byte bSENDDCOND = (byte) 0x12;//Receiving command to set dialysis conductivity
    byte bSENDDTEMP = (byte) 0x14;//Receiving command to set dialysis temperature
    byte bSENDDPUMPS = (byte) 0x16;//Receiving command to set pumps flows
    byte bHEARTBEAT = (byte) 0x18;//Receiving command to set pumps flows

    byte bPAUSE = (byte) 0x5A;//Send to pause current procedure
    byte bFILLING = (byte) 0x5B;//Send to set procedure to FILLING
    byte bDIALYSIS = (byte) 0x5C;//Send to set procedure to DIALYSIS
    byte bFLUSH = (byte) 0x5D;//Send to set procedure to FLUSH
    byte bDISINFECTION = (byte) 0x5E;//Send to set procedure to DISINFECTION
    byte bSHUTDOWN = (byte) 0x5F;//Send to set procedure to SHUTDOWN

    byte bBATT = (byte) 0xE9;//Receiving battery stats

    byte bSTATUS = (byte) 0xEF;//Receiving current procedure
    byte bPROCEDURE_FILLING = (byte) 0x5B;
    byte bPROCEDURE_DIALYSIS = (byte) 0x5C;
    byte bPROCEDURE_DISINFECTION = (byte) 0x5E;
    byte bPROCEDURE_SHUTDOWN = (byte) 0x5F;
    byte bPROCEDURE_READY = (byte) 0x5A;
    byte bPROCEDURE_FLUSH = (byte) 0x5D;

    byte bPARAMS = (byte) 0x84;//Receiving procedure params
    byte bPARAMS_NORM = (byte) 0x10;
    byte bPARAMS_DANGER = (byte) 0x11;

    byte bSORBTIME = (byte) 0x85;//Receiving sorbtime

    byte bFUNCT = (byte) 0x86;//Receiving device functioning
    byte bFUNCT_CORRECT = (byte) 0x10;
    byte bFUNCT_FAULT = (byte) 0x11;

    byte bDPRESS1 = (byte) 0xE0;//Receiving dialysis pressure1
    byte bDPRESS2 = (byte) 0xE1;//Receiving dialysis pressure2
    byte bDPRESS3 = (byte) 0xE2;//Receiving dialysis pressure3

    byte bDTEMP1 = (byte) 0xE3;//Receiving dialysis temperature1
    byte bDCOND1 = (byte) 0xE4;//Receiving dialysis conductivity1

    byte bDCUR1 = (byte) 0xE5;//Receiving dialysis current1
    byte bDCUR2 = (byte) 0xE6;//Receiving dialysis current2
    byte bDCUR3 = (byte) 0xE7;//Receiving dialysis current3
    byte bDCUR4 = (byte) 0xE8;//Receiving dialysis current4

    /**
     * *ERROR codes
     */
    byte PE_PRESS1 = (byte) 0xF0;    // Error on pressure sensor 1
    byte PE_PRESS2 = (byte) 0xF1;    // Error on pressure sensor 2
    byte PE_PRESS3 = (byte) 0xF2;    // Error on pressure sensor 3
    byte PE_TEMP = (byte) 0xF3;    // Error on temperature sensor
    byte PE_ELECTRO = (byte) 0xF4;    // Error on conductivity sensor
    byte PE_EDS1 = (byte) 0xF5;    // Error on electric cell 1
    byte PE_EDS2 = (byte) 0xF6;    // Error on electric cell 2
    byte PE_EDS3 = (byte) 0xF7;    // Error on electric cell 3
    byte PE_EDS4 = (byte) 0xF8;    // Error on electric cell 4
    byte PE_BATT = (byte) 0xF9;    // Error on low battery
    byte PE_PUMP1 = (byte) 0xFA;    // Pump 1 error, rpm low
    byte PE_PUMP2 = (byte) 0xFB;    // Pump 2 error, rpm low
    byte PE_PUMP3 = (byte) 0xFC;    // Pump 3 error, rpm low
    byte PE_ERROR = (byte) 0xFF;    // Pump 3 error, rpm low

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
