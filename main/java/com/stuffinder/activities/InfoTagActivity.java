package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.stuffinder.R;

public class InfoTagActivity extends Activity {

    EditText EditTextNom ;
    EditText EditTextImage ;
    EditText EditTextId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_tag);

        EditTextNom = (EditText)findViewById(R.id.editTextNom) ;
        EditTextImage = (EditText)findViewById(R.id.editTextImage) ;
        EditTextId = (EditText)findViewById(R.id.editTextId) ;



    }

    public void creerCompte (View view) {
        Intent intent = new Intent (InfoTagActivity.this, TagsActivity.class);
        startActivity(intent);
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
