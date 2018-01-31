package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.DialogFragment;
import android.widget.SeekBar;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class addComicDialog extends DialogFragment {
    // prepare some variables that are used in the whole class.
    ArrayList<comic> newComic;
    SeekBar slider;
    int amount;
    Button add ;
    Button cancel;
    ArrayList<ownedComic> collection;
    collectionView collectionView;
    String condition;
    int comicId;
    private DialogInterface.OnDismissListener onDismissListener;
    ArrayList<Integer> inputIds= new ArrayList<>();

    // the method creates the possibility to open the fragment with an included variable.
    public addComicDialog newInstance(int comicId) {
        addComicDialog f = new addComicDialog();
        Bundle args = new Bundle();
        args.putInt("comicId", comicId);
        f.setArguments(args);
        return f;
    }

    public addComicDialog() {
        // Required empty public constructor.
    }

    // retrieves the comic from the Marvel API.
    public void getComic(final String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String link = tools.createLink(query, 0);
        // start a volley request.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        newComic = comicInfo.JSONify(response);
                        createTitle(newComic.get(0).title);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                // retry if there is an error.
                getComic(query);
            }
        });
        queue.add(stringRequest);
    }

    // sets the title of the dialog.
    public void createTitle(String s) {
        String title = "Add "+s+ " to your collection?";
        tools.setTextView(R.id.titleAdd,title,getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_comic_dialog, container, false);
        // set the required variables.
        inputIds = getInputIds();
        comicId = getArguments().getInt("comicId");

        // retrieves the collection and the comic
        getComic(comicId + "");
        getCollection();
        getDialog().setCanceledOnTouchOutside(false);

        // sets the components and the listeners.
        slider = view.findViewById(R.id.amountBar);
        slider.setOnSeekBarChangeListener(new seekbarManager());

        add = view.findViewById(R.id.sendButton);
        add.setOnClickListener(new addToFireBase());

        cancel = view.findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new leave());
        return view;
    }

    // retrieves the scores from the input boxes.
    public String getCondition() {
        String result = "";
        inputIds.clear();
        inputIds = getInputIds();
        // for each input box.
        for (int id : inputIds) {
            // check if the box is visible.
            if (checkVisibility(id)) {
                String input = getEditText(id, getView());
                // check if the box isn't empty.
                if (!Objects.equals(input, "")) {
                    if (!Objects.equals(result, "")) {
                        result = result + "," + input;
                    } else {
                        result = input;
                    }
                }
            }
        }
        return result;
    }

    // checks the visibility of the id.
    public Boolean checkVisibility(int id){
        EditText editText= getView().findViewById(id);
        return editText.getVisibility() != getView().GONE;
    }

    // manages interactions with the seekbar.
    public class seekbarManager implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            // changes the value from the textview on the right of the bar.
            amount=i;
            tools.setTextView(R.id.amount,(i+1)+ "",getView());
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // changes the visibility of the input boxes.
            visibilityInput(inputIds,amount);

        }
    }

    // changes the visibility of the input boxes.
    public void visibilityInput(ArrayList<Integer> inputIds, int i) {
        for(int id:inputIds){
            if(i>-1){
            setVisibility(id);
            }
            else{
                EditText input = getView().findViewById(id);
                input.setVisibility(View.GONE);
            }
            i=i-1;
        }
    }

    // puts the ids of the input boxes in a ArrayList.
    public ArrayList<Integer> getInputIds(){
        inputIds.add(R.id.condition1);
        inputIds.add(R.id.condition2);
        inputIds.add(R.id.condition3);
        inputIds.add(R.id.condition4);
        inputIds.add(R.id.condition5);
        return inputIds;
    }

    // sets the input boxes to visible.
    public void setVisibility(int id){
        EditText input = getView().findViewById(id);
        input.setVisibility(View.VISIBLE);
    }

    // retrieves the collection from the FireBase.
    public void getCollection(){
        // prepares the required variables.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();
        if (userId != null) {
            nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // saves the collection to the variable.
                            collection = collectionView.saveCollection(dataSnapshot);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("cancel error", databaseError.getMessage());
                        }
                    });
        }
    }

    // retrieves the input from the input boxes.
    public static String getEditText(Integer id,View view) {
        EditText editText = view.findViewById(id);
        return editText.getText().toString();
    }

    // updates the collection with new scores.
    public void transpose(comic comic){
        Boolean newInCollection= true;
        condition = getCondition();
        for(ownedComic owned:collection){
            // if the comic is already in the collection.
            if(owned.comicId==comic.id){
                newInCollection=false;
                owned.condition=owned.condition+","+condition;
                condition= owned.condition;
            }
        }
        if(newInCollection){
            ownedComic transposed = new ownedComic(comic.id,condition,comic.thumbExt,comic.thumbLink,comic.title);
            collection.add(transposed);
        }
    }

    // the OnClickListener to confirm the new scores.
    private class addToFireBase implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // check if the values are allowed.
            if(checkRequirements()){
                transpose(newComic.get(0));
                updateFireBase(collection,getContext());
                setCondition(getContext(),condition);
                dismiss();
            }
        }
    }

    // checks if the values are allowed.
    public boolean checkRequirements() {
        inputIds=getInputIds();
        for (int id : inputIds) {
            // check visibility.
            if (checkVisibility(id)) {
                String input = getEditText(id, getView());
                // checks if the box ain't empty.
                if (input.length() != 0) {
                    Double score = Double.parseDouble(input);
                    // checks if the scores are allowed.
                    if (score > 10.0 || score <= 0.0) {
                        listAdapter.notifyUser("Please fill in a value between 1-10", getContext());
                        return false;
                    }
                }
                else {
                    listAdapter.notifyUser("Please fill in a value between 1-10", getContext());
                    return false;
                }
            }
        }
        return true;}

    // sets the scores to the shared preference.
    public static void setCondition(Context context,String condition) {
        SharedPreferences sharedPref1 = context.getSharedPreferences("newScores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putString("newScores",condition);
        if (Objects.equals(condition, "")){
            editor.putBoolean("status",false);
        }
        else{
            editor.putBoolean("status",true);
        }
        editor.apply();
    }

    // update the FireBase database to store the new scores.
    public static void updateFireBase(final ArrayList<ownedComic> collection, final Context context) {
        // retrieve the required variables.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();

        if (userId != null) {
            nDatabase.child(userId.getUid()).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            // set the values.
                            dataSnapshot.getRef().child("collection").setValue(collection);
                            mainActivity.backAdministration(false,context);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("cancel error", databaseError.getMessage());
                        }
                    });
        }
    }

    // the OnClickListener of the cancel button.
    private class leave implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    }

    // sets the onDismissListener.
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

