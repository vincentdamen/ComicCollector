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
    ArrayList<comic> newComic;
    comicInfo comicInfo;
    SeekBar slider;
    int amount;
    Button add ;
    Button cancel;
    ArrayList<ownedComic> collection;
    collectionView collectionView;
    String condition;
    int comicId;

    ArrayList<Integer> inputIds= new ArrayList<Integer>();

    public addComicDialog newInstance(int comicId) {
        addComicDialog f = new addComicDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("comicId", comicId);
        f.setArguments(args);
        return f;
    }

    public addComicDialog() {
        // Required empty public constructor
    }

    public void getComic(final String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String link = tools.createLink(query, 0);
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
                getComic(query);
            }
        });
        queue.add(stringRequest);
    }

    public void createTitle(String s) {
        String title = "Add "+s+ " to your collection?";
        tools.setTextView(R.id.titleAdd,title,getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inputIds=getInputIds();
        View view =inflater.inflate(R.layout.fragment_add_comic_dialog, container, false);
        comicId = getArguments().getInt("comicId");
        getComic(comicId + "");
        getCollection();
        getDialog().setCanceledOnTouchOutside(false);
        slider = view.findViewById(R.id.amountBar);
        add = view.findViewById(R.id.sendButton);
        cancel = view.findViewById(R.id.cancelButton);
        slider.setOnSeekBarChangeListener(new seekbarManager());
        add.setOnClickListener(new addToFireBase());
        cancel.setOnClickListener(new leave());

        return view;
    }

    public String getCondition() {
        String result = "";
        for (int id : inputIds) {
            if (checkVisibility(id)) {
                String input = getEditText(id, getView());
                if (input != "") {
                    if (result != "") {
                        result = result + "," + input;
                    } else {
                        result = input;
                    }
                }
            }
        }
        return result;
    }
    public Boolean checkVisibility(int id){
        EditText editText= getView().findViewById(id);
        if(editText.getVisibility()== getView().GONE){
            return false;
        }
        else if (Objects.equals(editText.getText().toString(), "")){
            return false;
        }
        return true;
    }

    public class seekbarManager implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            amount=i;
            Log.d("tester",i+"");
            tools.setTextView(R.id.amount,i+ "",getView());
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            visbilityInput(inputIds,amount);

        }
    }

    public void visbilityInput(ArrayList<Integer> inputIds,int i) {
        for (int n =0;n<i;n++){
            setVisibility(inputIds.get(n));
        }
        for(int n=4;n>i;n--) {
            Log.d("dd",n+"");
            EditText input = getView().findViewById(inputIds.get(n));
            input.setVisibility(View.GONE);
        }

    }
    public ArrayList<Integer> getInputIds(){
        inputIds.add(R.id.condition1);
        inputIds.add(R.id.condition2);
        inputIds.add(R.id.condition3);
        inputIds.add(R.id.condition4);
        inputIds.add(R.id.condition5);
        return inputIds;
    }
    public void setVisibility(int id){
        EditText input = getView().findViewById(id);
        input.setVisibility(View.VISIBLE);
    }
    public void getCollection(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();
        nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        collection = collectionView.saveCollection(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static String getEditText(Integer id,View view) {
        EditText editText = view.findViewById(id);
        return editText.getText().toString();
    }
    public void transpose(comic comic){
        Boolean newInCollection= true;
        condition = getCondition();
        for(ownedComic owned:collection){
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
    private class addToFireBase implements View.OnClickListener {
        @Override
        public void onClick(View view) {
        transpose(newComic.get(0));
        updateFireBase(collection,getContext());
        dismiss();
        setCondition(getContext(),condition);
        }
    }

    public static void setCondition(Context context,String condition) {
        SharedPreferences sharedPref1 = context.getSharedPreferences("newScores", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putString("newScores",condition);
        editor.apply();
    }

    public static void updateFireBase(final ArrayList<ownedComic> collection, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();
        nDatabase.child(userId.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().child("collection").setValue(collection);
                        mainActivity.backAdministration(false,context);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                }
        );
    }

    private class leave implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            dismiss();
        }
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

