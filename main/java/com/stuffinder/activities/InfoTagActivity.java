package com.stuffinder.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.io.File;

public class InfoTagActivity extends BasicActivity {

    EditText editTextNom;
    EditText editTextImage;
    ImageView imageView;

    private static Tag tagModif;

    private int selectedImageResourceId;
    private boolean imageSelected;
    private File currentImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info_tag);

        editTextNom = (EditText)findViewById(R.id.editTextNom) ;
        editTextNom.setText(tagModif.getObjectName(), TextView.BufferType.EDITABLE);

        imageView = (ImageView)findViewById(R.id.imageViewTag);
        if(tagModif.getObjectImageName() != null)
        {
            currentImage = new File(tagModif.getObjectImageName());

            Bitmap myBitmap = BitmapFactory.decodeFile(tagModif.getObjectImageName());
            if(myBitmap != null)
                imageView.setImageBitmap(myBitmap);
            else
            {
                Log.w(getClass().getName(), "image \"" + tagModif.getObjectImageName() + "\" fails to be loaded or decoded.");
                imageView.setImageResource(R.drawable.question);
            }
        }

    }

    public void retour7 (View view) {
        finish();
    }

    public void goToPics (View view) {

        PicturesActivity.getTag(tagModif);
        Intent intentPics = new Intent (this, PicturesActivity.class);
        PicturesActivity.setCallback(new PicChooserCallback());
        startActivity(intentPics);
    }

    public void modifierTag(View view) {

        String objectName = editTextNom.getText().toString();

        if(objectName.length() == 0)
            Toast.makeText(this, "Entrer nom", Toast.LENGTH_LONG).show();
       else
        {
            boolean hideAtEnd = true;

            if(! objectName.equals(tagModif.getObjectName())) // the object name is modified.
            {
                try {
                    tagModif = EngineServiceProvider.getEngineService().modifyObjectName(tagModif, objectName);
                } catch (IllegalFieldException e) {
                    switch(e.getFieldId())
                    {
                        case IllegalFieldException.TAG_UID :
                            if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                Toast.makeText(this, "Modification impossible : ce tag a été supprimé", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                            break;
                        case IllegalFieldException.TAG_OBJECT_NAME :
                            if(e.getReason() == IllegalFieldException.REASON_VALUE_ALREADY_USED)
                                Toast.makeText(this, "Nom déjà utilisé", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(this, "Nom incorrect", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                            break;
                    }
                    hideAtEnd = false;
                } catch (NotAuthenticatedException e) {// abnormal error.
                    Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                    hideAtEnd = false;
                } catch (NetworkServiceException e) {
                    Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
                    hideAtEnd = false;
                }
            }

            if(hideAtEnd && ((currentImage == null && tagModif.getObjectImageName() != null) || (currentImage != null && tagModif.getObjectImageName() == null) || (currentImage != null && ! currentImage.getPath().equals(tagModif.getObjectImageName())))) // the image filename is modified.
            {
                try {
                    tagModif = EngineServiceProvider.getEngineService().modifyObjectImage(tagModif, currentImage);
                } catch (IllegalFieldException e) {
                    switch(e.getFieldId())
                    {
                        case IllegalFieldException.TAG_UID :
                            if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                                Toast.makeText(this, "Modification impossible : ce tag a été supprimé", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                            break;
                        case IllegalFieldException.TAG_OBJECT_IMAGE :
                            Toast.makeText(this, "Fichier incorrect", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                            break;
                    }
                    hideAtEnd = false;
                } catch (NotAuthenticatedException e) {// abnormal error.
                    Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
                    hideAtEnd = false;
                } catch (NetworkServiceException e) {
                    Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
                    hideAtEnd = false;
                }
            }

            if(hideAtEnd)
                finish();
        }

    }

    void supprimerTagsSelectionnes(){

        boolean errorOccured = false;
        boolean oneTagRemoved = false;

        int i=0;
        try {
            EngineServiceProvider.getEngineService().removeTag(tagModif);
            oneTagRemoved = true;
            finish();
        } catch (IllegalFieldException e) {// abnormal error.
            if(e.getReason() == IllegalFieldException.REASON_VALUE_NOT_FOUND)
                Toast.makeText(this, "Suppresion impossible : le tag \"" + tagModif.getObjectName() + "\" a déjà été supprimé.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            errorOccured = true;
        } catch (NotAuthenticatedException e) {// abnormal error.
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
            errorOccured = true;
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
            errorOccured = true;
        }


    }

    public void actionSupprimerTagsSelectionnes(View view){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Cette opération est définitive.")
                .setTitle("Êtes-vous certain de vouloir supprimer ce tag ?");

        // Add the buttons
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                supprimerTagsSelectionnes();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });


        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info_tag, menu);
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

    public static void changeTag(Tag tag)
    {
        tagModif = tag ;

    }

    class PicChooserCallback extends PicturesActivity.PictureChooserCallback
    {
        @Override
        public void onPictureSelected(int drawableResourceId) {
            selectedImageResourceId = drawableResourceId;
            currentImage = getImageFileByResource(drawableResourceId);
            imageSelected = true;

            imageView.setImageResource(drawableResourceId);
        }

        @Override
        public void onPictureUnselected() {
            imageSelected = false;
            currentImage = null;
            imageView.setImageResource(R.drawable.no_picture);
        }
    }

}
