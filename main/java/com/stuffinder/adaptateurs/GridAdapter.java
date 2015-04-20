package com.stuffinder.adaptateurs;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.stuffinder.R;

public class GridAdapter extends BaseAdapter {

    private Drawable[] listPics = null;
    LayoutInflater layoutInflater;
    Context context;
    private int lastPosition = -1;

    // constructeur
    public GridAdapter(Context context, Drawable[] listPics) {
        this.listPics = listPics;
        layoutInflater = LayoutInflater.from(context);
        this.listPics = listPics;
        this.context = context;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listPics.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return listPics[position];
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    static class ViewHolder {

        ImageView pictureView;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_item, null);
            holder = new ViewHolder();
            // initialisation des vues

            holder.pictureView = (ImageView) convertView.findViewById(R.id.picture);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // affchier les données convenablement dans leurs positions

        if(position == listPics.length - 1) // la dernière position est utilisée pour le cas "ne pas sélectionner d'image".
            holder.pictureView.setImageDrawable(null);
        else
            holder.pictureView.setImageDrawable(listPics[position].getCurrent());


        lastPosition = position;
        return convertView;

    }
}
