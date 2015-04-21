package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.stuffinder.data.Tag;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;
import com.stuffinder.R;

public class InfoTagActivity extends Activity {

    EditText editTextNom;
    EditText editTextImage;

    private static Tag tagModif;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info_tag);

        editTextNom = (EditText)findViewById(R.id.editTextNom) ;
        editTextImage = (EditText)findViewById(R.id.editTextImage) ;


            editTextNom.setText(tagModif.getObjectName(), TextView.BufferType.EDITABLE);
            editTextImage.setText(tagModif.getObjectImageName(), TextView.BufferType.EDITABLE);

    }

    public void retour7 (View view) {
        finish();
    }

    public void modifierTag(View view) {

        String objectName = editTextNom.getText().toString();
        String objectImageFileName = editTextImage.getText().toString();

        if(objectName.length() == 0)
            Toast.makeText(this, "Entrer nom", Toast.LENGTH_LONG).show();
       else
        {
            boolean hideAtEnd = true;

            if(! objectName.equals(tagModif.getObjectName())) // the object name is modified.
            {
                try {
                    tagModif = NetworkServiceProvider.getNetworkService().modifyObjectName(tagModif, objectName);
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

            if(hideAtEnd && ! objectImageFileName.equals(tagModif.getObjectImageName())) // the image filename is modified.
            {
                try {
                    tagModif = NetworkServiceProvider.getNetworkService().modifyObjectImage(tagModif, objectImageFileName);
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

}
