package com.stuffinder.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.stuffinder.R;
import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;

import java.util.ArrayList;
import java.util.List;

public class ModifierProfileBisActivity extends BasicActivity {



    private ListView listView = null;
    private ArrayAdapter<Tag> tagArrayAdapter;
    private Button buttonModifier = null;
    private static List<Tag> tagsList = new ArrayList<>();

    private static Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modifier_profile_bis);


        listView = (ListView) findViewById(R.id.listView);

        buttonModifier = (Button) findViewById(R.id.buttonCreer);

        tagArrayAdapter = new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_multiple_choice);
        tagArrayAdapter.addAll(tagsList);

        listView.setAdapter(tagArrayAdapter);

        for ( int i = 0; i< tagsList.size(); i++ )
        { if ( profile.getTags().contains(tagsList.get(i)) ) {
            listView.setItemChecked(i,true);}
        }
    }

    public void retour(View view){
        finish();
    }

    public static void changeProfile ( Profile profile) {
        ModifierProfileBisActivity.profile = profile;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modifier_profile_bis, menu);
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
