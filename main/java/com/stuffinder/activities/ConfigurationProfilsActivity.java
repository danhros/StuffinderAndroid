package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import android.view.View;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.exceptions.IllegalFieldException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.List;

public class ConfigurationProfilsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_configuration_profils);


    }

    public void goToSup(View view) {

        try {
            List<Profile> list = EngineServiceProvider.getEngineService().getProfiles();
            SupprimerProfilActivity.ChangeListProfiles(list);
            Intent intentGoToSup = new Intent(ConfigurationProfilsActivity.this, SupprimerProfilActivity.class);
            startActivity(intentGoToSup);
        } catch (NotAuthenticatedException e) {
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
        } catch (IllegalFieldException e) {
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
        }
    }

    public void retour(View view) {
        onBackPressed();
    }

    public void goToModif(View view) {


        try {

            List<Profile> list = EngineServiceProvider.getEngineService().getProfiles();
            ModifierProfileActivity.ChangeListProfiles(list);
            Intent intentModProf = new Intent(ConfigurationProfilsActivity.this, ModifierProfileActivity.class);
            startActivity(intentModProf);


        } catch (NotAuthenticatedException e) {  // anormal error.
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
        }
    }


    public void goToCreer(View view) {

        try {
            List<Tag> list = EngineServiceProvider.getEngineService().getTags();
            CreerProfilActivity.changeTagsList(list);
            Intent intentModProf = new Intent(ConfigurationProfilsActivity.this, CreerProfilActivity.class);
            startActivity(intentModProf);
        } catch (NotAuthenticatedException e) {  // anormal error.
            Toast.makeText(this, "Une erreur anormale est survenue. Veuiller redémarrer l'application", Toast.LENGTH_LONG).show();
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "Une erreur réseau est survenue.", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_configuration_profils, menu);
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
