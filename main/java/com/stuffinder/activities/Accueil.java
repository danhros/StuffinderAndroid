package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stuffinder.R;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.tests.NetworkServiceEmulator;


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
        setContentView(R.layout.activity_accueil);

        NetworkServiceProvider.setNetworkService(NetworkServiceEmulator.getInstance());

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
