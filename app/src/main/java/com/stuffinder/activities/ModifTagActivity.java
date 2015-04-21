package com.stuffinder.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.stuffinder.R;
import com.stuffinder.data.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModifTagActivity extends Activity {

    private ListView mListModif = null;

    private Button mSend = null;
    private static List<Tag> tagsList = new ArrayList<>();

    public void retour3 (View view) {
        finish();
    }

    public void goToFiche(View view){

        int rang = mListModif.getCheckedItemPosition() ;
        Tag tag = tagsList.get(rang);

        InfoTagActivity.changeTag(tag);
        Intent intent = new Intent (ModifTagActivity.this, InfoTagActivity.class);

        finish();
        startActivity(intent);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_modif_tag);

        mListModif = (ListView) findViewById(R.id.listModif);

        mSend = (Button) findViewById(R.id.send);

        ArrayAdapter<Tag> tagArrayAdapter = new ArrayAdapter<Tag>(this, android.R.layout.simple_list_item_single_choice);
        tagArrayAdapter.addAll(tagsList);

        mListModif.setAdapter(tagArrayAdapter);
        mListModif.setItemChecked(0, true);




    }

    public static void ChangeTagsList(List<Tag> list)
    {
        tagsList.clear();

        tagsList.addAll(list);

        Collections.sort(tagsList, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getObjectName().compareTo(rhs.getObjectName());
            }
        });
    }
}