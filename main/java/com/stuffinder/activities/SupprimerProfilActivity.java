package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SupprimerProfilActivity extends Activity {


    private static List<Profile> listProfiles = new ArrayList<>();
    private static Profile selectedProfile = null;
    private ListView listView = null;
    private List<String> listNames = new ArrayList<>();


    public static void ChangeListProfiles(List<Profile> list) {        // Méthode qui agit sur la variable de classe listProfiles, elle met à jour les données de la liste des profils
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_supprimer_profil);


        listView = (ListView) findViewById(R.id.listSuppr);

        for (int i = 0; i < listProfiles.size(); i++) {
            listNames.add(listProfiles.get(i).getName());
        }

        ArrayAdapter<String> profileArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        profileArrayAdapter.addAll(listNames);

        listView.setAdapter(profileArrayAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedProfile = listProfiles.get(position);
                }
            }
        );

        listView.setItemChecked(0, true);
        if(listProfiles.size() > 0)
            selectedProfile = listProfiles.get(0);
    }


    public void retour(View view) {
        onBackPressed();
    }


    public void supprimer(View view) {

            if (selectedProfile != null) {
                try {
                    EngineServiceProvider.getEngineService().removeProfile(selectedProfile);
                    onBackPressed();
                } catch (IllegalFieldException e) {
                    if(e.getReason() == IllegalFieldException.REASON_VALUE_INCORRECT)
                    {
                        Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
                        return;
                    }// sinon, le profil a déja été supprimé du serveur.
                } catch (NetworkServiceException e) {
                    Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
                    return;
                } catch (NotAuthenticatedException e) {
                    Toast.makeText(this, "Une erreur anormale est survenue", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        else
            Toast.makeText(this, "Vous n'avez sélectionné aucun profil", Toast.LENGTH_LONG).show();
    }


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
