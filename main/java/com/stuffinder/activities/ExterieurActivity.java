package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

import com.stuffinder.R;
import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExterieurActivity extends Activity {

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

        ArrayAdapter<String> profileArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        profileArrayAdapter.addAll(listNames);

       listView.setAdapter(profileArrayAdapter);
       listView.setItemChecked(0,true); }







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
}
