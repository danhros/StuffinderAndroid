package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.stuffinder.data.Tag;
import com.stuffinder.engine.FieldVerifier;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.exceptions.TagAlreadyUsedException;

import com.stuffinder.R;

public class AjoutTagActivity extends Activity {

    EditText EditTextNom ;
    EditText EditTextImage ;
    EditText EditTextId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_tag);

        EditTextNom = (EditText)findViewById(R.id.editTextNom) ;
        EditTextImage = (EditText)findViewById(R.id.editTextImage) ;
        EditTextId = (EditText)findViewById(R.id.editTextId) ;

    }

    public void creerTag (View view) {
        Intent intent = new Intent (AjoutTagActivity.this, TagsActivity.class);



        String nom = EditTextNom.getText().toString();
        String image = EditTextImage.getText().toString();
        String identifiant = EditTextId.getText().toString();


        try {

            if (!FieldVerifier.verifyTagName(nom)) {
                throw new IllegalFieldException(IllegalFieldException.TAG_OBJECT_NAME, "remplir nom");
            }                                                                            /* Vérification de la validité des champs*/
            if (!FieldVerifier.verifyTagUID(identifiant)) {
                throw new IllegalFieldException(IllegalFieldException.TAG_UID, "remplir prenom");
            }


            Tag tag = new Tag(identifiant, nom, image);
            NetworkServiceProvider.getNetworkService().addTag(tag) ;


            startActivity(intent);  }
                                                                         /* Passer à l'activité Home*/
        catch (NullPointerException e) {}

        catch (IllegalFieldException e) {
            switch (e.getFieldId()) {
                case IllegalFieldException.TAG_OBJECT_NAME:
                    Toast.makeText(AjoutTagActivity.this, "Entrer nom", Toast.LENGTH_LONG).show();
                    break;
                case IllegalFieldException.TAG_OBJECT_IMAGE:
                    Toast.makeText(AjoutTagActivity.this, "Entrer image", Toast.LENGTH_LONG).show();
                case IllegalFieldException.TAG_UID:
                    Toast.makeText(AjoutTagActivity.this, "Entrer identifiant", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        catch (NotAuthenticatedException e) {
            e.printStackTrace();
        }

        catch (NetworkServiceException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_configuration, menu);
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
