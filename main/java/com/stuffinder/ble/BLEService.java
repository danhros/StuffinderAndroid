package com.stuffinder.ble;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import com.stuffinder.activities.BasicActivity;
import com.stuffinder.data.Tag;

import java.util.UUID;

/**
 * Created by propriétaire on 08/04/2015.
 */
public class BLEService  extends Service{

    private static final int REQUEST_ENABLE_BT = 1;

    private boolean isBLESuported;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    /**
     * the address of the connected bluetooth device.
     */
    private String mBluetoothDeviceAddress;

    /**
     * Gatt connected the bluetooth device which has {@link #mBluetoothDeviceAddress} as address.
     */
    private BluetoothGatt mBluetoothGatt;

    private boolean serviceRunning;

    private Context context;

    public boolean initBLEService(Context context)
    {
        if(serviceRunning) //TODO modify to optimise.
            return true;

        this.context = context;

        isBLESuported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        if(! isBLESuported)
            return false;

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();

        if(bluetoothAdapter == null)
        {
            isBLESuported = false;
            return false;
        }

        if(! enableBluetooth())
            return false;

        // to launch the bluetooth service.
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.startService(gattServiceIntent);

        //TODO add code to lauch service.
        return true;
    }

    public void startBLEService()
    {
        if(isBLESuported && enableBluetooth())
        {
            Intent gattServiceIntent = new Intent(context, BLEService.class);
            context.startService(gattServiceIntent);
        }
    }

    public void connectToService(Context context)
    {
        Intent intent = new Intent(context, BLEService.class);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectToService(Context context)
    {
        Intent intent = new Intent(context, BLEService.class);
        context.unbindService(mServiceConnection);
    }

    public void stopBLEService()
    {
        if(isBLESuported)
        {
            Intent gattServiceIntent = new Intent(context, BLEService.class);
            context.stopService(gattServiceIntent);
        }
    }


    public boolean enableBluetooth()
    {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BasicActivity.getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return bluetoothAdapter.isEnabled();
    }


    //TODO implement these methods.
    public boolean connectToTag(Tag tag){

        return false;
    }

    public boolean disconnectFromTag(){
        return false;
    }

    public boolean enableTagLED(boolean enable) {
        return false;
    }

    public boolean enableTagSound(boolean enable) {
        return false;
    }

    public boolean enableTagBuzzer(boolean enable) {
        return false;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            if (! bluetoothAdapter.isEnabled()) {
                Log.e(getClass().getName(), "Unable to initialize Bluetooth");
                return;
            }
            // Automatically connects to the device upon successful start-up
            // initialization.

//            connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    // constants used for the gatt state.
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";

    public final static String EXTRA_DATA = "EXTRA_DATA";

    /**
     * UUID used to perform a write operation on the connection.
     */
    public final static UUID UUID_BLE_SHIELD_TX = UUID.fromString(RBLGattAttributes.BLE_SHIELD_TX);

    /**
     * UUID used to perform a read operation on the connection.
     */
    public final static UUID UUID_BLE_SHIELD_RX = UUID.fromString(RBLGattAttributes.BLE_SHIELD_RX);

    /**
     *
     */
    public final static UUID UUID_BLE_SHIELD_SERVICE = UUID.fromString(RBLGattAttributes.BLE_SHIELD_SERVICE);

    /**
     * Object to perform bluetooth features by callbacks.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(getClass().getName(), "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(getClass().getName(), "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(getClass().getName(), "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_RSSI, rssi);
            } else {
                Log.w(getClass().getName(), "onReadRemoteRssi received: " + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(getClass().getName(), "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    /**
     * Sends a message to all activities connected on this service to notify them about an action.
     * @param action action to notify
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     *
     * @param action
     * @param rssi
     */
    private void broadcastUpdate(final String action, int rssi) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        sendBroadcast(intent);
    }

    /**
     *
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
            final byte[] rx = characteristic.getValue();
            intent.putExtra(EXTRA_DATA, rx);
        }

        sendBroadcast(intent);
    }


    /**
     * Establish the connection between this service and a bluetooth device.
     * @param address the address of the bluetooth device.
     * @return true on success, false otherwise.
     */
    public boolean connect(String address)
    {
        if (bluetoothAdapter == null || address == null)
        {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        // if the device which has the address address is already connected, do disconnection and establish a new connection.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null)
        {
            Log.d(getClass().getName(), "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        else if(mBluetoothGatt != null) // already connected to another device.
        {
            disconnect();
            close();
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        // to detect the bluetooth device.
        if (device == null) // if the device is not found.
        {
            Log.w(getClass().getName(), "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.

        // to perform the connection.

        Log.d(getClass().getName(), "Trying to create a new connection.");
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;

        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }



    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void readRssi() {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readRemoteRssi();
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized !");
            return;
        }

        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID
                            .fromString(RBLGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public BluetoothGattService getSupportedGattService() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getService(UUID_BLE_SHIELD_SERVICE);
    }



    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.instance;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }


    private static BLEService instance = new BLEService();

    public static BLEService getInstance()
    {
        return instance;
    }
}
