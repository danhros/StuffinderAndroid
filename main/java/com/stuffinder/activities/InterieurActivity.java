package com.stuffinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.stuffinder.R;
import com.stuffinder.data.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class InterieurActivity extends Activity {

    private ListView mListInt = null;
    private static List<Tag> arrayAdapter = new ArrayList<>();

    public void retour8 (View view){
      finish();
    }

    public void goToLoc (View view) {
        int rang = mListInt.getCheckedItemPosition() ;
        Tag tag = arrayAdapter.get(rang);

        LocalisationActivity.ChangeTag(tag);

        Intent intentLoc = new Intent (InterieurActivity.this, LocalisationActivity.class);
        startActivity(intentLoc);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_interieur);

        mListInt = (ListView) findViewById(R.id.listInt);

        ArrayAdapter<Tag> tagArrayAdapter = new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_single_choice);
        tagArrayAdapter.addAll(arrayAdapter);

        mListInt.setAdapter(tagArrayAdapter);
        mListInt.setItemChecked(0,true);
    }


    public static void ChangeTagsList(List<Tag> list)
    {
        arrayAdapter.clear();

        arrayAdapter.addAll(list);

        Collections.sort(arrayAdapter, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getObjectName().compareTo(rhs.getObjectName());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_interieur, menu);
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
