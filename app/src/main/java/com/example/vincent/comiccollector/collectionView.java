package com.example.vincent.comiccollector;


import android.content.Context;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class collectionView extends Fragment {
    GridView collectionGrid;
    mainActivity mainActivity;

    public collectionView() {
        // Required empty public constructor
    }

    public String createLink(String query) {
        // Creates the link to access the Marvel API
        String link = getString(R.string.apiLink)+ query+ getString(R.string.limit);
        String timeStamp = System.currentTimeMillis() / 1000 + "";
        String privateKey = getString(R.string.privateKey);
        String publicKey = getString(R.string.publicKey);
        String combination = timeStamp + privateKey + publicKey;
        String hash = createHash(combination);
        return combineLink(link, timeStamp, hash);
    }

    public String combineLink(String link, String timeStamp, String hash) {
        // Creates the full link
        return link + "&ts=" + timeStamp + "&apikey=" + getString(R.string.publicKey) +
                "&hash=" + hash;
    }

    public String createHash(String combination) {
        // Creates the hash code to validate the keys
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(combination.getBytes(), 0, combination.length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "Something went wrong";
    }

    public void contactApi(final String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, createLink(query),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("test",JSONify(response).get(0).mainCharacter);
                        openInfo();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                contactApi(query);
            }
        });
        queue.add(stringRequest);
    }

    private void openInfo() {
        FragmentManager fm = getFragmentManager();
        comicInfo fragment = new comicInfo();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.commit();
    }

    public ArrayList<comic> JSONify(String response){
        ArrayList<comic> result = new ArrayList<comic>();
        JSONArray subArray = new JSONArray();
        try{
            JSONObject object =(JSONObject) new JSONTokener(response).nextValue();
            subArray = object.getJSONObject("data").getJSONArray("results");
            result = stripComics(subArray);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<comic> stripComics(JSONArray comics){
        ArrayList<comic> result = new ArrayList<comic>();
        JSONObject extracted = new JSONObject();
        for (int i=0;i<comics.length();i++) {
            try {
                extracted = comics.getJSONObject(i);
                result.add(storeComics(extracted));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public comic storeComics(JSONObject extracted) {
        comic result = null;
        String mainCharacter = getMainCharacter(extracted);
        try {
            result = new comic(extracted.getInt("id")
                    ,extracted.getString("title")
                    ,extracted.getDouble("issueNumber")
                    ,extracted.getString("description")
                    ,extracted.getJSONObject("thumbnail").getString("path")
                    ,extracted.getJSONObject("thumbnail").getString("extension")
                    ,extracted.getInt("pageCount")
                    ,extracted.getJSONObject("series").getString("name")
                    ,extracted.getJSONArray("dates").getJSONObject(0)
                    .getString("date").substring(0,4)
                    ,mainCharacter);

        } catch (JSONException e) {
            e.printStackTrace();

        }
        return result;
    }

    private String getMainCharacter(JSONObject extracted) {
        String result;
        try {
            result =extracted.getJSONObject("characters").getJSONArray("items")
                    .getJSONObject(0).getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
            result = "Unknown";
        }
        return result;
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
                        ArrayList<ownedComic> collection = saveCollection(dataSnapshot);
                        gridAdapter adapter = new gridAdapter(getContext(),collection,1);
                        GridView gridView = getView().findViewById(R.id.collectionGrid);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new showInfo());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public ArrayList<ownedComic> saveCollection(DataSnapshot dataSnapshot) {
        ArrayList<ownedComic> collectionList = new ArrayList<ownedComic>();
        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
            ownedComic comicBook = noteDataSnapshot.getValue(ownedComic.class);
            collectionList.add(comicBook);
        }
        return collectionList;
    }

    private class  showInfo implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ImageView comicImage = view.findViewById(R.id.icon);
            String comicId = comicImage.getContentDescription().toString();
            contactApi(comicId);


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

    }
}
