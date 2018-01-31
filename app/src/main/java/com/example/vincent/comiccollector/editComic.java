package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;




/**
 * A simple {@link Fragment} subclass.
 */
public class editComic extends DialogFragment {
    // prepare some variables that are used in the whole class.
    ArrayList<ownedComic> collection = new ArrayList<ownedComic>();
    int comicId;
    String condition;
    private DialogInterface.OnDismissListener onDismissListener;

    // the method creates the possibility to open the fragment with an included variable.
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

        Button send = view.findViewById(R.id.sendButton);
        Button cancel = view.findViewById(R.id.cancelButton);
        send.setOnClickListener(new sendToFirebase());
        cancel.setOnClickListener(new cancelChanges());

        listAdapter.notifyUser(getString(R.string.saveWarning),getContext());
        return view;
    }

    // retrieves the scores from firebase.
    public void getScores(final String query) {
        // prepares the required variables.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();

        nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // for each comic, add it to collection.
                        for (DataSnapshot comic : dataSnapshot.getChildren()) {
                            collection.add(comic.getValue(ownedComic.class));
                            // if the comicId matches. retrieve all the necessary info
                            if (Objects.equals(comic.getValue(ownedComic.class).comicId + "", query)) {
                                ownedComic comicBook = comic.getValue(ownedComic.class);
                                ArrayList<Double> scores = comicInfo.stripScores(comicBook.condition);
                                configSharedPrefScores(scores);

                                // set the listAdapter.
                                listAdapter adapter = new listAdapter(getContext(), scores, 1);
                                String title = "Editing: " + comicBook.title;
                                tools.setTextView(R.id.titleEdit, title, getView());
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

    // store the shared preference to temporarily save the scores.
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

    // retrieve the scores from the shared preference.
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

    // the OnClickListener of the cancel button.
    private class cancelChanges implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            removeScores();
            dismiss();
        }
    }

    // remove the score from the shared preference.
    public void removeScores() {
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("scores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.clear().apply();
    }

    // the OnClickListener to send the updated scores.
    private class sendToFirebase implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            applyChanges();
            addComicDialog.updateFireBase(collection,getContext());
            addComicDialog.setCondition(getContext(),condition);
            dismiss();

        }
    }

    // get the latest scores and update the collection.
    public void applyChanges() {
        ArrayList<ownedComic> toRemove= new ArrayList<ownedComic>();
        ArrayList<Double> scores = getSharedPrefScores();
        condition = Stringify(scores);
        for (ownedComic owned:collection){
            if(owned.comicId==comicId){
                if(Objects.equals(condition, "")){
                    toRemove.add(owned);
                }
                else {
                    owned.condition = condition;
                }
            }
        }
        collection.removeAll(toRemove);
    }

    //transform the Arraylist of scores to a string.
    public String Stringify(ArrayList<Double> scores) {
        String result = "";

        if (scores.size()!=0){
        for(Double score:scores){
            result = result + score.toString() +",";
        }
        result= result.substring(0,result.lastIndexOf(","));}
        return result;
    }

}
