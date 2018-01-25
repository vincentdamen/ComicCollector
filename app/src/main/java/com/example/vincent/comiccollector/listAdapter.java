package com.example.vincent.comiccollector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        delete.setOnClickListener(new explainLongClick());
        delete.setOnLongClickListener(new deleteScore(position,view));
        save.setOnClickListener(new saveScore(position,view));
        return view;
    }

    public void setEditText(Double input,int id,View view){
        EditText editText = view.findViewById(id);
        editText.setText(input.toString());
    }

    private class deleteScore implements View.OnLongClickListener {
        int position;
        View view;
        public deleteScore(int i,View view) {
            this.position=i;
            this.view = view;
        }
        @Override
        public boolean onLongClick(View view) {
            view=this.view;
            changeScore(position,false,view);
            ConstraintLayout constraintLayout = view.findViewById(R.id.content);
            constraintLayout.setVisibility(View.GONE);
            TextView textView = view.findViewById(R.id.deletedInfo);
            textView.setVisibility(View.VISIBLE);
            return true;
        }
    }

    private class saveScore implements View.OnClickListener {
        int position;
        View view;
        public saveScore(int i,View view) {
            this.view=view;
            this.position=i;
        }
        @Override
        public void onClick(View view) {
            view=this.view;

            changeScore(position,true,view);
        }
    }

    private class explainLongClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            notfiyUser(getContext().getString(R.string.warningDelete),getContext());
        }
    }
    public void changeScore(int i,Boolean changing,View view){
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        if(changing){
            String editText=addComicDialog.getEditText(R.id.qualityScore,view);
            editor.putString("score_"+i,editText);
        }
        else {
            editor.remove("score_" + i);
        }
        editor.apply();
        Log.d("dd",sharedPref1.getAll().toString());

    }
    public static void notfiyUser(String text,Context context){
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
