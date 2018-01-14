package com.example.vincent.comiccollector;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Vincent on 14-1-2018.
 */

public class gridAdapter extends ArrayAdapter<comic> {
    public gridAdapter(@NonNull Context context, ArrayList<comic> comics, int resource) {
        super(context, resource, comics);
    }
}
