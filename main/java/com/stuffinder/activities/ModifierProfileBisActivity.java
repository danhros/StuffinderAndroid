package com.stuffinder.activities;

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
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;


import com.stuffinder.R;
import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;


import static com.stuffinder.exceptions.IllegalFieldException.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModifierProfileBisActivity extends BasicActivity {



    private ListView listView = null;
    private ArrayAdapter<Tag> tagProfArrayAdapter;
    private Button buttonModifier = null;
    private static List<Tag> tagsList = new ArrayList<>();
    private static Profile profile;
    private EditText editTextModifierNom = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modifier_profile_bis);

        listView = (ListView) findViewById(R.id.listModiProf);
        buttonModifier = (Button) findViewById(R.id.buttonCreer);
        editTextModifierNom= (EditText)findViewById(R.id.editTextModifierNomProf);

        editTextModifierNom.setText(profile.getName());

        tagProfArrayAdapter = new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_multiple_choice);
        tagProfArrayAdapter.addAll(tagsList);
        listView.setAdapter(tagProfArrayAdapter);

        for ( int i = 0; i< tagsList.size(); i++ )
        { if ( profile.getTags().contains(tagsList.get(i)) ) {
            listView.setItemChecked(i,true);}
        }


    }

    public void modifierProfil ( View view ) throws InterruptedException {

        SparseBooleanArray tab = listView.getCheckedItemPositions() ;
        List<Tag> newTagsProfileList = new ArrayList<>();
        int tagPourProfils = 0 ;
        String nomProfil = editTextModifierNom.getText().toString();

        for ( int i = 0 ; i<tagsList.size(); i++ ) {
            if (tab.get(i) == true) { newTagsProfileList.add(tagsList.get(i));
                                      tagPourProfils ++ ; }}



        if ( nomProfil.length() == 0 ) { Toast.makeText(this, "Entrez un nom de profils.", Toast.LENGTH_LONG).show(); }
        else {
         try { NetworkServiceProvider.getNetworkService().modifyProfileName(profile, nomProfil); }

        catch ( NotAuthenticatedException e ) { Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show(); }
        catch ( NetworkServiceException e ) { Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show(); }
        catch ( IllegalFieldException e ) { switch (e.getFieldId()) {
            case IllegalFieldException.REASON_VALUE_ALREADY_USED :
                Toast.makeText(this, "Nom de Profil déjà utilisé.", Toast.LENGTH_LONG).show();
            case IllegalFieldException.REASON_VALUE_INCORRECT :
                Toast.makeText(this, "Nom de Profils Incorrect", Toast.LENGTH_LONG).show(); }
            }}



        if ( tagPourProfils == 0  ) { Toast.makeText(this, "Aucune puce n'est sélectionnée , votre profil sera vide", Toast.LENGTH_LONG).show();}
        else {
            try {  NetworkServiceProvider.getNetworkService().replaceTagListOfProfile(profile, newTagsProfileList) ;
                   Intent intentGotoConfiProf = new Intent ( this, ConfigurationProfilsActivity.class );
                   finish(); }
        catch ( NetworkServiceException e ) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show(); }
        catch ( IllegalFieldException e ) { }
        catch ( NotAuthenticatedException e ) { Toast.makeText(this, "Une erreur anormale est survenue.", Toast.LENGTH_LONG).show();} }


    }

    public void retour(View view){
        finish();
    }



            public static void changeProfile ( Profile profile) {
        ModifierProfileBisActivity.profile = profile;
    }


    public static void changeTagsList(List<Tag> list)
    {
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
