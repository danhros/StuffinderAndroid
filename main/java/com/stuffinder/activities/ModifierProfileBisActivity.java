package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModifierProfileBisActivity extends Activity {


    private static List<Tag> tagsList = new ArrayList<>();
    private static Profile profile;
    private ListView listView = null;
    private ArrayAdapter<Tag> tagProfArrayAdapter;
    private Button buttonModifier = null;
    private EditText editTextModifierNom = null;

    public static void changeProfile(Profile profile) {
        ModifierProfileBisActivity.profile = profile;
    }

    public static void changeTagsList(List<Tag> list) {
        tagsList.clear();
        tagsList.addAll(list);
        Collections.sort(tagsList, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getObjectName().compareTo(rhs.getObjectName());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modifier_profile_bis);

        listView = (ListView) findViewById(R.id.listModiProf);
        buttonModifier = (Button) findViewById(R.id.buttonCreer);
        editTextModifierNom = (EditText) findViewById(R.id.editTextModifierNomProf);

        editTextModifierNom.setText(profile.getName());

        tagProfArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice);
        tagProfArrayAdapter.addAll(tagsList);
        listView.setAdapter(tagProfArrayAdapter);

        for (int i = 0; i < tagsList.size(); i++) {
            if (profile.getTags().contains(tagsList.get(i))) {
                listView.setItemChecked(i, true);
            }
        }


    }

    public void modifierProfil(View view) {

        SparseBooleanArray tab = listView.getCheckedItemPositions();
        List<Tag> newTagsProfileList = new ArrayList<>();
        int tagPourProfils = 0;
        String nomProfil = editTextModifierNom.getText().toString();

        boolean fermerActivite = false;

        for (int i = 0; i < tagsList.size(); i++) {
            if (tab.get(i) == true) {
                newTagsProfileList.add(tagsList.get(i));
                tagPourProfils++;
            }
        }


        if (nomProfil.length() == 0) {
            Toast.makeText(this, "Entrez un nom de profils.", Toast.LENGTH_LONG).show();
        } else {
            try {
                EngineServiceProvider.getEngineService().modifyProfileName(profile, nomProfil);
                fermerActivite = true;
            } catch (NotAuthenticatedException e) {
                Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
            } catch (NetworkServiceException e) {
                Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            } catch (IllegalFieldException e) {
                if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                    Toast.makeText(this, "Nom déjà utilisé pour un autre profil.", Toast.LENGTH_LONG).show();
                else if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                    Toast.makeText(this, "Modification impossible : ce profil semble avoir été supprimé.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, "Nom du Profil incorrect", Toast.LENGTH_LONG).show();
            }
        }

        if(! fermerActivite)
            return;

        if (tagPourProfils == 0) {
            Toast.makeText(this, "Aucune puce n'est sélectionnée , votre profil sera vide", Toast.LENGTH_LONG).show();
        } else {
            try {
                EngineServiceProvider.getEngineService().replaceTagListOfProfile(profile, newTagsProfileList);

                finish();
            } catch (NetworkServiceException e) {
                Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            } catch (IllegalFieldException e) {
                switch (e.getFieldId()) {
                    case IllegalFieldException.TAG_UID:
                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                            Toast.makeText(this, "Modification impossible : La puce ayant l'identifiant \"" + e.getFieldValue() + "\"semble avoir été supprimé.", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.PROFILE_NAME:
                        if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                            Toast.makeText(this, "Modification impossible : ce profil semble avoir été supprimé.", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
                        break;
                }
            } catch (NotAuthenticatedException e) {
                Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();
            }
        }


    }

    public void retour(View view) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modifier_profile_bis, menu);
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
