package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.tests.NetworkService;


public class Accueil extends Activity {




    public void accueilToSeCo (View view) {
        Intent intentSeCo = new Intent ( Accueil.this, SeConnecterActivity.class);
        startActivity(intentSeCo);
    }

    public void accueilToCreer ( View view) {
        Intent intentCreer = new Intent ( Accueil.this, CreerCompteActivity.class);
        startActivity (intentCreer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_accueil);

        NetworkServiceProvider.setNetworkService(NetworkService.getInstance());
        try {
            NetworkServiceProvider.getNetworkService().initNetworkService();
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "L'initialisation de l'application a échoué. L'application va être arrêté.", Toast.LENGTH_LONG).show();
            finish();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accueil, menu);
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
