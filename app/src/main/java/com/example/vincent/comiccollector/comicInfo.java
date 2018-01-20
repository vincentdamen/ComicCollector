package com.example.vincent.comiccollector;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class comicInfo extends Fragment {
    mainActivity mainActivity;
    ArrayList<Integer> scoreId= new ArrayList<Integer>();


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
                        ArrayList<comic> result = JSONify(response);
                        setView(result);

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
        String description = getDescription(extracted);
        try {
            result = new comic(extracted.getInt("id")
                    ,extracted.getString("title")
                    ,extracted.getDouble("issueNumber")
                    ,description
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

    public String getDescription(JSONObject extracted) {
        String result= null;
        try {
            result = extracted.getString("description");
            if(result=="null"){
                result=getString(R.string.fix_summary);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }



    public String getMainCharacter(JSONObject extracted) {
        String result;
        try {
            result =extracted.getJSONObject("characters").getJSONArray("items")
                    .getJSONObject(0).getString("name");
        } catch (JSONException e) {
            result = "Unknown";
        }
        return result;
    }

    public comicInfo newInstance( Boolean collected, int comicId,String condition ) {
        comicInfo f = new comicInfo();
        Bundle args = new Bundle();
        args.putBoolean("collected", collected);
        args.putInt("comicId",comicId);
        args.putString("condition",condition);
        f.setArguments(args);
        return f;
    }
    public comicInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_comic_info, container, false);
        if(getArguments().getBoolean("collected")){
            String condition = getArguments().getString("condition");
            view = setScores(condition, view);
        }
        int comicId = getArguments().getInt("comicId");
        contactApi(comicId+"");
        return view;
    }

    public void setView(ArrayList<comic> info) {
        comic information = info.get(0);
        String title= information.title.split(" \\(")[0];
        setTextView(R.id.title,title,getView());
        setTextView(R.id.year,information.year,getView());
        setTextView(R.id.issue,information.issueNumber+"",getView());
        setTextView(R.id.pageCount,information.pageCount+"",getView());
        setTextView(R.id.series,information.series,getView());
        setTextView(R.id.mainCharacter,information.mainCharacter,getView());
        setTextView(R.id.description,information.description,getView());
        setImageView(R.id.cover,information.ThumbLink+"."+information.ThumbExt
                ,getView(),getContext());
    }



    public View setScores(String condition, View view) {
        ArrayList<Double> scores = stripScores(condition);
        scoreId = setScoreId();
        for(int i=0; i<scores.size();i++){
            setTextView(scoreId.get(i),scores.get(i).toString(),view);
        }
        return view;
    }

    public void setTextView(Integer id, String input,View view) {
        TextView textView = view.findViewById(id);
        textView.setText(input);
    }
    public void setImageView(Integer id, String link, View view, Context context) {
        ImageView imageView = view.findViewById(id);
        Glide.with(context).load(link).into(imageView);
    }
    public ArrayList<Double> stripScores(String condition) {
        ArrayList<Double> result = new ArrayList<Double>();
        String[] values = condition.split(",");
        for (String value : values) {
            result.add(Double.parseDouble(value));
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    public ArrayList<Integer> setScoreId(){
        scoreId.add(R.id.book1);
        scoreId.add(R.id.book2);
        scoreId.add(R.id.book3);
        scoreId.add(R.id.book4);
        scoreId.add(R.id.book5);
        return scoreId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity.backAdministration(false,getContext());
    }
}
