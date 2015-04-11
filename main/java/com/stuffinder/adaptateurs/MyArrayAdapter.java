package com.stuffinder.adaptateurs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stuffinder.R;
import com.stuffinder.data.Tag;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter {

    private final Context context;
    private final List<Tag> listTag;

    public MyArrayAdapter(Context context, List<Tag> listTag) {
        super(context, R.layout.custom_list, listTag);
        this.context = context;
        this.listTag = listTag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.custom_list, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        textView.setText(listTag.get(position).getObjectName());

        if(listTag.get(position).getObjectImageName() != null)
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(listTag.get(position).getObjectImageName());
            if(myBitmap != null)
                imageView.setImageBitmap(myBitmap);
            else
                Log.w(getClass().getName(), "image \"" + listTag.get(position).getObjectImageName() + "\" fails to be loaded or decoded.");

        }

        // Change icon based on name
//
//        String s = listTag.get(position).getObjectImageName();
//        s = s == null ? "" : s;
//
//        if (s.equals("bag")) {
//            imageView.setImageResource(R.drawable.bag);
//        } else if (s.equals("carkey")) {
//            imageView.setImageResource(R.drawable.carkey);
//        } else if (s.equals("keys")) {
//            imageView.setImageResource(R.drawable.keys);
//        }else if (s.equals("smartphone")) {
//            imageView.setImageResource(R.drawable.smartphone);
//        }else if (s.equals("tablet")) {
//            imageView.setImageResource(R.drawable.tablet);
//        }else if (s.equals("tag")) {
//            imageView.setImageResource(R.drawable.tag);
//        }else if (s.equals("wallet")) {
//            imageView.setImageResource(R.drawable.wallet);
//        }

        return rowView;
    }

}
