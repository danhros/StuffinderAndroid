package com.stuffinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.io.File;

public class AjoutTagActivity extends BasicActivity {

    EditText EditTextNom ;
    EditText EditTextId ;
    ImageView imageView;

    private int selectedImageResourceId;
    private boolean imageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ajout_tag);

        imageSelected = false;

        EditTextNom = (EditText)findViewById(R.id.editTextNom);
        EditTextId = (EditText)findViewById(R.id.editTextId);
        imageView = (ImageView) findViewById(R.id.imageViewTag);
    }

    public void retour2 (View view) {
        finish();
    }

    public void choosePic (View view) {

        Intent intentPics = new Intent (this, PicturesActivity.class);
        PicturesActivity.setCallback(new PicChooserCallback());
        startActivity(intentPics);
    }

    public void creerTag (View view) {

        String nom = EditTextNom.getText().toString();
        String identifiant = EditTextId.getText().toString();

        File imageFile = imageSelected ? getImageFileByResource(selectedImageResourceId) : null;

        if(nom.length() == 0)
            Toast.makeText(this, "Entrer nom", Toast.LENGTH_LONG).show();
        else if(identifiant.length() == 0)
            Toast.makeText(this, "Entrer identifiant", Toast.LENGTH_LONG).show();
        else
        {
            try {

                Tag tag = new Tag(identifiant, nom, null);
                EngineServiceProvider.getEngineService().addTag(tag, imageFile);

                finish();
            }

            catch (IllegalFieldException e) {
                switch (e.getFieldId()) {
                    case IllegalFieldException.TAG_OBJECT_NAME:
                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                            Toast.makeText(this, "Nom déjà utilisé", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(this, "Nom incorrect", Toast.LENGTH_LONG).show();
                        break;
                    case IllegalFieldException.TAG_OBJECT_IMAGE:
                        Toast.makeText(this, "Image incorrecte", Toast.LENGTH_LONG).show();
                    case IllegalFieldException.TAG_UID:
                        if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                            Toast.makeText(this, "Identifiant déjà utilisé", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(this, "Identifiant incorrect", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            catch (NotAuthenticatedException e) {
                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            }
            catch (NetworkServiceException e) {
                Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            }
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

    class PicChooserCallback extends PicturesActivity.PictureChooserCallback
    {
        @Override
        public void onPictureSelected(int drawableResourceId) {
            selectedImageResourceId = drawableResourceId;
            imageSelected = true;

            imageView.setImageResource(drawableResourceId);
        }

        @Override
        public void onPictureUnselected() {
            imageSelected = false;
            imageView.setImageDrawable(null);
        }
    }

}