package com.stuffinder.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;

import com.stuffinder.R;
import com.stuffinder.adaptateurs.GridAdapter;
import com.stuffinder.data.Tag;

public class PicturesActivity extends BasicActivity {

    Drawable[] listDrawable;
    int imageResourceIds[];
    GridView grid;
    private static Tag tagModif;

    public static void getTag(Tag tag)
    {
        tagModif = tag ;
    }

    public void retour11 (View view) {
        onBackPressed();
    }

    public void goBack(View view, int position) {

        int selectedResId = imageResourceIds[position];

        if(callback != null)
            if(selectedResId != R.drawable.no_picture)
                callback.onPictureSelected(selectedResId);
            else
                callback.onPictureUnselected();

        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                getResources().getDrawable(R.drawable.wallet),
                null};

        imageResourceIds = new int[]{
                R.drawable.bag,
                R.drawable.carkey,
                R.drawable.glasses,
                R.drawable.keys,
                R.drawable.smartphone,
                R.drawable.tablet,
                R.drawable.tag,
                R.drawable.wallet,
                R.drawable.no_picture
        };
        grid.setAdapter(new GridAdapter(getApplicationContext(), listDrawable));

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                goBack(view,position);

            }
        });

    }

    static abstract class PictureChooserCallback
    {
        public abstract void onPictureSelected(int drawableResourceId);
        public abstract void onPictureUnselected();
    }

    private static PictureChooserCallback callback;

    static void setCallback(PictureChooserCallback callback)
    {
        PicturesActivity.callback = callback;
    }

}
