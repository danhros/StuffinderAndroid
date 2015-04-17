package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SupprimerProfilActivity extends Activity {


    private ListView listView = null ;
    private static List<Profile> listProfiles = new ArrayList<>();
    private List<String> listNames = new ArrayList<>();
    private static Profile profile = null;
    private int nombreProfilSup = 0;



   


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supprimer_profil);


        listView = (ListView) findViewById(R.id.listSuppr);

        for (int i = 0 ; i < listProfiles.size(); i ++  ) { listNames.add(listProfiles.get(i).getName()); }

        ArrayAdapter<String> profileArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        profileArrayAdapter.addAll(listNames);

        listView.setAdapter(profileArrayAdapter);
        listView.setItemChecked(0,true); }



    public void retour(View view) {
        finish();
    }


    public void supprimer(View view) {

        SparseBooleanArray tab = listView.getCheckedItemPositions() ;

        for ( int i =0 ; i<tab.size() ; i++) {

            if ( tab.get(i) ) {
                nombreProfilSup ++;
                try { NetworkServiceProvider.getNetworkService().removeProfile(listProfiles.get(i));
                     }
                catch (IllegalFieldException e)  {}
                catch (NetworkServiceException e) { Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();}
                catch (NotAuthenticatedException e) { Toast.makeText(this, "Une erreur anormale est survenue", Toast.LENGTH_LONG).show();}} }

        if ( nombreProfilSup == 0 ) { Toast.makeText(this, "Vous n'avez sélectionné aucun profil", Toast.LENGTH_LONG).show();}
         else {
         Intent intentGotoConfiProf = new Intent ( this, ConfigurationProfilsActivity.class );
         finish(); } }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_supprimer_profil, menu);
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
