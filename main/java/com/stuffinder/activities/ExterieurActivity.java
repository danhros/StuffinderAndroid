package com.stuffinder.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.ble.BLEService;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.BLEServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExterieurActivity extends BasicActivity {

        private ListView listView = null ;
        private static List<Profile> listProfiles = new ArrayList<>();
        private List<String> listNames = new ArrayList<>();




    public static void ChangeListProfiles ( List<Profile> list) {        // Méthode qui agit sur la variable de classe listProfiles, elle met à jour les données de la liste des profils
    listProfiles.clear();                                               // Enelève les anciens profils de la liste
    listProfiles.addAll(list);                                          // Ajoute les profils à jour
    Collections.sort(listProfiles, new Comparator<Profile>() {          // Classe par ordre alphabétique
            @Override
            public int compare(Profile lhs, Profile rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    public void retour(View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_exterieur);

        listView = (ListView) findViewById(R.id.listExt);


        for (int i = 0 ; i < listProfiles.size(); i ++  ) { listNames.add(listProfiles.get(i).getName()); }

        ArrayAdapter<String> profileArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        profileArrayAdapter.addAll(listNames);

       listView.setAdapter(profileArrayAdapter);
       listView.setItemChecked(0,true);
       listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        connectToBLEService();
    }



    public void onEnableSurveillance(View view)
    {
        if(! binded)
            Toast.makeText(this, "Activation impossible pour le moment.", Toast.LENGTH_LONG).show();
        else
        {
            Profile selectedProfile = listProfiles.get(listView.getCheckedItemPosition());
            try {
                service.enableSurveillance(EngineServiceProvider.getEngineService().getCurrentAccount().getBraceletUID(), selectedProfile);
            } catch (BLEServiceException e) {
                e.printStackTrace();
                Toast.makeText(this, "Une erreur est survenue (profil non activé).", Toast.LENGTH_LONG).show();
            } catch (NotAuthenticatedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onDisableSurveillance(View view)
    {
        if(! binded)
            Toast.makeText(this, "Désactivation impossible pour le moment.", Toast.LENGTH_LONG).show();
        else
        {
            try {
                service.disableSurveillance();
            } catch (BLEServiceException e) {
                e.printStackTrace();
                Toast.makeText(this, "Une erreur est survenue (profil non activé).", Toast.LENGTH_LONG).show();
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exterieur, menu);
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



    private boolean binded;
    private SurveillanceServiceConnection serviceConnection;
    private BLEService service;


    public void connectToBLEService()
    {
        Intent intent = new Intent(this, BLEService.class);
        serviceConnection = new SurveillanceServiceConnection();

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectFromBLEService()
    {
        try {
            if(service.getSurveillanceState() == BLEService.SURVEILLANCE_STARTED)
                service.disableSurveillance();

            service.setSurveillanceCallback(null);
        } catch (BLEServiceException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "an error has occured in the ble service.", e);
        }
        unbindService(serviceConnection);
    }

    class SurveillanceServiceConnection implements ServiceConnection
    {
        public void onServiceConnected(ComponentName name, IBinder service) {

            ExterieurActivity.this.service = ((BLEService.LocalBinder) service).getService();
            ExterieurActivity.this.service.setSurveillanceCallback(new MySurveillanceCallback());
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Connection established with the BLE service.");

            binded = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            binded = false;
            service = null;
        }
    }

    class MySurveillanceCallback extends BLEService.SurveillanceCallback
    {

        @Override
        public void onSurveilllanceStarting() {
        }

        @Override
        public void onSurveilllanceStarted() {
        }

        @Override
        public void onSurveilllanceStopping() {
        }

        @Override
        public void onSurveilllanceStopped() {
        }

        @Override
        public void onSurveillanceFailed(String message) {
            showErrorMessage(message);
        }

        @Override
        public void onSurveillanceFailed(List<Tag> tagsNotFound) {
            String message = "Les objets suivants sont manquants : ";

            for(Tag tag : tagsNotFound)
                message += tag.getObjectName() + ", \n";


            showErrorMessage(message.substring(0, message.length() - 4) + ".");
        }

        @Override
        public void onBraceletNotFound(String braceletUID) {
            showErrorMessage("Le bracelet n'a pas été trouvé.");
        }

        @Override
        public void onBraceletConnected() {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Bracelet connected.");
        }

        @Override
        public void onBraceletConnectionFailed() {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Bracelet connection has failed while being realized.");
            showErrorMessage("Erreur : bracelet non trouvé");
        }

        @Override
        public void onBraceletDisconnected() {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Bracelet disconnected.");
        }
    }

}
