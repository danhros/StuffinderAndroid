package com.stuffinder.ble;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.exceptions.BLEServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by propriétaire on 08/04/2015.
 */
public class BLEService  extends Service{

    private static final int REQUEST_ENABLE_BT = 1;

    private boolean isBLESupported;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


    public static void startBLEService(Context context)
    {
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.startService(gattServiceIntent);
    }


    public static void stopBLEService(Context context)
    {
        Intent gattServiceIntent = new Intent(context, BLEService.class);
        context.stopService(gattServiceIntent);
    }

    private void verifyIfBLESupported() throws BLEServiceException
    {
        if(!isBLESupported)
        {
            Log.e(getClass().getName(), "BLE feature not supported.");
            throw new BLEServiceException("BLE not supported.");
        }
    }

    @Override
    public void onCreate()
    {
        isBLESupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        if(!isBLESupported)
            return;

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();

        if(bluetoothAdapter == null)
        {
            isBLESupported = false;
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
        try {
            disableSurveillance();
        } catch (BLEServiceException e) {
            e.printStackTrace();
        }
    }



    private boolean enableBluetooth()
    {
        return true;
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            BasicActivity.getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
//
//        return bluetoothAdapter.isEnabled();
    }



    /**
     * Bluetooth gatt used to communicate with the located tag.
     */
    private BluetoothGatt locationBluetoothGatt;
    private String tagAddress;

    public final static int ALLUMER_LED = 1;
    public final static int ETEINDRE_LED = 2;
    public final static int ALLUMER_SON = 3;
    public final static int ALLUMER_MOTEUR = 4;
    public final static int ETEINDRE_MOTEUR = 5;

    public final static int TRES_PROCHE=1;
    public final static int MOYENNELENT_PROCHE=2;
    public final static int LOIN=3;


    //methods used for location.


    private LocationCallback locationCallback;

    public void setLocationCallback(LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
    }

    /**
     * to perform connection with a tag.
     * @param tag
     * @return true if the connection is established, false if the tag is not found.
     * @throws BLEServiceException
     * @throws IllegalArgumentException
     */
    public void connectToTag(Tag tag) throws BLEServiceException, IllegalArgumentException
    {
        verifyIfBLESupported();

        if(tag == null)
            throw new IllegalArgumentException("tag parameter can't be null");

        if(locationBluetoothGatt != null)
        {
            disconnect(locationBluetoothGatt);
            close(locationBluetoothGatt);
        }

        if(locationCallback != null)
            locationCallback.onTagConnecting(tag);

        locationBluetoothGatt = connect(tag.getUid(), LocationGattCallback);


        if(locationBluetoothGatt == null)
        {
            if(locationCallback != null)
                locationCallback.onTagConnected(tag);
                locationCallback.onTagNotFound();
        }
        else
        {
            tagAddress = tag.getUid();
        }
    }

    /**
     * To disconnect the location service with the current tag.
     */
    public void disconnectFromTag() throws BLEServiceException
    {
        verifyIfBLESupported();

        if(locationBluetoothGatt != null)
        {
            locationCallback.onTagDisconnecting(null);
            disconnect(locationBluetoothGatt);
            close(locationBluetoothGatt);
            locationCallback.onTagDisconnected(null);
            locationBluetoothGatt = null;
            tagAddress = null;
        }
    }

    public void getLocatedTagDistance()throws BLEServiceException
    {
        verifyIfBLESupported();

        readRssi(locationBluetoothGatt);
        //les données sont reçus dans le broadcast update
    }

    private int getDistance(int rssi){

        rssi = rssi < 0 ? -rssi : rssi;

        if (rssi < 40)
            return TRES_PROCHE;

        else if (rssi < 70 && rssi >= 40)
            return MOYENNELENT_PROCHE;

        return LOIN;
    }

    public boolean enableTagLED(boolean enable) throws BLEServiceException
    {
        verifyIfBLESupported();

        if(enable) {
            if (!remote(ALLUMER_LED))
                return false;
        }
        else {
            if (!remote(ETEINDRE_LED))
                return false;
        }
        return true;
    }

    public boolean enableTagSound(boolean enable) throws BLEServiceException //joue une mélodie d'environ 6s avec le piezo
    {
        verifyIfBLESupported();

        if(enable) {
            if (!remote(ALLUMER_SON))
                return false;
        }

        return true;
    }

    public boolean enableTagBuzzer(boolean enable) throws BLEServiceException
    {
        verifyIfBLESupported();
        if(enable) {
            if (!remote(ALLUMER_MOTEUR))
                return false;
        }
        else {
            if (!remote(ETEINDRE_MOTEUR))
                return false;
        }
        return true;
    }




    // constants used for the gatt state.
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI = "ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";

    public final static String MISSING_TAGS =" MISSING_TAGS";

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
    private final BluetoothGattCallback LocationGattCallback = new LocationBluetoothGattCallback();

    class LocationBluetoothGattCallback extends BluetoothGattCallback
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
//                intentAction = ACTION_GATT_CONNECTED;
//                broadcastUpdate(intentAction);
                Log.i(getClass().getName(), "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(getClass().getName(), "Attempting to start service discovery:"
                        + gatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
//                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(getClass().getName(), "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);

                if(locationCallback != null)
                    locationCallback.onTagDisconnected(null);
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_RSSI, rssi);

                Log.d(getClass().getName(), "RSSI read for the located tag is done successfully.");
                if(locationCallback != null)
                    locationCallback.onDistanceMeasured(getDistance(rssi));
            } else {
                Log.w(getClass().getName(), "onReadRemoteRssi received: " + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                getGattService(getSupportedGattService(gatt));

                if(locationCallback != null && getSupportedGattService(gatt) != null)
                    locationCallback.onTagConnected(null);
                else if(locationCallback != null)
                {
                    locationCallback.onTagConnected(null);
                    locationCallback.onTagNotFound();
                }

            }
            else
            {
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
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                byte data[] = characteristic.getValue();

                if(data != null && data.length > 0)
                {
                    switch(data[0])
                    {
                        case ALLUMER_LED:
                            locationCallback.onLEDEnabled(true);
                        break;
                        case ETEINDRE_LED:
                            locationCallback.onLEDEnabled(false);
                        break;
                        case ALLUMER_MOTEUR:
                            locationCallback.onBuzzerEnabled(true);
                        break;
                        case ETEINDRE_MOTEUR:
                            locationCallback.onBuzzerEnabled(false);
                        break;
                        case ALLUMER_SON:
                            locationCallback.onSoundEnabled(true);
                        break;
                    }
                }
                else
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "onCharacteristicWrite : no data found in characterictic object.");
            }
            else
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "onCharacteristicWrite : write operation seems to has failed.");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }



// partie surveillance.

    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private String braceletUID;
    private BluetoothGatt braceletBluetoothGatt;

    public void connectToBracelet(String braceletUID) throws BLEServiceException, IllegalArgumentException
    {
        verifyIfBLESupported();

        if(braceletUID == null)
            throw new IllegalArgumentException("tag parameter can't be null");

        if(braceletBluetoothGatt != null)
        {
            disconnect(braceletBluetoothGatt);
            close(braceletBluetoothGatt);
        }

        if(surveillanceCallback != null)
            surveillanceCallback.onSurveilllanceStarting();


        braceletBluetoothGatt = connect(braceletUID, braceletBluetoothGattCallback);


        if(braceletBluetoothGatt == null && surveillanceCallback != null)
        {
            surveillanceCallback.onBraceletNotFound(braceletUID);
            surveillanceCallback.onSurveilllanceStopped();
        }
    }

    /**
     * To disconnect the location service with the current tag.
     */
    private void disconnectFromBracelet() throws BLEServiceException
    {
        if(braceletBluetoothGatt != null)
        {
            disconnect(braceletBluetoothGatt);
            close(braceletBluetoothGatt);
            braceletBluetoothGatt = null;
        }
    }


    private BraceletCommunicationThread braceletCommunicationThread;
    private TagsSurveillanceThread tagsSurveillanceThread;

    private int surveillanceState = SURVEILLANCE_STOPPED;

    public static final int SURVEILLANCE_STOPPING = 0;
    public static final int SURVEILLANCE_STARTING = 1;
    public static final int SURVEILLANCE_STARTED = 2;
    public static final int SURVEILLANCE_STOPPED = 3;

    public int getSurveillanceState()
    {
        return surveillanceState;
    }


    public void enableSurveillance(final String braceletUID, final Profile profile) throws BLEServiceException
    {
        verifyIfBLESupported();

        if(surveillanceState == SURVEILLANCE_STARTING || surveillanceState == SURVEILLANCE_STARTED)
        {
            disableSurveillance();

            while(surveillanceState != SURVEILLANCE_STOPPED)
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        if(profile.getTags().size() == 0)
            throw new IllegalArgumentException("The profile to be activated must have one tag at least.");

        surveillanceState = SURVEILLANCE_STARTING;

        braceletCommunicationThread = new BraceletCommunicationThread(braceletUID);
        tagsSurveillanceThread = new TagsSurveillanceThread(profile.getTags());

        braceletCommunicationThread.start();
        tagsSurveillanceThread.start();

        if(surveillanceCallback != null)
            surveillanceCallback.onSurveilllanceStarting();
    }

    public void disableSurveillance() throws BLEServiceException
    {
        verifyIfBLESupported();

        if(surveillanceState == SURVEILLANCE_STARTING || surveillanceState == SURVEILLANCE_STARTED)
        {
            surveillanceState = SURVEILLANCE_STOPPING;
            tagsSurveillanceThread.setContinueTagsSurveillance(false);
            braceletCommunicationThread.stopRunning();

            if(surveillanceCallback != null)
                surveillanceCallback.onSurveilllanceStopping();
        }
    }


    private class BraceletCommunicationThread extends Thread
    {
        private boolean continueRunning;
        private String braceletUID;

        private BraceletCommunicationThread(String braceletUID) {
            super();

            if(braceletUID == null || braceletUID.length() == 0)
                throw new IllegalArgumentException("bracelet UID parameter can't be null.");

            this.braceletUID = braceletUID;
        }

        public boolean isContinueRunning() {
            return continueRunning;
        }

        public void stopRunning() {
            this.continueRunning = false;
        }

        @Override
        public void run()
        {
            continueRunning = true;
            while (continueRunning) {
                try {
                    connectToBracelet(braceletUID);
                    try {
                        Thread.sleep(5000); //delai entre chaque annonce au bracelet
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    disconnectFromBracelet();
                } catch (BLEServiceException e) {
                    e.printStackTrace();
                }

                try {
                    if(continueRunning)
                        Thread.sleep(5000); //delai entre chaque annonce au bracelet
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int notificationId = 0;
    HashMap<Tag, Integer> tagNotificationIndexes = new HashMap<>();
    HashMap<Tag, Integer> tagNotificationNumbers = new HashMap<>();

    private void notifyMissingTag(Tag tag)
    {
        Integer index = tagNotificationIndexes.get(tag);

        if(index == null)
        {
            index = notificationId++;
            tagNotificationIndexes.put(tag, index);
        }

        Integer number = tagNotificationNumbers.get(tag);
        number = number == null ? 1 : number + 1;

        tagNotificationNumbers.put(tag, number);


        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Un object est manquant")
                        .setContentText("Votre objet " + tag.getObjectName() + " est perdu depuis " + 15 * number + " secondes.");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(index, mBuilder.build());
    }

    private void notifyTagFoundAgain(Tag tag)
    {
        Integer number = tagNotificationNumbers.get(tag);

        if(number != null)
        {
            Integer index = tagNotificationIndexes.get(tag);
            tagNotificationNumbers.remove(tag);


            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Un object est manquant")
                            .setContentText("Votre objet" + tag.getObjectName() + " a été retrouvé.");

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(index, mBuilder.build());
        }
    }


    private class TagsSurveillanceThread extends Thread
    {
        private boolean continueTagsSurveillance;
        private List<Tag> tagsToSurveil;

        private TagsSurveillanceThread(List<Tag> tagsToSurveil) {
            super();
            continueTagsSurveillance = false;
            this.tagsToSurveil = new LinkedList<>(tagsToSurveil);
        }

        public boolean isContinueTagsSurveillance() {
            return continueTagsSurveillance;
        }

        public void setContinueTagsSurveillance(boolean continueTagsSurveillance) {
            this.continueTagsSurveillance = continueTagsSurveillance;
        }

        @Override
        public void run()
        {
            boolean firstLoop = true;
            continueTagsSurveillance = true;
            tagNotificationNumbers.clear();

            while (continueTagsSurveillance) {
                mDevices.clear();
                bluetoothAdapter.startLeScan(mLeScanCallback);

                try {
                    Thread.sleep(5000); //durée du scan
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                bluetoothAdapter.stopLeScan(mLeScanCallback);

                if(continueTagsSurveillance)
                {
                    ArrayList<Tag> missingTags = new ArrayList<>();
                    int i = 0;

                    for (Tag t : tagsToSurveil) {
                        i = 0;
                        for (BluetoothDevice mdevice : mDevices)
                            if (mdevice.getAddress().equals(t.getUid())) {
                                i++;
                                break;
                            }

                        if (i == 0)
                            missingTags.add(t);
                        else
                            notifyTagFoundAgain(t); // will notify if the tag was lost and is found again.
                    }

                    if(firstLoop)
                    {
                        firstLoop = false;
                        if(surveillanceCallback != null && missingTags.size() > 0)
                        {
                            surveillanceState = SURVEILLANCE_STOPPED;
                            surveillanceCallback.onSurveillanceFailed(missingTags);
                            continueTagsSurveillance = false;
                        }
                        else if(surveillanceCallback != null)
                        {
                            surveillanceState = SURVEILLANCE_STARTED;
                            surveillanceCallback.onSurveilllanceStarted();
                        }
                    }
                    else
                    {
                        for(Tag tag : missingTags)
                            notifyMissingTag(tag);
                    }
//
//                final Intent intent = new Intent(MISSING_TAGS);
//                intent.putExtra(EXTRA_DATA, missingTags); //envoie la liste des tag manquants
//                sendBroadcast(intent);

                    try {
                        Thread.sleep(10000); //délai entre chaque scan
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            surveillanceState = SURVEILLANCE_STOPPED;

            if(surveillanceCallback != null)
                surveillanceCallback.onSurveilllanceStopped();
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (device != null && rssi > - 70) {

                mDevices.add(device);
            }

        }
    };

    BraceletBluetoothGattCallback braceletBluetoothGattCallback = new BraceletBluetoothGattCallback();

    //TODO implement callback methods for surveillance with bracelet.
    class BraceletBluetoothGattCallback extends BluetoothGattCallback
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
//                intentAction = ACTION_GATT_CONNECTED;
//                broadcastUpdate(intentAction);
                Log.i(getClass().getName(), "Connected to bracelet GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(getClass().getName(), "Attempting to start service discovery:"
                        + gatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
//                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(getClass().getName(), "Disconnected from bracelet GATT server.");
//                broadcastUpdate(intentAction);

                if(surveillanceCallback != null)
                    surveillanceCallback.onBraceletDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                getGattService(getSupportedGattService(gatt));

                if(surveillanceCallback != null && getSupportedGattService(gatt) != null)
                    try {
                        disconnectFromBracelet();
                    } catch (BLEServiceException e) {// will never occur.
                        e.printStackTrace();
                    }
                else if(surveillanceCallback != null)
                {
                }

            }
            else
            {
                Log.w(getClass().getName(), "onServicesDiscovered received: " + status);
            }
        }
    };

    private SurveillanceCallback surveillanceCallback;

    public static abstract class SurveillanceCallback
    {
        public abstract void onSurveilllanceStarting();
        public abstract void onSurveilllanceStarted();

        public abstract void onSurveilllanceStopping();
        public abstract void onSurveilllanceStopped();


        public abstract void onSurveillanceFailed(String message);

        public abstract void onSurveillanceFailed(List<Tag> tagsNotFound);

        public abstract void onBraceletNotFound(String braceletUID);

        public abstract void onBraceletConnected();

        public abstract void onBraceletConnectionFailed();

        public abstract void onBraceletDisconnected();
    }

    public void setSurveillanceCallback(SurveillanceCallback surveillanceCallback)
    {
        this.surveillanceCallback = surveillanceCallback;
    }

    // mutual code, used for surveillance and location.


    /**
     * Sends a message to all activities connected on this service to notify them about an action.
     * @param action action to notify
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);

//        if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //ajout bruno : récupère les services BLE Shield Service
//            getGattService(getSupportedGattService(this.mBluetoothGatt));
//        }
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
    private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();


    /**
     * Establish the connection between this service and a bluetooth device.
     * @param address the address of the bluetooth device.
     * @return the gatt on success, null if the device is not found.
     */
    private BluetoothGatt connect(String address, BluetoothGattCallback bluetoothGattCallback) throws BLEServiceException
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
        BluetoothGatt bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

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


    private boolean remote(int action) throws BLEServiceException{

        BluetoothGattCharacteristic characteristic = map.get(UUID_BLE_SHIELD_TX);
        if (characteristic != null) {

            byte[] tx = new byte[1];
            tx[0] = (byte) action;
            characteristic.setValue(tx);
            try {
                writeCharacteristic(locationBluetoothGatt, characteristic);
                return true;
            } catch (BLEServiceException e) {
                e.printStackTrace();
            }
        }
        return false;
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
    private void readCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.readCharacteristic(characteristic);
    }

    private void readRssi(BluetoothGatt bluetoothGatt) throws BLEServiceException
    {
        verifyIfBLESupported();

        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        bluetoothGatt.readRemoteRssi();
    }

    private void writeCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) throws BLEServiceException
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
    private void setCharacteristicNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, boolean enabled) throws BLEServiceException
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
    private BluetoothGattService getSupportedGattService(BluetoothGatt bluetoothGatt)
    {
        if (bluetoothGatt == null)
            throw new IllegalArgumentException("bluetooth gatt can't be null");

        return bluetoothGatt.getService(UUID_BLE_SHIELD_SERVICE);
    }

    private void getGattService(BluetoothGattService gattService) { //les notifications de RX ne sont pas activées
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID_BLE_SHIELD_TX);

        map.put(characteristic.getUuid(), characteristic);

        BluetoothGattCharacteristic characteristicRx = gattService.getCharacteristic(UUID_BLE_SHIELD_RX);
    }



    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
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


    public static abstract class LocationCallback
    {
        public abstract void onTagConnected(Tag tag);
        public abstract void onTagConnecting(Tag tag);
        public abstract void onTagDisconnected(Tag tag);
        public abstract void onTagDisconnecting(Tag tag);

        public abstract void onTagNotFound();

        public abstract void onDistanceMeasured(int distance);

        public abstract void onLEDEnabled(boolean enabled);
        public abstract void onBuzzerEnabled(boolean enabled);
        public abstract void onSoundEnabled(boolean enabled);
    }
}
