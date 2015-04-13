package com.stuffinder.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Tag;
import com.stuffinder.exceptions.TagNotDetectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;


public class LocalisationActivity extends Activity {

    // fields used to BLE tags detection.
    private String UUID_Tag = "F9:1F:24:D3:1B:D4" ;//adresse du TAG actuel

    private BluetoothAdapter mBluetoothAdapter;
    public static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private static HashMap<BluetoothDevice,Integer> listRssi = new HashMap<BluetoothDevice, Integer>();
    private static final long SCAN_PERIOD = 5000; //durée d'un scan


    private Boolean presence = false;
    private int puissance;


    private static Tag tagLoc;
    TextView nomObjTextView ;
    TextView positionTextView ;


    public static void ChangeTag(Tag tag)
    {
        tagLoc = tag ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localisation);

        nomObjTextView = (TextView)findViewById(R.id.textViewNomObj);
        positionTextView = (TextView)findViewById(R.id.textViewPosition);

        nomObjTextView.setText(tagLoc.getObjectName());


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { //alerte support BLE
            Toast.makeText(this, "BLE feature not supported.", Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); //initialisation du bluetooth manager
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) { //activation du ble sur le terminal
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);

            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                finish();
                return;
            }
        }

        if(isConnected(tagLoc.getUid())){
            try {
                if (distance(tagLoc.getUid())==1)
                {
                    positionTextView.setText("est tres proche");
                }
                else {
                    if (distance(tagLoc.getUid())==2)
                        positionTextView.setText("est moyennement proche");

                    else
                        positionTextView.setText("est loin");
                }

            } catch (TagNotDetectedException e) {
                Toast.makeText(this, "Une erreur anormale est survenue. Veuillez relancer l'application.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            positionTextView.setText("n'a pas été localisé.");
        }
    }

    public void retour9 (View view) {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_localisation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // methods to manage BLE tags detection.

    public boolean isConnected(String id_Tag){ //"F9:1F:24:D3:1B:D4"

        presence = false;

        scanLeDevice();


        for (int i=0; i < mDevices.size(); i++) {
            if (mDevices.get(i).getAddress().equals(id_Tag)) {
                presence = true;
            }
        }




        return presence;
    }

    public int distance(String id_Tag) //"F9:1F:24:D3:1B:D4"
            throws TagNotDetectedException {

        int indicePuissance =3;

        if (!presence)
            throw new TagNotDetectedException();

        else {
            for (int i = 0; i < mDevices.size(); i++) {
                if (mDevices.get(i).getAddress().equals(id_Tag)) {
                    puissance = Math.abs(listRssi.get(mDevices.get(i)).intValue());
                }
            }


            if (puissance < 40)
                indicePuissance = 1;

            else if (puissance < 70 && puissance >= 40)
                indicePuissance = 2;
        }

        listRssi.clear();
        mDevices.clear();

        return indicePuissance;
    }

    private void scanLeDevice() { //timer pour le scan




        mBluetoothAdapter.startLeScan(mLeScanCallback);

        try {
            Thread.sleep(SCAN_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mBluetoothAdapter.stopLeScan(mLeScanCallback);


    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            if (device != null) {
                if (mDevices.indexOf(device) == -1)
                    mDevices.add(device);
                listRssi.put(device, new Integer(rssi));

                /*if (device.getAddress().equals(UUID_Tag)) {
                    presence = true;
                    puissance = rssi;
                } */
            }

        }
    };

}
