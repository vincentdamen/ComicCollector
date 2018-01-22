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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    comicInfo comicInfo;

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

    private class viewComic implements android.widget.AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            int comicId = transposed.get(i).comicId;
            String condition = transposed.get(i).condition;
            openInfo(comicId,condition);
        }
    }
    public ArrayList<ownedComic> prepareComic(ArrayList<comic> input) {
        ArrayList<ownedComic> result = new ArrayList<ownedComic>();
        for(comic comic:input){
            ownedComic transformer = new ownedComic(comic.id,
                    0,
                    "",
                    comic.thumbExt,
                    comic.thumbLink);
            result.add(transformer);
        }
        return result;
    }

    public void openInfo(int comicId,String condition) {
        mainActivity.backAdministration(false,getContext());
        FragmentManager fm = getFragmentManager();
        comicInfo fragment = new comicInfo().newInstance(false,comicId,condition);
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.targetFrame, fragment);
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
        getComics(randomOffset());
        mainActivity.backAdministration(true,getContext());


    }


}
