package com.stuffinder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.stuffinder.R;
import com.stuffinder.data.Account;
import com.stuffinder.data.Profile;
import com.stuffinder.engine.NetworkServiceProvider;
import com.stuffinder.exceptions.NotAuthenticatedException;

import java.util.ArrayList;
import java.util.List;


public class ExterieurActivity extends Activity {

        ListView listView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_exterieur);

        listView= (ListView)findViewById(R.id.listView);
        try {
            Account account = NetworkServiceProvider.getNetworkService().getCurrentAccount();

            List<Profile> profiles = account.getProfils();
            ArrayList<String> liste = new ArrayList<String>();
            int size = profiles.size();

            for ( int i=0; i<size; i++ ) {
                String name = profiles.get(i).getName();
                liste.add(name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.list_content, liste);
           listView.setAdapter(adapter);
        }

        catch ( NotAuthenticatedException e ) {
            Toast.makeText(ExterieurActivity.this, "Nous n'avons pas réussi à récupérer les informations de votre compte, veuillez réassyer", Toast.LENGTH_LONG).show();}


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exterieur, menu);
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
