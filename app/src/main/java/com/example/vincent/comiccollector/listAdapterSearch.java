package com.example.vincent.comiccollector;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


import java.util.ArrayList;


public class listAdapterSearch extends ArrayAdapter {

    private final ArrayList<String> users;
    private Context context;
    public listAdapterSearch(@NonNull Context context, ArrayList<String> users, int resource) {
        super(context, resource, users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.row_layout_search,null);
        tools.setTextView(R.id.user,users.get(position),view);
        return view;
    }



}
