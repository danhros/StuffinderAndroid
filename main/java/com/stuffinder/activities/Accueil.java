package com.stuffinder.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.ble.BLEService;
import com.stuffinder.engine.EngineService;
import com.stuffinder.engine.EngineServiceProvider;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.EngineServiceException;
import com.stuffinder.exceptions.NetworkServiceException;
import com.stuffinder.webservice.NetworkService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class Accueil extends BasicActivity {




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

        EngineServiceProvider.setEngineService(EngineService.getInstance());
        NetworkServiceProvider.setNetworkService(NetworkService.getInstance());

        try {
            NetworkServiceProvider.getNetworkService().initNetworkService();
            EngineServiceProvider.getEngineService().initEngineService(this);
            BLEService.startBLEService(this);
            initializeDefaultImageSet();

            EngineServiceProvider.getEngineService().setAutoSynchronization(true);
        } catch (NetworkServiceException e) {
            Toast.makeText(this, "L'initialisation de l'application a échoué. L'application va être arrêté.", Toast.LENGTH_LONG).show();
            finish();
        } catch (EngineServiceException e) {
            Toast.makeText(this, "L'initialisation de l'application a échoué. L'application va être arrêté.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    void initializeDefaultImageSet()
    {
        File folder = new File(getFilesDir(), "default_images");
        folder.mkdirs();
        int resources[] = {R.drawable.bag, R.drawable.carkey, R.drawable.keys, R.drawable.smartphone, R.drawable.tablet, R.drawable.tag, R.drawable.wallet};

        File initMark = new File(folder, "initialized");
        if(initMark.exists())
        {
            Log.i(getClass().getName(), "Default image folder is already initialized.");
            return;
        }

        for(int i=0; i < resources.length; i++)
        {
            try {
                copyResourceToFolder(folder, resources[i], getResources().getResourceEntryName(resources[i]));
                Log.i(getClass().getName(), "resource copied : " + getResources().getResourceEntryName(resources[i]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            if(initMark.createNewFile())
                Log.i(getClass().getName(), "initialization finished.");
            else
                Log.i(getClass().getName(), "initialization failed at the end. application can continue to run.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void copyResourceToFolder(File folder, int resourceId, String newFilename) throws FileNotFoundException
    {
        File file = new File(folder, newFilename + ".png");

        OutputStream outputStream = new FileOutputStream(file);

        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;
        Bitmap bbicon = BitmapFactory.decodeResource(getResources(), resourceId);

        bbicon.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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
