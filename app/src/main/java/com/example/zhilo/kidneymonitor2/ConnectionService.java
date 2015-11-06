package com.example.zhilo.kidneymonitor2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import java.nio.ByteBuffer;

public class ConnectionService extends Service {

    //Initialisation of LogWriter
    private static final String TAG = "ConnectionService";
    LogWriter lw = new LogWriter();

    public static boolean isServiceRunning = false;

    private SharedPreferences sPref;

    private BluetoothLeService mBluetoothLeService;
    private boolean BLEConnected = false;

    //Handler tries to connect to device every 5s 10 times
    Handler AutoconnectHandler = new Handler();
    static int ConnectTryCount = 0;

    //Random value for notifications IDs
    private static int NOTIFY_ID = 238;

    public ConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        lw.appendLog(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        lw.appendLog(TAG, "onCreate");
        sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE); //Load preferences;

        //Register receiver with filter to receive messages from MainActivity
        IntentFilter intFilt = new IntentFilter(Constants.CONNECTIONSERVICE_ACTION);
        registerReceiver(brCommandReceiver, intFilt);
        registerReceiver(brBLEUpdateReceiver, makeGattUpdateIntentFilter());
        AutoconnectHandler.post(AutoconnectTask);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        lw.appendLog(TAG, "onStartCommand");
        isServiceRunning = true;

        if (sPref.getBoolean(Constants.SETTINGS_FOREGROUND, false))
            startInForeground();

        return START_STICKY;//Service will be restarted if killed by Android
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lw.appendLog(TAG, "onDestroy");

        isServiceRunning = false;
        unregisterReceiver(brCommandReceiver);
        unregisterReceiver(brBLEUpdateReceiver);
        if(BLEConnected)
            getApplicationContext().unbindService(BLEServiceConnection);
        mBluetoothLeService = null;
        stopForeground(true);
        stopSelf();
    }

    /**
     * Start service in foreground with notification in bar
     */
    public void startInForeground() {

        Bitmap icon = BitmapFactory.decodeResource(ConnectionService.this.getResources(),
                R.mipmap.ic_launcher);

        //start MainActivity on notification click
        Intent notificationIntent = new Intent(ConnectionService.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ConnectionService.this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notif = new Notification.Builder(ConnectionService.this)
                .setContentIntent(contentIntent)
                .setContentTitle(getResources().getText(R.string.app_name))
                .setContentText("title_click_to_open")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .build();

        Intent i = new Intent(this, ConnectionService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startForeground(Constants.FOREGROUND_SERVICE_ID, notif);
    }

    /**
     * Send notification to user through notification bar
     *
     * @param currentArg String to show
     */
    void sendNotification(String currentArg) {
        lw.appendLog(TAG, "NOTIF:" + currentArg);

        Context context = ConnectionService.this;
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_danger);

        //start MainActivity on click
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .setContentTitle(getResources().getText(R.string.app_name))
                .setContentText(currentArg)
                .setSmallIcon(R.drawable.ic_danger)
                .setLargeIcon(icon)
                .setAutoCancel(true)
                .setLights(Color.WHITE, 0, 1)
                .build();

        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;

        if (sPref.getBoolean(Constants.SETTINGS_VIBRATION, false))
            notification.vibrate = new long[]{1000, 1000, 1000};


        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (sPref.getBoolean(Constants.SETTINGS_SOUND, false))
            notification.sound = soundUri;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
        NOTIFY_ID++;
        notificationManager.cancel(NOTIFY_ID--);
    }

    /**
     * Converts byte array to hex string
     *
     * @param bytes Non-null byte array
     * @return Hex string
     */
    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Handle messages received from main screen activity: setting status and pause/resume,
     * do pairing  with saved address
     */
    BroadcastReceiver brCommandReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String task = intent.getStringExtra(Constants.CONNECTIONSERVICE_TASK);
            final String arg = intent.getStringExtra(Constants.CONNECTIONSERVICE_ARG);
            // switch tasks for setting main screen values
            if (Constants.CONNECTIONSERVICE_ACTION_START_BLE_SERVICE.equals(task)) {
                Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
                getApplicationContext().bindService(gattServiceIntent, BLEServiceConnection, BIND_AUTO_CREATE);
            } else if (Constants.CONNECTIONSERVICE_ACTION_STOP_BLE_SERVICE.equals(task)) {
                //unbindService(BLEServiceConnection);
                getApplicationContext().unbindService(BLEServiceConnection);
                mBluetoothLeService = null;
            } else {

            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection BLEServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize())
                lw.appendLog(TAG, "Unable to initialize Bluetooth");
            else
                lw.appendLog(TAG, "Bluetooth initialized");
            // Automatically connects to the device upon successful start-up initialization.
            String address = sPref.getString(Constants.SETTINGS_ADDRESS, "00:00:00:00:00:00");
            if (!"00:00:00:00:00:00".equals(address)) {
                mBluetoothLeService.connect(address);
                lw.appendLog(TAG, "Trying to connect to " + address);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver brBLEUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {
                BLEConnected = true;
                ConnectTryCount = 0;
                lw.appendLog(TAG, "BLE connected");
            } else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                BLEConnected = false;
                Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_STOP_BLE_SERVICE);
                sendBroadcast(intentValues);
                lw.appendLog(TAG, "BLE disconnected");
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.hasExtra(Constants.EXTRA_DATA)) {
                    byte[] pack = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                    lw.appendLog(TAG, bytesToHex(pack));
                    processPack(pack);
                }
            }
        }
    };

    void processPack(byte[] pack){
        if(checkPack(pack)){
            byte com1 = pack[Constants.COM1_INDEX];//first command
            byte com2 = pack[Constants.COM2_INDEX];//second command

            int data_int = ByteBuffer.wrap(pack, Constants.DATA_INDEX, Constants.DATA_LENGTH).getInt();//data converted to int
            float data_float = ByteBuffer.wrap(pack, Constants.DATA_INDEX, Constants.DATA_LENGTH).getFloat();//data converted to float

            ProcedureSettings.getInstance().setLast_connection(System.currentTimeMillis());//setting flag for time of last received command
            switch (com1) {//executing first command

                case Constants.bBATT: {//setting battery percentage
                    lw.appendLog(TAG, "batt_set" + data_int + "%", true);
                    lw.appendLog(TAG, "setting battery to " + data_int + "%");
                    ProcedureSettings.getInstance().setBattery(data_int);
                    break;
                }

                case Constants.bSTATUS: {//setting current procedure
                    lw.appendLog(TAG, "got command STATUS and " + data_int);
                    sendMessageBytes(bHEARTBEAT);
                    switch (data_int) {
                        case Constants.bPROCEDURE_FILLING: {
                            lw.appendLog(TAG, "setting STATUS to FILLING, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_filling", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_FILLING) &&
                               (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_FILLING);
                            break;
                        }

                        case Constants.bPROCEDURE_DIALYSIS: {
                            lw.appendLog(TAG, "setting STATUS to DIALYSIS, previous is " + ProcedureSettings.getInstance().getProcedure_previous());

                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_dialysis", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DIALYSIS) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_DIALYSIS);
                            break;
                        }

                        case Constants.bPROCEDURE_SHUTDOWN: {
                            lw.appendLog(TAG, "setting STATUS to SHUTDOWN, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_shutdown", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_SHUTDOWN) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_SHUTDOWN);
                            break;
                        }

                        case Constants.bPROCEDURE_DISINFECTION: {
                            lw.appendLog(TAG, "setting STATUS to DISINFECTION, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_disinfection", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DISINFECTION) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_DISINFECTION);
                            break;
                        }

                        case Constants.bPROCEDURE_READY: {
                            lw.appendLog(TAG, "setting STATUS to READY, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_ready", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_READY) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_READY);
                            break;
                        }

                        case Constants.bPROCEDURE_FLUSH: {
                            lw.appendLog(TAG, "setting STATUS to FLUSH, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_flush", true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_FLUSH) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_FLUSH);
                            break;
                        }

                        default: {
                            lw.appendLog(TAG, "setting STATUS to UNKNOWN, previous is " + ProcedureSettings.getInstance().getProcedure_previous(), true);
                            lw.appendLog(TAG, "starus_set" +
                                    "value_status_unknown", true);
                            ProcedureSettings.getInstance().setProcedure(Constants.PARAMETER_UNKNOWN);
                            break;
                        }
                    }
                    break;
                }

                case Constants.bPARAMS: {//NOTE: not received from device
                    lw.appendLog(TAG, "got command PARAMS and " + currentArg);
                    switch (currentArg) {
                        case bPARAMS_NORM: {
                            lw.appendLog(TAG, getResources().getText(R.string.params_set).toString() +
                                    getResources().getText(R.string.value_procedure_params_normal).toString(), true);
                            PARAMS = PARAMS_NORMAL;
                            break;
                        }

                        case bPARAMS_DANGER: {
                            lw.appendLog(TAG, getResources().getText(R.string.params_set).toString() +
                                    getResources().getText(R.string.value_procedure_params_danger).toString(), true);
                            PARAMS = PARAMS_DANGER;
                            break;
                        }

                        default: {
                            lw.appendLog(TAG, getResources().getText(R.string.params_set).toString() +
                                    getResources().getText(R.string.value_procedure_params_unknown).toString(), true);
                            PARAMS = PARAMS_UNKNOWN;
                            break;
                        }
                    }
                    break;
                }

                case bSORBTIME: {//NOTE: not received from device
                    lw.appendLog(TAG, "setting SORBTIME to " + currentArg, true);
                    SORBTIME = String.valueOf(data_int);
                    break;
                }

                case bFUNCT: {//NOTE: not received from device
                    lw.appendLog(TAG, "got command FUNCT and " + currentArg);
                    switch (currentArg) {
                        case bFUNCT_CORRECT: {
                            lw.appendLog(TAG, getResources().getText(R.string.funct_set).toString() +
                                    getResources().getText(R.string.value_device_functioning_correct).toString(), true);
                            FUNCT = FUNCT_CORRECT;
                            break;
                        }

                        case bFUNCT_FAULT: {
                            lw.appendLog(TAG, getResources().getText(R.string.funct_set).toString() +
                                    getResources().getText(R.string.value_device_functioning_fault).toString(), true);
                            FUNCT = FUNCT_FAULT;
                            break;
                        }

                        default: {
                            lw.appendLog(TAG, getResources().getText(R.string.funct_set).toString() +
                                    getResources().getText(R.string.value_device_functioning_unknown).toString(), true);
                            FUNCT = FUNCT_UNKNOWN;
                            break;
                        }
                    }
                    break;
                }

                case bDPRESS1: {//setting first pressure value
                    fDPRESS1 = data_float * 51.715f;
                    DPRESS1 = String.valueOf(fDPRESS1);//converting to mmHg and string
                    lw.appendLog(TAG, "setting DPRESS1 to " + DPRESS1);
                    break;
                }

                case bDPRESS2: {//setting second pressure value
                    fDPRESS2 = data_float * 51.715f;
                    DPRESS2 = String.valueOf(fDPRESS2);//converting to mmHg and string
                    lw.appendLog(TAG, "setting DPRESS2 to " + DPRESS2);
                    break;
                }

                case bDPRESS3: {//setting third pressure value
                    fDPRESS3 = data_float * 51.715f;
                    DPRESS3 = String.valueOf(fDPRESS3);//converting to mmHg and string
                    lw.appendLog(TAG, "setting DPRESS3 to " + DPRESS3);
                    break;
                }

                case bDTEMP1: {//setting temperature value
                    fDTEMP1 = data_int / 10.0f;//converting to Celsius degrees and string
                    DTEMP1 = String.valueOf(fDTEMP1);
                    lw.appendLog(TAG, "setting DTEMP1 to " + DTEMP1);
                    break;
                }

                case bDCOND1: {////setting conductivity value
                    iDCOND1 = data_int;
                    DCOND1 = String.valueOf(iDCOND1);
                    lw.appendLog(TAG, "setting DCOND1 to " + DCOND1);
                    break;
                }

                case bDCUR1: {//setting first electric current value
                    fDCUR1 = data_float * 1000;
                    DCUR1 = String.valueOf(fDCUR1);
                    lw.appendLog(TAG, "setting DCUR1 to " + DCUR1);
                    break;
                }

                case bDCUR2: {//setting second electric current value
                    fDCUR2 = data_float * 1000;
                    DCUR2 = String.valueOf(fDCUR2);
                    lw.appendLog(TAG, "setting DCUR2 to " + DCUR2);
                    break;
                }

                case bDCUR3: {//setting third electric current value
                    fDCUR3 = data_float * 1000;
                    DCUR3 = String.valueOf(fDCUR3);
                    lw.appendLog(TAG, "setting DCUR3 to " + DCUR3);
                    break;
                }

                case bDCUR4: {//setting fourth electric current value
                    fDCUR4 = data_float * 1000;
                    DCUR4 = String.valueOf(fDCUR4);
                    lw.appendLog(TAG, "setting DCUR4 to " + DCUR4);
                    break;
                }

                case bSENDDPUMPS: {//sending pumps flows
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send FPUMP1FLOW");
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x01, intTo4byte(FPUMP1FLOW));//first filling pump
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DPUMP1FLOW");
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x02, intTo4byte(DPUMP1FLOW));//first dialysis pump
                            break;
                        }

                        case (byte)0x03:{
                            lw.appendLog(TAG, "send UFPUMP1FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x03, intTo4byte(UFPUMP1FLOW));//first unfilling pump
                            break;
                        }

                        case (byte)0x11:{
                            lw.appendLog(TAG, "send FPUMP2FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x11, intTo4byte(FPUMP2FLOW));//first filling pump
                            break;
                        }

                        case (byte)0x12:{
                            lw.appendLog(TAG, "send DPUMP2FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x12, intTo4byte(DPUMP2FLOW));//first dialysis pump
                            break;
                        }

                        case (byte)0x13:{
                            lw.appendLog(TAG, "send UFPUMP2FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x13, intTo4byte(UFPUMP2FLOW));//first unfilling pump
                            break;
                        }

                        case (byte)0x21:{
                            lw.appendLog(TAG, "send FPUMP3FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x21, intTo4byte(FPUMP3FLOW));//first filling pump
                            break;
                        }

                        case (byte)0x22:{
                            lw.appendLog(TAG, "send DPUMP3FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x22, intTo4byte(DPUMP3FLOW));//first dialysis pump
                            break;
                        }

                        case (byte)0x23:{
                            lw.appendLog(TAG, "send UFPUMP3FLOW", true);
                            sendMessageBytes((byte) (bSENDDPUMPS + (byte) 0x01), (byte) 0x23, intTo4byte(UFPUMP3FLOW));//first unfilling pump
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case bSENDDPRESS: {//sending values for pressures ranges
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DPRESS1MIN");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x01, floatTo4byte(DPRESS1MIN));//first min value
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DPRESS1MAX");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x02, floatTo4byte(DPRESS1MAX));//first max value
                            break;
                        }

                        case (byte)0x11:{
                            lw.appendLog(TAG, "send DPRESS2MIN");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x11, floatTo4byte(DPRESS2MIN));//second min value
                            break;
                        }

                        case (byte)0x12:{
                            lw.appendLog(TAG, "send DPRESS2MAX");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x12, floatTo4byte(DPRESS2MAX));//second max value
                            break;
                        }

                        case (byte)0x21:{
                            lw.appendLog(TAG, "send DPRESS3MIN");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x21, floatTo4byte(DPRESS3MIN));//third min value
                            break;
                        }

                        case (byte)0x22:{
                            lw.appendLog(TAG, "send DPRESS3MAX");
                            sendMessageBytes((byte) (bSENDDPRESS + (byte) 0x01), (byte) 0x22, floatTo4byte(DPRESS3MAX));//third max value
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case bSENDDTEMP: {//sending values for temperature range
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DTEMP1MIN ");
                            sendMessageBytes((byte) (bSENDDTEMP + (byte) 0x01), (byte) 0x01, floatTo4byte(DTEMP1MIN));//min temp
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DTEMP1MAX ");
                            sendMessageBytes((byte) (bSENDDTEMP + (byte) 0x01), (byte) 0x02, floatTo4byte(DTEMP1MAX));//max temp
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case bSENDDCOND: {//sending values for conductivity range
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DCOND1MIN ");
                            sendMessageBytes((byte) (bSENDDCOND + (byte) 0x01), (byte) 0x01, floatTo4byte(DCOND1MIN));
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DCOND1MAX ");
                            sendMessageBytes((byte) (bSENDDCOND + (byte) 0x01), (byte) 0x02, floatTo4byte(DCOND1MAX));
                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case PE_PRESS1: {//receiving error
                    processError(getResources().getText(R.string.error_press).toString() + "1");
                    break;
                }

                case PE_PRESS2: {//receiving error
                    processError(getResources().getText(R.string.error_press).toString() + "2");
                    break;
                }

                case PE_PRESS3: {//receiving error
                    processError(getResources().getText(R.string.error_press).toString() + "3");
                    break;
                }

                case PE_TEMP: {//receiving error
                    processError(getResources().getText(R.string.error_temp).toString());
                    break;
                }

                case PE_ELECTRO: {//receiving error
                    processError(getResources().getText(R.string.error_electro).toString());
                    break;
                }

                case PE_EDS1: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "1");
                    break;
                }

                case PE_EDS2: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "2");
                    break;
                }

                case PE_EDS3: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "3");
                    break;
                }

                case PE_EDS4: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "4");
                    break;
                }

                case PE_BATT: {//receiving error
                    processError(getResources().getText(R.string.error_batt).toString());
                    break;
                }

                case PE_PUMP1: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "1");
                    break;
                }

                case PE_PUMP2: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "2");
                    break;
                }

                case PE_PUMP3: {//receiving error
                    processError(getResources().getText(R.string.error_eds).toString() + "3");
                    break;
                }

                case PE_ERROR: {//receiving error
                    processError(getResources().getText(R.string.error_unknown).toString());
                    break;
                }

                default:
                    break;
            }
        }
    }

    /**
     * Check if received package is in desired format
     * @param pack package as array of bytes
     * @return true if package is correct, otherwise false
     */
    boolean checkPack(byte[] pack){
        if(pack.length == 8)
            return (pack[0] == Constants.PACK_START && pack[7] == Constants.PACK_END);
        return false;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    Runnable AutoconnectTask = new Runnable() {
        @Override
        public void run() {
            if (!BLEConnected && sPref.getBoolean(Constants.SETTINGS_AUTOCONNECT, false) &&
                    ConnectTryCount <= Constants.CONNECT_ATTEMPTS_MAX) {//if waiting for connection - try to connect to saved device
                String address = sPref.getString(Constants.SETTINGS_ADDRESS, "00:00:00:00:00:00");
                if (!"00:00:00:00:00:00".equals(address)) {
                    Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_BLE_SERVICE);
                    sendBroadcast(intentValues);
                    lw.appendLog(TAG, "Connecting try " + ConnectTryCount);
                    ConnectTryCount++;
                }
            }
            if (ConnectTryCount > Constants.CONNECT_ATTEMPTS_MAX) {
                SharedPreferences.Editor ed = sPref.edit(); //Setting for preference editing
                ed.putBoolean(Constants.SETTINGS_AUTOCONNECT, false);
                ed.apply();
                sendNotification("autoconnect_failed");
                ConnectTryCount = 0;
            }
            AutoconnectHandler.postDelayed(AutoconnectTask, Constants.RECONNECT_INTERVAL_MS);//refresh after RECONNECT_INTERVAL_MS milliseconds
        }
    };
}
