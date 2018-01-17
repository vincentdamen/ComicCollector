package com.example.vincent.comiccollector;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class gridAdapter extends ArrayAdapter<ownedComic> {


    private Context context;
    private ArrayList<ownedComic> collection;
    public gridAdapter(@NonNull Context context, ArrayList<ownedComic> collection, int resource) {
        super(context, resource, collection);
        this.context = context;
        this.collection = collection;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.cell_layout,   null);
        return assignInfo(view,position);

    }

    public View assignInfo(View view,int position) {
        String imageUrl = collection.get(position).thumbLink +
                 "."+collection.get(position).thumbExt;
        Log.d("image",imageUrl);
        ImageView imageView = view.findViewById(R.id.icon);
        Glide.with(context).load(imageUrl).into(imageView);
        return view;
    }
}
