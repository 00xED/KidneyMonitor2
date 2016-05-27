package com.example.zhilo.kidneymonitor2;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Window;
import android.view.WindowManager;

import java.nio.ByteBuffer;

public class ConnectionService extends Service {

    //Initialisation of LogWriter
    private static final String TAG = "CS";
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
                .setContentTitle(Constants.APP_NAME)
                .setContentText(getResources().getText(R.string.click_to_open).toString())
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
                .setContentTitle(Constants.APP_NAME)
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
            final int arg = intent.getIntExtra(Constants.CONNECTIONSERVICE_ARG, Constants.PARAMETER_UNKNOWN);
            // switch tasks for setting main screen values
            if (Constants.CONNECTIONSERVICE_ACTION_START_BLE_SERVICE.equals(task)) {
                Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
                getApplicationContext().bindService(gattServiceIntent, BLEServiceConnection, BIND_AUTO_CREATE);
            } else if (Constants.CONNECTIONSERVICE_ACTION_STOP_BLE_SERVICE.equals(task)) {
                //unbindService(BLEServiceConnection);
                getApplicationContext().unbindService(BLEServiceConnection);
                mBluetoothLeService = null;
            } else if (Constants.CONNECTIONSERVICE_ACTION_START_PROCEDURE.equals(task)) {
                startProcedure(arg);
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
                ProcedureSettings.getInstance().setStatus(Constants.STATUS_ON);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendMessageBytes(Constants.bTIME, Constants.bTIME_GET);//current time request
                    }
                }, 2000);

            } else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                BLEConnected = false;
                Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_STOP_BLE_SERVICE);
                sendBroadcast(intentValues);
                lw.appendLog(TAG, "BLE disconnected");
                ProcedureSettings.getInstance().setStatus(Constants.STATUS_OFF);
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.hasExtra(Constants.EXTRA_DATA)) {
                    byte[] pack = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                    //lw.appendLog(TAG, "GOT " + bytesToHex(pack), true);
                    processPack(pack);
                    //mBluetoothLeService.writeSPP(pack);
                }
            }
        }
    };

    void processPack(byte[] pack){
        if(checkPack(pack)){
            byte com1 = pack[Constants.COM1_INDEX];//first command
            byte com2 = pack[Constants.COM2_INDEX];//second command

            byte data1 = pack[Constants.DATA_INDEX];
            int data_int = ByteBuffer.wrap(pack, Constants.DATA_INDEX, Constants.DATA_LENGTH).getInt();//data converted to int
            float data_float = ByteBuffer.wrap(pack, Constants.DATA_INDEX, Constants.DATA_LENGTH).getFloat();//data converted to float

            float tempfValue = 0.0f;
            int tempiValue = 0;
            int errorCount = 0;

            ProcedureSettings.getInstance().setLast_connection(System.currentTimeMillis());//setting flag for time of last received command
            switch (com1) {//executing first command

                case Constants.bBATT: {//setting battery percentage
                    switch (com2) { //battery type
                        case Constants.bBATT_ACC: {
                            lw.appendLog(TAG, getResources().getText(R.string.batt_set).toString() + data_int + "%", true);
                            lw.appendLog(TAG, "setting battery to " + data_int + "%");
                            ProcedureSettings.getInstance().setBattery(data_int);
                            break;
                        }

                        case Constants.bBATT_COIN: {
                            lw.appendLog(TAG, "coin battery is " + data_int + "%");
                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case Constants.bSTATUS: {//setting current procedure
                    lw.appendLog(TAG, "got command STATUS and " + data_int);
                    sendMessageBytes(Constants.bHEARTBEAT);
                    switch (data1) {
                        case Constants.bPROCEDURE_FILLING: {
                            lw.appendLog(TAG, "setting STATUS to FILLING, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_filling).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_FILL) &&
                               (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());
                            ProcedureSettings.getInstance().setSorbtime(-1);
                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_FILL);
                            break;
                        }

                        case Constants.bPROCEDURE_DIALYSIS: {
                            lw.appendLog(TAG, "setting STATUS to DIALYSIS, previous is " + ProcedureSettings.getInstance().getProcedure_previous());

                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_dialysis).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DIALYSIS) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            if(ProcedureSettings.getInstance().getProcedure_previous() == Constants.PROCEDURE_FILL)
                                ProcedureSettings.getInstance().setSorbtime(System.currentTimeMillis());
                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_DIALYSIS);
                            break;
                        }

                        case Constants.bPROCEDURE_SHUTDOWN: {
                            lw.appendLog(TAG, "setting STATUS to SHUTDOWN, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_shutdown).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_SHUTDOWN) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());
                            ProcedureSettings.getInstance().setSorbtime(-1);
                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_SHUTDOWN);
                            break;
                        }

                        case Constants.bPROCEDURE_DISINFECTION: {
                            lw.appendLog(TAG, "setting STATUS to DISINFECTION, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_disinfection).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_DISINFECTION) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());
                            ProcedureSettings.getInstance().setSorbtime(-1);
                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_DISINFECTION);
                            break;
                        }

                        case Constants.bPROCEDURE_READY: {
                            lw.appendLog(TAG, "setting STATUS to READY, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_ready).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_READY) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());

                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_READY);
                            break;
                        }

                        case Constants.bPROCEDURE_FLUSH: {
                            lw.appendLog(TAG, "setting STATUS to FLUSH, previous is " + ProcedureSettings.getInstance().getProcedure_previous());
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    getResources().getText(R.string.procedure_flush).toString(), true);
                            if ((ProcedureSettings.getInstance().getProcedure() != Constants.PROCEDURE_FLUSH) &&
                                    (ProcedureSettings.getInstance().getProcedure() != Constants.PARAMETER_UNKNOWN))//if previous status is not FILLING and not UNKNOWN
                                ProcedureSettings.getInstance().setProcedure_previous(ProcedureSettings.getInstance().getProcedure());
                            ProcedureSettings.getInstance().setSorbtime(-1);
                            ProcedureSettings.getInstance().setProcedure(Constants.PROCEDURE_FLUSH);
                            break;
                        }

                        default: {
                            lw.appendLog(TAG, "setting STATUS to UNKNOWN, previous is " + ProcedureSettings.getInstance().getProcedure_previous(), true);
                            lw.appendLog(TAG, getResources().getText(R.string.starus_set).toString() +
                                    "procedure_unknown", true);
                            ProcedureSettings.getInstance().setProcedure(Constants.PARAMETER_UNKNOWN);
                            break;
                        }
                    }
                    break;
                }

                case Constants.bDPRESS1: {//getting first pressure value
                    tempfValue = data_float * Constants.PRESS_COEF;
                    ProcedureSettings.getInstance().setDialPress1(tempfValue);
                    lw.appendLog(TAG, "setting DPRESS1 to " + tempfValue);
                    /*if(tempfValue > ProcedureSettings.getInstance().getDialPress1Max() ||
                       tempfValue < ProcedureSettings.getInstance().getDialPress1Min()){
                        errorCount++;
                        processError("DPRESS1 not in range!");
                    }*/
                    break;
                }

                case Constants.bDPRESS2: {//getting second pressure value
                    tempfValue = data_float * Constants.PRESS_COEF;
                    ProcedureSettings.getInstance().setDialPress2(tempfValue);
                    lw.appendLog(TAG, "setting DPRESS2 to " + tempfValue);
                   /* if(tempfValue > ProcedureSettings.getInstance().getDialPress2Max() ||
                            tempfValue < ProcedureSettings.getInstance().getDialPress2Min()){
                        errorCount++;
                        processError("DPRESS2 not in range!");
                    }*/
                    break;
                }

                case Constants.bDPRESS3: {//getting third pressure value
                    tempfValue = data_float * Constants.PRESS_COEF;
                    ProcedureSettings.getInstance().setDialPress3(tempfValue);
                    lw.appendLog(TAG, "setting DPRESS3 to " + tempfValue);
                   /* if(tempfValue > ProcedureSettings.getInstance().getDialPress3Max() ||
                            tempfValue < ProcedureSettings.getInstance().getDialPress3Min()){
                        errorCount++;
                        processError("DPRESS3 not in range!");
                    }*/
                    break;
                }

                case Constants.bDTEMP1: {//getting temperature value
                    tempfValue = data_int / Constants.TEMP_COEF;
                    ProcedureSettings.getInstance().setDialTemp1(tempfValue);
                    lw.appendLog(TAG, "setting DTEMP1 to " + tempfValue);
                   /* if(tempfValue > ProcedureSettings.getInstance().getDialTemp1Max() ||
                            tempfValue < ProcedureSettings.getInstance().getDialTemp1Min()){
                        errorCount++;
                        processError("DTEMP1 not in range!");
                    }*/
                    break;
                }

                case Constants.bDCOND1: {////getting conductivity value
                    if(com2 == (byte)0x00){
                        tempiValue = data_int;
                        ProcedureSettings.getInstance().setDialCond1(tempiValue);
                        lw.appendLog(TAG, "setting DCOND1 to " + tempiValue);
                    }
                   /* if(tempiValue > ProcedureSettings.getInstance().getDialCond1Max() ||
                            tempiValue < ProcedureSettings.getInstance().getDialCond1Min()){
                        errorCount++;
                        processError("DCOND1 not in range!");
                    }*/
                    break;
                }

                case Constants.bDCUR1: {//getting first electric current value
                    tempfValue = data_float * Constants.CUR_COEF;
                    ProcedureSettings.getInstance().setDialCurrent1(tempfValue);
                    lw.appendLog(TAG, "setting DCUR1 to " + tempfValue);
                    break;
                }

                case Constants.bDCUR2: {//getting second electric current value
                    tempfValue = data_float * Constants.CUR_COEF;
                    ProcedureSettings.getInstance().setDialCurrent2(tempfValue);
                    lw.appendLog(TAG, "setting DCUR2 to " + tempfValue);
                    break;
                }

                case Constants.bDCUR3: {//getting third electric current value
                    tempfValue = data_float * Constants.CUR_COEF;
                    ProcedureSettings.getInstance().setDialCurrent3(tempfValue);
                    lw.appendLog(TAG, "setting DCUR3 to " + tempfValue);
                    break;
                }

                case Constants.bDCUR4: {//getting fourth electric current value
                    tempfValue = data_float * Constants.CUR_COEF;
                    ProcedureSettings.getInstance().setDialCurrent4(tempfValue);
                    lw.appendLog(TAG, "setting DCUR4 to " + tempfValue);
                    break;
                }

                case Constants.bDATETIME: {//getting fourth electric current value
                    long unixTime = System.currentTimeMillis() / 1000L;
                    lw.appendLog(TAG, "TIME is " + data_int, true);
                    if (Math.abs(data_int - unixTime) < 20 * 60) //if difference is more than 20 minutes
                            lw.appendLog(TAG, "TIME setting is not correct", true);
                    break;
                }

                case Constants.bSENDDPUMPS: {//sending pumps flows
                    ProcedureSettings.getInstance().setStatus(Constants.STATUS_SENDING);
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send FPUMP1FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x01,
                                    intTo4byte(ProcedureSettings.getInstance().getFillPump1Flow()));//first filling pump
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DPUMP1FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x02,
                                    intTo4byte(ProcedureSettings.getInstance().getDialPump1Flow()));//first dialysis pump
                            break;
                        }

                        case (byte)0x03:{
                            lw.appendLog(TAG, "send UFPUMP1FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x03,
                                    intTo4byte(ProcedureSettings.getInstance().getFlushPump1Flow()));//first unfilling pump
                            break;
                        }

                        case (byte)0x11:{
                            lw.appendLog(TAG, "send FPUMP2FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x11,
                                    intTo4byte(ProcedureSettings.getInstance().getFillPump2Flow()));//second filling pump
                            break;
                        }

                        case (byte)0x12:{
                            lw.appendLog(TAG, "send DPUMP2FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x12,
                                    intTo4byte(ProcedureSettings.getInstance().getDialPump2Flow()));//second dialysis pump
                            break;
                        }

                        case (byte)0x13:{
                            lw.appendLog(TAG, "send UFPUMP2FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x13,
                                    intTo4byte(ProcedureSettings.getInstance().getFlushPump2Flow()));//second unfilling pump
                            break;
                        }

                        case (byte)0x21:{
                            lw.appendLog(TAG, "send FPUMP3FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x21,
                                    intTo4byte(ProcedureSettings.getInstance().getFillPump3Flow()));//third filling pump
                            break;
                        }

                        case (byte)0x22:{
                            lw.appendLog(TAG, "send DPUMP3FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x22,
                                    intTo4byte(ProcedureSettings.getInstance().getDialPump3Flow()));//third dialysis pump
                            break;
                        }

                        case (byte)0x23:{
                            lw.appendLog(TAG, "send UFPUMP3FLOW");
                            sendMessageBytes((byte) (Constants.bSENDDPUMPS + (byte) 0x01), (byte) 0x23,
                                    intTo4byte(ProcedureSettings.getInstance().getFlushPump3Flow()));//third unfilling pump
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case Constants.bSENDDPRESS: {//sending values for pressures ranges
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DPRESS1MIN");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x01,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress1Min()));//first min value
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DPRESS1MAX");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x02,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress1Max()));//first max value
                            break;
                        }

                        case (byte)0x11:{
                            lw.appendLog(TAG, "send DPRESS2MIN");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x11,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress2Min()));//second min value
                            break;
                        }

                        case (byte)0x12:{
                            lw.appendLog(TAG, "send DPRESS2MAX");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x12,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress2Max()));//second max value
                            break;
                        }

                        case (byte)0x21:{
                            lw.appendLog(TAG, "send DPRESS3MIN");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x21,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress3Min()));//third min value
                            break;
                        }

                        case (byte)0x22:{
                            lw.appendLog(TAG, "send DPRESS3MAX");
                            sendMessageBytes((byte) (Constants.bSENDDPRESS + (byte) 0x01), (byte) 0x22,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialPress3Max()));//third max value
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case Constants.bSENDDTEMP: {//sending values for temperature range
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DTEMP1MIN ");
                            sendMessageBytes((byte) (Constants.bSENDDTEMP + (byte) 0x01), (byte) 0x01,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialTemp1Min()));//min temp
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DTEMP1MAX ");
                            sendMessageBytes((byte) (Constants.bSENDDTEMP + (byte) 0x01), (byte) 0x02,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialTemp1Max()));//max temp
                            break;
                        }

                        default:
                            break;
                    }
                    break;
                }

                case Constants.bSENDDCOND: {//sending values for conductivity range
                    switch (com2){
                        case (byte)0x01:{
                            lw.appendLog(TAG, "send DCOND1MIN ");
                            sendMessageBytes((byte) (Constants.bSENDDCOND + (byte) 0x01), (byte) 0x01,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialCond1Min()));
                            break;
                        }

                        case (byte)0x02:{
                            lw.appendLog(TAG, "send DCOND1MAX ");
                            sendMessageBytes((byte) (Constants.bSENDDCOND + (byte) 0x01), (byte) 0x02,
                                    floatTo4byte(ProcedureSettings.getInstance().getDialCond1Max()));

                            ProcedureSettings.getInstance().setStatus(Constants.STATUS_NORMAL);
                            break;
                        }

                        default:
                            break;
                    }

                    break;
                }

                case Constants.PE_PRESS1: {//receiving error
                  /*  processError(getResources().getText(R.string.error_press).toString() + "1");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_PRESS2: {//receiving error
                   /* processError(getResources().getText(R.string.error_press).toString() + "2");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_PRESS3: {//receiving error
                  /*  processError(getResources().getText(R.string.error_press).toString() + "3");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_TEMP: {//receiving error
                   /* processError(getResources().getText(R.string.error_temp).toString());
                    errorCount++;*/
                    break;
                }

                case Constants.PE_ELECTRO: {//receiving error
                  /*  processError(getResources().getText(R.string.error_electro).toString());
                    errorCount++;*/
                    break;
                }

                case Constants.PE_EDS1: {//receiving error
                 /*   processError(getResources().getText(R.string.error_eds).toString() + "1");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_EDS2: {//receiving error
                  /*  processError(getResources().getText(R.string.error_eds).toString() + "2");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_EDS3: {//receiving error
                /*    processError(getResources().getText(R.string.error_eds).toString() + "3");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_EDS4: {//receiving error
                   /* processError(getResources().getText(R.string.error_eds).toString() + "4");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_BATT: {//receiving error
                  /*  processError(getResources().getText(R.string.error_batt).toString());
                    errorCount++;*/
                    break;
                }

                case Constants.PE_PUMP1: {//receiving error
                   /* processError(getResources().getText(R.string.error_eds).toString() + "1");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_PUMP2: {//receiving error
                  /*  processError(getResources().getText(R.string.error_eds).toString() + "2");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_PUMP3: {//receiving error
                  /*  processError(getResources().getText(R.string.error_eds).toString() + "3");
                    errorCount++;*/
                    break;
                }

                case Constants.PE_ERROR: {//receiving error
                    /*processError(getResources().getText(R.string.error_unknown).toString());
                    errorCount++;*/
                    break;
                }

                case Constants.bAALARM: {//receiving alarm
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setClass(this, AlertActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("text", "страшная ошибка!!");
                    startActivity(intent);
                    break;
                }

                default:
                    break;
            }

          /*  if(errorCount != 0){
                ProcedureSettings.getInstance().setStatus(Constants.STATUS_ERROR);
            }
            else{
                ProcedureSettings.getInstance().setStatus(Constants.STATUS_NORMAL);
            }*/

        }
    }

    public void startProcedure(int selectedProcedure){
        switch (selectedProcedure) {
            case Constants.PROCEDURE_DIALYSIS: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bDIALYSIS});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.procedure_dialysis).toString(), true);
                break;
            }

            case Constants.PROCEDURE_FILL: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bFILLING});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.procedure_filling).toString(), true);
                break;
            }

            case Constants.PROCEDURE_SHUTDOWN: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bSHUTDOWN});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.procedure_shutdown).toString(), true);
                break;
            }

            case Constants.PROCEDURE_DISINFECTION: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bDISINFECTION});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.procedure_disinfection).toString(), true);
                break;
            }

            case Constants.PROCEDURE_FLUSH: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bFLUSH});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.procedure_flush).toString(), true);
                break;
            }

            case Constants.PROCEDURE_READY: {
                sendMessageBytes(Constants.bSTATUS, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.bPAUSE});
                lw.appendLog(TAG, getResources().getText(R.string.user_switched_to).toString() +
                        getResources().getText(R.string.pause_procedure).toString(), true);
                break;
            }

            default:
                break;
        }
    }

    /**
     * Send packet with only first command
     *
     * @param com1 first command
     */
    void sendMessageBytes(byte com1) {
        byte[] outp = new byte[]{Constants.PACK_START, com1, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.PACK_END};
        mBluetoothLeService.writeSPP(outp);
    }

    /**
     * Send packet with first command and data
     *
     * @param com1 first command
     * @param data data array
     */
    void sendMessageBytes(byte com1, byte[] data) {
        byte[] outp = new byte[]{Constants.PACK_START, com1, (byte) 0x00, data[3], data[2], data[1], data[0], Constants.PACK_END};
        mBluetoothLeService.writeSPP(outp);
    }

    /**
     * Send packet with only two commands
     *
     * @param com1 first command
     * @param com2 second command
     */
    void sendMessageBytes(byte com1, byte com2) {
        byte[] outp = new byte[]{Constants.PACK_START, com1, com2, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, Constants.PACK_END};
        mBluetoothLeService.writeSPP(outp);
    }

    /**
     * Send packet with two commands and data
     *
     * @param com1 first command
     * @param com2 second command
     * @param data data array
     */
    void sendMessageBytes(byte com1, byte com2, byte[] data) {
        byte[] outp = new byte[]{Constants.PACK_START, com1, com2, data[3], data[2], data[1], data[0], Constants.PACK_END};
        mBluetoothLeService.writeSPP(outp);
    }

    void processError(String msg){
        sendNotification(msg);
        lw.appendLog(TAG, msg, true);
        ProcedureSettings.getInstance().setStatus(Constants.STATUS_ERROR);
    }

    /**
     * convert float to 4 byte array
     */
    byte[] floatTo4byte(float fvalue) {//
        return ByteBuffer.allocate(4).putFloat(fvalue).array();
    }

    /**
     * convert int to 4 byte array
     */
    byte[] intTo4byte(int ivalue) {
        return ByteBuffer.allocate(4).putInt(ivalue).array();
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
                sendNotification(getResources().getText(R.string.autoconnect_failed).toString());
                ConnectTryCount = 0;
            }
            AutoconnectHandler.postDelayed(AutoconnectTask, Constants.RECONNECT_INTERVAL_MS);//refresh after RECONNECT_INTERVAL_MS milliseconds
        }
    };
}
