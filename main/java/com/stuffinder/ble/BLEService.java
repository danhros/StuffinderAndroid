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
import com.stuffinder.exceptions.BLEServiceException;

import java.util.UUID;

/**
 * Created by propriétaire on 08/04/2015.
 */
public class BLEService  extends Service{

    private static final int REQUEST_ENABLE_BT = 1;

    private boolean isBLESuported;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


    public static void startBLEService(Context context)
    {
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.startService(gattServiceIntent);
    }

    public static BLEServiceConnection connectToService(Context context)
    {
        Intent intent = new Intent(context, BLEService.class);
        BLEServiceConnection bleServiceConnection = new BLEServiceConnection();

        context.bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);

        return bleServiceConnection;
    }

    public static void disconnectToService(Context context, BLEServiceConnection bleServiceConnection)
    {
        context.unbindService(bleServiceConnection);
    }

    public static void stopBLEService(Context context)
    {
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.stopService(gattServiceIntent);
    }

    static class BLEServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

//            if (! bluetoothAdapter.isEnabled()) {
//                Log.e(getClass().getName(), "Unable to initialize Bluetooth");
//                return;
//            }
            // Automatically connects to the device upon successful start-up
            // initialization.

//            connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }

    private void verifyIfBLESupported() throws BLEServiceException
    {
        if(! isBLESuported)
        {
            Log.e(getClass().getName(), "BLE feature not supported.");
            throw new BLEServiceException("BLE not supported.");
        }
    }

    @Override
    public void onCreate()
    {
        isBLESuported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        if(! isBLESuported)
            return;

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();

        if(bluetoothAdapter == null)
        {
            isBLESuported = false;
            return;
        }

        if(! enableBluetooth())
            return;

        //TODO add code to launch the thread for surveillance.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        try {
            disconnectFromTag();
        } catch (BLEServiceException e) {
            e.printStackTrace();
        }
        //TODO add code to properly stop surveillance thread and remove all bluetooth connections.
    }



    private boolean enableBluetooth()
    {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BasicActivity.getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        return bluetoothAdapter.isEnabled();
    }



    /**
     * Bluetooth gatt used to communicate with the located tag.
     */
    private BluetoothGatt locationBluetoothGatt;
    private String tagAddress;

    //methods used for location.

    //TODO implement these methods.

    /**
     * to perform connection with a tag.
     * @param tag
     * @return true if the connection is established, false if the tag is not found.
     * @throws BLEServiceException
     * @throws IllegalArgumentException
     */
    public boolean connectToTag(Tag tag) throws BLEServiceException, IllegalArgumentException
    {
        verifyIfBLESupported();

        if(locationBluetoothGatt != null)
        {
            disconnect(locationBluetoothGatt);
            close(locationBluetoothGatt);
        }

        locationBluetoothGatt = connect(tag.getUid());


        if(locationBluetoothGatt == null)
            return false;

        tagAddress = tag.getUid();
        return true;
    }

    /**
     * To disconnect the location service with the current tag.
     */
    public void disconnectFromTag() throws BLEServiceException
    {
        verifyIfBLESupported();

        if(locationBluetoothGatt != null)
        {
            disconnect(locationBluetoothGatt);
            close(locationBluetoothGatt);
            locationBluetoothGatt = null;
            tagAddress = null;
        }
    }

    public int getLocatedTagDistance()throws BLEServiceException
    {
        verifyIfBLESupported();

        return 0;
    }

    public boolean enableTagLED(boolean enable) throws BLEServiceException
    {
        verifyIfBLESupported();

        return false;
    }

    public boolean enableTagSound(boolean enable) throws BLEServiceException
    {
        verifyIfBLESupported();

        return false;
    }

    public boolean enableTagBuzzer(boolean enable) throws BLEServiceException
    {
        verifyIfBLESupported();

        return false;
    }




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
     * Gatt connected the bluetooth device.
     */
    private BluetoothGatt mBluetoothGatt;


    /**
     * Establish the connection between this service and a bluetooth device.
     * @param address the address of the bluetooth device.
     * @return the gatt on success, null if the device is not found.
     */
    private BluetoothGatt connect(String address) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (address == null || address.length() == 0)
        {
            Log.w(getClass().getName(), "BluetoothAdapter not initialized or unspecified address.");
            throw new IllegalArgumentException("device address can't be null.");
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
        Log.d(getClass().getName(), "Trying to create a new connection with the device which has address \"" + address + "\".");
        BluetoothGatt bluetoothGatt = device.connectGatt(this, false, mGattCallback);

        return bluetoothGatt;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     * @param bluetoothGatt gatt to use to do disconnection.
     * @throws BLEServiceException
     */
    private void disconnect(BluetoothGatt bluetoothGatt) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null) {
            Log.w(getClass().getName(), "bluetooth gatt can't be null.");
            throw new IllegalArgumentException("parameter must be not null.");
        }
        bluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     * @param bluetoothGatt gatt to close to release resource properly.
     * @throws BLEServiceException
     */
    private void close(BluetoothGatt bluetoothGatt) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null) {
            Log.w(getClass().getName(), "bluetooth gatt can't be null.");
            throw new IllegalArgumentException("parameter must be not null.");
        }

        bluetoothGatt.close();
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
    public void readCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void readRssi(BluetoothGatt bluetoothGatt) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.readRemoteRssi();
    }

    public void writeCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, boolean enabled) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (UUID_BLE_SHIELD_RX.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(RBLGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public BluetoothGattService getSupportedGattService(BluetoothGatt bluetoothGatt)
    {
        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        return bluetoothGatt.getService(UUID_BLE_SHIELD_SERVICE);
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
//        close();
        return super.onUnbind(intent);
    }


    private static BLEService instance = new BLEService();

    public static BLEService getInstance()
    {
        return instance;
    }
}
