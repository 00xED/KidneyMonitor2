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
        unbindService(BLEServiceConnection);
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
                lw.appendLog(TAG, "BLE connected");
            } else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                BLEConnected = false;
                lw.appendLog(TAG, "BLE disconnected");
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.hasExtra(Constants.EXTRA_DATA)) {
                    byte[] pack = intent.getByteArrayExtra(Constants.EXTRA_DATA);
                    lw.appendLog(TAG, bytesToHex(pack));
                }
            }
        }
    };

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
                unbindService(BLEServiceConnection);
                mBluetoothLeService = null;
            } else {

            }
        }
    };

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
                    ConnectTryCount <= 10) {//if waiting for connection - try to connect to saved device
                String address = sPref.getString(Constants.SETTINGS_ADDRESS, "00:00:00:00:00:00");
                if (!"00:00:00:00:00:00".equals(address)) {
                    Intent intentValues = new Intent(Constants.CONNECTIONSERVICE_ACTION);
                    intentValues.putExtra(Constants.CONNECTIONSERVICE_TASK, Constants.CONNECTIONSERVICE_ACTION_START_BLE_SERVICE);
                    sendBroadcast(intentValues);
                    ConnectTryCount++;
                }
            }
            if (ConnectTryCount > 10 && ConnectTryCount < 20) {
                SharedPreferences.Editor ed = sPref.edit(); //Setting for preference editing
                ed.putBoolean(Constants.SETTINGS_AUTOCONNECT, false);
                ed.apply();
                sendNotification("autoconnect_failed");
                ConnectTryCount = 20;
            }
            AutoconnectHandler.postDelayed(AutoconnectTask, 5000);//refresh after one second
        }
    };

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
}
