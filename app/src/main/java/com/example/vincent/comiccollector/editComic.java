package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.vincent.comiccollector.collectionView.saveCollection;
import static com.example.vincent.comiccollector.comicInfo.JSONify;
import static com.example.vincent.comiccollector.comicInfo.createLink;


/**
 * A simple {@link Fragment} subclass.
 */
public class editComic extends DialogFragment {
    public ArrayList<ownedComic> collection;
    int comicId;

    public editComic newInstance(int comicId) {
        editComic f = new editComic();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("comicId", comicId);
        f.setArguments(args);
        return f;
    }

    public editComic() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_comic, container, false);
        comicId = getArguments().getInt("comicId");
        getScores(comicId + "");
        return view;
    }

    public void getScores(final String query) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();
        nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot comic : dataSnapshot.getChildren()) {
                            if (Objects.equals(comic.getValue(ownedComic.class).comicId + "", query)) {
                                ownedComic comicBook = comic.getValue(ownedComic.class);
                                ArrayList<Double> scores = comicInfo.stripScores(comicBook.condition);
                                configSharedPrefScores(scores);
                                listAdapter adapter = new listAdapter(getContext(), scores, 1);
                                String title = "Editing: " + comicBook.title;
                                comicInfo.setTextView(R.id.titleEdit, title, getView());
                                ListView listView = getView().findViewById(R.id.listEdit);
                                listView.setAdapter(adapter);


                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void configSharedPrefScores(ArrayList<Double> scores) {
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putInt("size", scores.size());
        for (int i = 0; i < scores.size(); i++) {
            String score = scores.get(i).toString();
            editor.putString("score_" + i, score);
        }
        editor.apply();
    }

    public ArrayList<Double> getSharedPrefScores() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("scores", Context.MODE_PRIVATE);
        int size = sharedPref.getInt("size", 0);
        ArrayList<Double> scores= new ArrayList<Double>();
        for (int i = 0; i < size; i++) {
            String retrieved= sharedPref.getString("score_"+i,"deleted");
            if (retrieved!="deleted"){
            Double score = Double.parseDouble(retrieved);
            scores.add(score);}
        }
        return scores;
    }
    private DialogInterface.OnDismissListener onDismissListener;
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
}
