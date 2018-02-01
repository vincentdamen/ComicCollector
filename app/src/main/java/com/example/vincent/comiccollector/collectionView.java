package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
public class collectionView extends Fragment {
    // prepare some variables that are used in the whole class.
    mainActivity mainActivity;
    ArrayList<ownedComic> collection = new ArrayList<ownedComic>();
    String uid;
    boolean otherUser=false;

    public collectionView() {
        // Required empty public constructor
    }

    // opens comicInfo.
    public void openInfo(int comicId,String condition) {
        // checks internet connection.
        if(mainActivity.checkInternet(getContext())) {
            FragmentManager fm = getFragmentManager();
            comicInfo fragment = new comicInfo();
            // checks if it's the own collection or not.
            if (!otherUser) {
                fragment = new comicInfo().newInstance(true, comicId, condition);
            } else {
                String ownCondition = getScore(comicId);
                Boolean ownedByUser = !Objects.equals(ownCondition, "null");
                fragment = new comicInfo().newInstance(ownedByUser, comicId, ownCondition);
            }
            // open the fragment.
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.targetFrame, fragment);
            ft.addToBackStack(null).commit();
        }
    }

    // retrieve the score from the shared preference.
    public String getScore(int comicId) {
        SharedPreferences sharedPref = getContext().getSharedPreferences("ownCollection", Context.MODE_PRIVATE);
        return sharedPref.getString("comicId_"+comicId,"null");
    }

    // retrieves the collection from FireBase.
    public void getCollection(final View view){
        // hide the navbar and show the loading screen.
        tools.hideNavBar(getActivity());
        tools.setLoadingScreen(R.id.loadingScreen,R.id.content,view);

        // sets the required variables.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        uid=checkOtherUser();
        nDatabase.child(uid).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // saves the collection to the variable.
                        collection = saveCollection(dataSnapshot);

                        // stores the scores.
                        storeScores();
                        // sets the grid.
                        gridAdapter adapter = new gridAdapter(getContext(),collection,1);
                        GridView gridView = view.findViewById(R.id.collectionGrid);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new showInfo());
                        gridView.setOnItemLongClickListener(new checkTitle());

                        // shows the navbar and hides the loading screen.
                        tools.showNavBar(getActivity());
                        tools.removeLoadingscreen(R.id.loadingScreen,R.id.content,view);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("cancel error", databaseError.getMessage());
                    }
                });
    }

    // store the scores in the shared preference.
    public void storeScores() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("ownCollection", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for(ownedComic comic:collection){
            editor.putString("comicId_"+comic.comicId,comic.condition);
        }
        editor.apply();
    }

    // check if the fragment is opened to show an other user.
    public String checkOtherUser() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("showUser", Context.MODE_PRIVATE);
        if(!Objects.equals(sharedPref.getString("uid", "null"), "null")) {
            uid=sharedPref.getString("uid","null");
            otherUser=true;
            mainActivity.backAdministration(false,getContext());
        }
        else{
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser userId = mAuth.getCurrentUser();
            uid=userId.getUid();
        }
        return uid;

    }


    // saves the collection to an arraylist.
    public static ArrayList<ownedComic> saveCollection(DataSnapshot dataSnapshot) {
        ArrayList<ownedComic> collectionList = new ArrayList<ownedComic>();
        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
            ownedComic comicBook = noteDataSnapshot.getValue(ownedComic.class);
            collectionList.add(comicBook);
        }
        return collectionList;
    }

    // sets the OnItemClickListener for each item, which opens the comicInfo.
    public class  showInfo implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            setOtherUser(otherUser);
            int comicId = collection.get(i).comicId;
            String condition = collection.get(i).condition;
            mainActivity.backAdministration(false,getContext());
            openInfo(comicId,condition);


        }
    }
    private class checkTitle implements android.widget.AdapterView.OnItemLongClickListener{

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            CharSequence text = collection.get(i).title;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
            return true;
        }
    }

    // set other user shared preference, to remove buttons.
    public void setOtherUser(boolean state){
        SharedPreferences sharedPref = getActivity().getSharedPreferences("otherUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("otherUser",state);
        editor.apply();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_collection_view, container,
                false);
        // gets the collection.
        getCollection(view);
        mainActivity.backAdministration(true,getContext());
        return view;
    }

    @Override
    public void onResume() {
        if(mainActivity.checkInternet(getContext())) {
            if (otherUser) {
                mainActivity.backAdministration(false, getContext());
            } else {

                getCollection(getView());
                mainActivity.backAdministration(true, getContext());
            }
            super.onResume();
        }
    }
}