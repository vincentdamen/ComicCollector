package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class collectionView extends Fragment {
    mainActivity mainActivity;
    ArrayList<ownedComic> collection = new ArrayList<ownedComic>();
    String uid;
    boolean otherUser=false;
    public collectionView() {
        // Required empty public constructor
    }


    public void openInfo(int comicId,String condition) {
        FragmentManager fm = getFragmentManager();
        comicInfo fragment = new comicInfo().newInstance(true,comicId,condition);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.addToBackStack(null).commit();
    }
    public void getCollection(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        uid=checkOtherUser();
        Log.d("uid",uid);

        nDatabase.child(uid).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("updated","true");
                        collection = saveCollection(dataSnapshot);
                        gridAdapter adapter = new gridAdapter(getContext(),collection,1);
                        GridView gridView = getView().findViewById(R.id.collectionGrid);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new showInfo());
                        gridView.setOnItemLongClickListener(new checkTitle());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public String checkOtherUser() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("showUser", Context.MODE_PRIVATE);
        if(!Objects.equals(sharedPref.getString("uid", "null"), "null")) {
            uid=sharedPref.getString("uid","null");
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("uid");
            editor.apply();
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

    public static ArrayList<ownedComic> saveCollection(DataSnapshot dataSnapshot) {
        ArrayList<ownedComic> collectionList = new ArrayList<ownedComic>();
        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
            ownedComic comicBook = noteDataSnapshot.getValue(ownedComic.class);
            collectionList.add(comicBook);
        }
        return collectionList;
    }

    public class  showInfo implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_collection_view, container,
                false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCollection();
        mainActivity.backAdministration(true,getContext());

    }

    @Override
    public void onResume() {
        if(otherUser) {
            mainActivity.backAdministration(false, getContext());


        }
        else{

            getCollection();
            mainActivity.backAdministration(true, getContext());}
        super.onResume();
    }
}