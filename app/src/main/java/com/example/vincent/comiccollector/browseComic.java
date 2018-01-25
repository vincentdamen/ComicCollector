package com.example.vincent.comiccollector;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;



/**
 * A simple {@link Fragment} subclass.
 */
public class browseComic extends Fragment {
    mainActivity mainActivity;
    collectionView collectionView;
    ArrayList<ownedComic> transposed;
    ArrayList<ownedComic> collection;
    comicInfo comicInfo;
    String offset;

    public browseComic() {
    }

    public String randomOffset(){
        Random rand = new Random();
        return rand.nextInt(41007) + 1+"";
    }

    public void getComics(final String query){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String link = comicInfo.createLink(query,1);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<comic> result = comicInfo.JSONify(response);
                        transposed = prepareComic(result);
                        gridAdapter adapter = new gridAdapter(getContext(),transposed,1);
                        GridView gridView = getView().findViewById(R.id.browseGrid);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new viewComic());
                        gridView.setOnItemLongClickListener(new checkTitle());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                getComics(query);
            }
        });
        queue.add(stringRequest);
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

    private class checkTitle implements AdapterView.OnItemLongClickListener{

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            CharSequence text = transposed.get(i).title;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
            return true;
        }
    }

    private class viewComic implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            boolean owned =false;
            for(ownedComic comic:collection){
                if (comic.comicId==transposed.get(i).comicId){
                    owned=true;
                    openInfo(comic.comicId,comic.condition,owned);
                }
            }
            if(!owned){
            int comicId = transposed.get(i).comicId;
            String condition = transposed.get(i).condition;
            openInfo(comicId,condition,owned);}
        }
    }
    public ArrayList<ownedComic> prepareComic(ArrayList<comic> input) {
        transposed = new ArrayList<ownedComic>();
        for(comic comic:input){
            ownedComic transformer = new ownedComic(comic.id,
                    "",
                    comic.thumbExt,
                    comic.thumbLink,
                    comic.title);
            transposed.add(transformer);
            }
        return transposed;
    }

    public void openInfo(int comicId,String condition,Boolean owned) {
        mainActivity.backAdministration(false,getContext());
        FragmentManager fm = getFragmentManager();
        comicInfo fragment = new comicInfo().newInstance(owned,comicId,condition);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.addToBackStack(null).commit();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse_comic, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCollection();
        String oldOffset= mainActivity.getOffset(getContext());
        if(!Objects.equals(oldOffset, "null")){
            offset = oldOffset;}
        else{
            offset = randomOffset();
            mainActivity.saveOffset(offset,getContext());}
        getComics(offset);
        mainActivity.backAdministration(true,getContext());
    }
    @Override
    public void onResume() {
        getComics(offset);
        mainActivity.backAdministration(true,getContext());
        super.onResume();
    }


}
