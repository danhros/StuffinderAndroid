package com.stuffinder.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModifierProfileActivity extends Activity {


    private static List<Profile> listProfiles = new ArrayList<>();
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


    public void retour(View view) {
        finish();
    }


    public void goToModoficiation(View view) {

        try {

            int rang = listView.getCheckedItemPosition();
            Profile profile = listProfiles.get(rang);
            List<Tag> list = NetworkServiceProvider.getNetworkService().getTags();

            ModifierProfileBisActivity.changeProfile(profile);
            ModifierProfileBisActivity.changeTagsList(list);

            Intent intent = new Intent(this, ModifierProfileBisActivity.class);

            finish();
            startActivity(intent);

        } catch (NotAuthenticatedException e) {// abnormal error.
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modifier_profile);


        listView = (ListView) findViewById(R.id.listModProf);


        for (int i = 0; i < listProfiles.size(); i++) {
            listNames.add(listProfiles.get(i).getName());
        }

        ArrayAdapter<String> profileArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
        profileArrayAdapter.addAll(listNames);

        listView.setAdapter(profileArrayAdapter);
        listView.setItemChecked(0, true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modifier_profile, menu);
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
