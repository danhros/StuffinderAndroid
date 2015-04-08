package com.stuffinder.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.stuffinder.R;
import com.stuffinder.adaptateurs.GridAdapter;

public class PicturesActivity extends Activity {

    Drawable[] listDrawable = null;
    GridView grid;

    public void retour11 (View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures);
        grid = (GridView) findViewById(R.id.grid);

        listDrawable = new Drawable[]{
                getResources().getDrawable(R.drawable.bag),
                getResources().getDrawable(R.drawable.carkey),
                getResources().getDrawable(R.drawable.glasses),
                getResources().getDrawable(R.drawable.keys),
                getResources().getDrawable(R.drawable.smartphone),
                getResources().getDrawable(R.drawable.tablet),
                getResources().getDrawable(R.drawable.tag),
                getResources().getDrawable(R.drawable.wallet)};

        grid.setAdapter(new GridAdapter(getApplicationContext(), listDrawable));

    }


}
