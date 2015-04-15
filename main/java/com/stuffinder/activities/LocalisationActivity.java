package com.stuffinder.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.ble.BLEService;
import com.stuffinder.data.Tag;
import com.stuffinder.exceptions.BLEServiceException;

import java.util.logging.Level;
import java.util.logging.Logger;


public class LocalisationActivity extends BasicActivity {

    private static Tag tagLoc;
    TextView nomObjTextView ;
    TextView positionTextView ;

    private boolean binded;
    private LocationServiceConnection serviceConnection = new LocationServiceConnection();
    private BLEService service;

    private static final int DISCONNECTED = 0;
    private static final int CONNECTING = 1;
    private static final int CONNECTED = 2;
    private static final int DISCONNECTING = 3;

    private int state = 0;

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

        state = DISCONNECTED;
        binded = false;

        connectToBLEService();
    }

    public void retour9 (View view) {
        onBackPressed();
    }

    private boolean ledEnabled = false;
    public void onLed (View view) {
        if(! binded)
            Logger.getLogger(getClass().getName()).log(Level.INFO, "can't perform operation on led because the connection is not done with the BLE service.");
        else if(state == CONNECTED)
        {
            try {
                service.enableTagLED(!ledEnabled);
            } catch (BLEServiceException e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(this, "impossible d'activer la led.", Toast.LENGTH_LONG).show();
    }

    private boolean buzzerEnabled = false;
    public void onSon (View view) {
        if(! binded)
            Logger.getLogger(getClass().getName()).log(Level.INFO, "can't perform operation on sound or buzzer because the connection is not done with the BLE service.");
        else if(state == CONNECTED)
        {
            try {
                service.enableTagBuzzer(!buzzerEnabled);
            } catch (BLEServiceException e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(this, "impossible d'activer le buzzer.", Toast.LENGTH_LONG).show();
    }

    public void retenter (View view) {
        if(! binded)
            Logger.getLogger(getClass().getName()).log(Level.INFO, "can't perform operation on sound or buzzer because the connection is not done with the BLE service.");
        else if(state == CONNECTED || state == DISCONNECTED)
        {
            try {
                service.connectToTag(tagLoc);
            } catch (BLEServiceException e) {
                e.printStackTrace();
            }
        }
        else
            Toast.makeText(this, "nouvelle localisation en cours.", Toast.LENGTH_LONG).show();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        disconnectFromBLEService();
        super.onBackPressed();
    }

    public void connectToBLEService()
    {
        Intent intent = new Intent(this, BLEService.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectFromBLEService()
    {
        try {
            if(state == CONNECTED)
                service.disconnectFromTag();
        } catch (BLEServiceException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "an error has occured in the ble service.", e);
        }
        unbindService(serviceConnection);
    }

    class LocationServiceConnection implements ServiceConnection
    {
        public void onServiceConnected(ComponentName name, IBinder service) {
            binded = true;

            LocalisationActivity.this.service = ((BLEService.LocalBinder) service).getService();
            LocalisationActivity.this.service.setLocationCallback(new MyLocationCallback());
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Connection established with the BLE service.");

            try {
                LocalisationActivity.this.service.connectToTag(tagLoc);
            } catch (BLEServiceException e) { // will not occur.
                e.printStackTrace();
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            binded = false;
            service = null;
        }
    }

    void notifyTagLocated(boolean located)
    {
        if(located)
            positionTextView.setText("est trouvé");
        else
            positionTextView.setText("n'est pas trouvé");
    }

    void notifyTagDistance(int distance)
    {
        switch (distance)
        {
            case BLEService.TRES_PROCHE :
                positionTextView.setText("est proche");
                break;
            case BLEService.MOYENNELENT_PROCHE :
                positionTextView.setText("est moyennement proche.");
                break;
            case BLEService.LOIN :
                positionTextView.setText("est loin");
                break;
            default :
                positionTextView.setText("n'est pas trouvé");
                break;
        }
    }


    class MyLocationCallback extends BLEService.LocationCallback
    {
        @Override
        public void onTagConnected(Tag tag) {
            state = CONNECTED;
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Connection established with the tag " + tagLoc);
            notifyTagLocated(true);
            try {
                service.getLocatedTagDistance();
            } catch (BLEServiceException e) { // will normally never occur.
                e.printStackTrace();
            }
        }

        @Override
        public void onTagConnecting(Tag tag) {
            state = CONNECTING;
        }

        @Override
        public void onTagDisconnected(Tag tag) {
            state = DISCONNECTED;
        }

        @Override
        public void onTagDisconnecting(Tag tag) {
            if(state == CONNECTING)
                notifyTagLocated(false);

            state = DISCONNECTING;
        }

        @Override
        public void onTagNotFound() {
            notifyTagLocated(false);
        }

        @Override
        public void onDistanceMeasured(int distance) {
            notifyTagDistance(distance);
        }

        @Override
        public void onLEDEnabled(boolean enabled) {
            ledEnabled = enabled;

            if(enabled)
                Toast.makeText(LocalisationActivity.this, "LED activée.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(LocalisationActivity.this, "LED désactivée.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBuzzerEnabled(boolean enabled) {
            buzzerEnabled = enabled;

            if(enabled)
                Toast.makeText(LocalisationActivity.this, "Buzzer activée.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(LocalisationActivity.this, "Buzzer désactivée.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSoundEnabled(boolean enabled) { // not implemented at the moment.

        }
    }
}
