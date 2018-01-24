package com.example.vincent.comiccollector;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;


public class listAdapter extends ArrayAdapter {

    private final ArrayList<Double> scores;
    private Context context;
    public listAdapter(@NonNull Context context, ArrayList<Double> scores, int resource) {
        super(context, resource, scores);
        this.context = context;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.row_layout,null);
        setEditText(scores.get(position),R.id.qualityScore,view);
        ImageButton save = view.findViewById(R.id.save);
        ImageButton delete = view.findViewById(R.id.delete);
        delete.setOnClickListener(new deleteScore());
        save.setOnClickListener(new saveScore());
        return view;
    }

    public void setEditText(Double input,int id,View view){
        EditText editText = view.findViewById(id);
        editText.setText(input.toString());
    }

    private class deleteScore implements View.OnClickListener {
        @Override
        public void onClick(View view) {

        }
    }

    private class saveScore implements View.OnClickListener {
        @Override
        public void onClick(View view) {

        }
    }
}
