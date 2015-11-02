package com.example.zhilo.kidneymonitor2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    //Initialisation of LogWriter
    private static final String TAG = "BluetoothLeService";
    private LogWriter lw = new LogWriter();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    static int mConnectionState = Constants.STATE_DISCONNECTED;

    private static byte[] incomingBuffer = new byte[128];
    private static int incomingBufferInd = 0;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = Constants.ACTION_GATT_CONNECTED;
                mConnectionState = Constants.STATE_CONNECTED;
                broadcastUpdate(intentAction);
                lw.appendLog(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                lw.appendLog(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = Constants.ACTION_GATT_DISCONNECTED;
                mConnectionState = Constants.STATE_DISCONNECTED;
                lw.appendLog(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(getSupportedGattServices());
            }/* else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }*/
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    /**
     * Iterate through the GATT services and characteristics, and set indication for needed ones.
     *
     * @param gattServices List of GATT services.
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.equals(Constants.UUID_SPP_DATA.toString())) {
                    setCharacteristicIndication(gattCharacteristic, true);
                    setCharacteristicNotification(gattCharacteristic, true);
                }
            }
        }
    }

    /**
     * Sends broadcast message
     *
     * @param action Broadcast action message
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Sends broadcast message, and if it's from SPP Data characteristic - attaches received package
     *
     * @param action         Broadcast action message
     * @param characteristic Characteristic to
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (Constants.UUID_SPP_DATA.equals(characteristic.getUuid())) {
            final byte[] pack = processInput(characteristic.getValue());
            if (pack != null)
                intent.putExtra(Constants.EXTRA_DATA, pack);
        }
        sendBroadcast(intent);
    }

    private byte[] processInput(byte[] input) {
        byte[] pack = null;

        for (int i = 0; i < input.length; i++) {
            incomingBuffer[incomingBufferInd] = input[i];
            incomingBufferInd++;
        }

        if ((arrayIndexOf(incomingBuffer, (byte) 0x55) != -1) &&
                (arrayIndexOf(incomingBuffer, (byte) 0xAA) != -1)) {
            int start = arrayIndexOf(incomingBuffer, (byte) 0x55);
            int end = arrayIndexOf(incomingBuffer, (byte) 0xAA) + 1;
            pack = Arrays.copyOfRange(incomingBuffer, start, end);
            incomingBufferInd -= pack.length;
            byte[] b = Arrays.copyOfRange(incomingBuffer, end, incomingBuffer.length);
            Arrays.fill(incomingBuffer, (byte) 0x00);
            for (int i = 0; i < b.length; i++) {
                incomingBuffer[i] = b[i];
            }
        }
        return pack;
    }

    /**
     * Returns index of first found byte in byte array, returns -1 if not found.
     *
     * @param inp  Byte array to search in
     * @param what Byte to find index of
     * @return Index of first found byte, -1 if not found
     */
    static int arrayIndexOf(byte[] inp, byte what) {
        for (int i = 0; i < inp.length; i++)
            if (inp[i] == what)
                return i;
        return -1;
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                lw.appendLog(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            lw.appendLog(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            lw.appendLog(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            lw.appendLog(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = Constants.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            lw.appendLog(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        lw.appendLog(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = Constants.STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            lw.appendLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            lw.appendLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a given characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            lw.appendLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Enables or disables indication on a given characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable indication.  False otherwise.
     */
    public void setCharacteristicIndication(BluetoothGattCharacteristic characteristic,
                                            boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            lw.appendLog(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattDescriptor desc = characteristic.getDescriptor(Constants.UUID_CONFIG_DESCRIPTOR);
        if (enabled)
            desc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        else
            desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(desc);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
