package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class comicInfo extends Fragment {
    mainActivity mainActivity;
    static ArrayList<Integer> scoreId= new ArrayList<Integer>();
    static String apiLink = "http://gateway.marvel.com/v1/public/comics";
    static String limit = "?limit=50";
    static String privateKey =  "160ba682255404c4190a9076642cacae20f9f4cf";
    static String publicKey = "25119df35812e08ad556e1341e548b06";
    static String errorHandler = "Is not available. I think it is an awesome comic!";
    static String backUpLink = "http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available/portrait_uncanny.jpg";
    static String deadLink = "http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available.jpg";
    FloatingActionButton add;
    FloatingActionButton addOwned;
    int comicId;
    boolean owned;
    String condition;


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
        add = view.findViewById(R.id.add);
        add.setOnClickListener(new addComic());
        owned = getArguments().getBoolean("collected");
        if(owned){
            addOwned = view.findViewById(R.id.edit);
            addOwned.setOnClickListener(new addComic());
            add.setOnClickListener(new editComics());
            add.setImageResource(R.drawable.ic_create_white_24dp);
            add.setOnLongClickListener(new showaddOwned());
            condition = getLatestCondition();

        }
        comicId = getArguments().getInt("comicId");
        getInfo(comicId+"");
        return view;
    }

    public static String createLink(String query,int type) {
        // Creates the link to access the Marvel API
        String link = placeQuery(query,type);
        String timeStamp = System.currentTimeMillis() / 1000 + "";
        String combination = timeStamp + privateKey + publicKey;
        String hash = createHash(combination);
        return combineLink(link, timeStamp, hash);
    }

    public static String placeQuery(String query, int type) {
        // Specific comic
        if(type==0){
            return apiLink+"/"+ query+ limit;
        }
        // Offset
        else if (type==1){
            return apiLink+limit+"&offset="+query;
        }
        return apiLink+limit;
    }

    public static String combineLink(String link, String timeStamp, String hash) {
        // Creates the full link
        return link + "&ts=" + timeStamp + "&apikey=" + publicKey +
                "&hash=" + hash;
    }

    public static String createHash(String combination) {
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

    public void getInfo(final String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, createLink(query,0),
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
                getInfo(query);
            }
        });
        queue.add(stringRequest);
    }

    public static ArrayList<comic> JSONify(String response){
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

    public static ArrayList<comic> stripComics(JSONArray comics){
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

    public static comic storeComics(JSONObject extracted) {
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

    public static String getDescription(JSONObject extracted) {
        String result= null;
        try {
            result = extracted.getString("description");
            if(Objects.equals(result, "null")){
                result=errorHandler;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getMainCharacter(JSONObject extracted) {
        String result;
        try {
            result =extracted.getJSONObject("characters").getJSONArray("items")
                    .getJSONObject(0).getString("name");
        } catch (JSONException e) {
            result = "Unknown";
        }
        return result;
    }

    public void setView(ArrayList<comic> info) {
        comic information = info.get(0);
        setScrollableText(R.id.description);
        String title= information.title.split(" \\(")[0];
        setTextView(R.id.title,title,getView());
        setTextView(R.id.year,information.year,getView());
        setTextView(R.id.issue,stripIssueNumber(information),getView());
        setTextView(R.id.pageCount,information.pageCount+"",getView());
        setTextView(R.id.series,information.series,getView());
        setTextView(R.id.mainCharacter,information.mainCharacter,getView());
        setTextView(R.id.description,information.description,getView());
        setImageView(R.id.cover,information.thumbLink+"."+information.thumbExt
                ,getView(),getContext());
        if(owned){
        setScores(getLatestCondition(), getView());}
    }

    public String stripIssueNumber(comic information) {
        Double issue = information.issueNumber;
        String result = issue.toString().split("\\.")[0];
        return result;
    }

    public void setScrollableText(int id){
        TextView textView = getView().findViewById(id);
        textView.setMovementMethod(new ScrollingMovementMethod());

    }

    public static void setScores(String condition, View view) {
        ArrayList<Double> scores = stripScores(condition);
        scoreId = setScoreId();
        int size = Math.min(scoreId.size(),scores.size());
        for(int i=0; i<size;i++){
            setTextView(scoreId.get(i),scores.get(i).toString(),view);
        }
    }

    public static void setTextView(Integer id, String input,View view) {
        TextView textView = view.findViewById(id);
        textView.setText(input);
    }


    public static void setImageView(Integer id, String link, View view, Context context) {
        ImageView imageView = view.findViewById(id);

        if(Objects.equals(link, deadLink)){
            link = backUpLink;
        }
        Glide.with(context).load(link).into(imageView);
    }

    public static ArrayList<Double> stripScores(String condition) {
        ArrayList<Double> result = new ArrayList<Double>();
        String[] values = condition.split(",");
        for (String value : values) {
            result.add(Double.parseDouble(value));
        }
        Collections.sort(result);
        Collections.reverse(result);
        return result;
    }

    public static ArrayList<Integer> setScoreId(){
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

    public String getLatestCondition() {
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("newScores", Context.MODE_PRIVATE);
        String latestCondition = sharedPref1.getString("newScores","null");
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.clear().apply();
        if(latestCondition=="null"){
            latestCondition=getArguments().getString("condition");

        }
        return latestCondition;
    }


    private class addComic implements FloatingActionButton.OnClickListener {
        @Override
        public void onClick(View view) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            addComicDialog fragment3 = new addComicDialog().newInstance(comicId);
            fragment3.show(ft, "dialog");
            fragment3.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getInfo(comicId+"");
                }
            });
        }
    }

    private class editComics implements FloatingActionButton.OnClickListener {

        @Override
        public void onClick(View view) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            editComic fragment4 = new editComic().newInstance(comicId);
            fragment4.show(ft, "dialog");
            fragment4.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    getInfo(comicId+"");
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getInfo(comicId+"");
        }



    private class showaddOwned implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            showAddButton();
            setTimer();
            return true;
        }
    }
    public void showAddButton(){
        Animation show_fab_1 = AnimationUtils.loadAnimation(getContext(), R.anim.show_fab_1);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
        addOwned.getLayoutParams();
        addOwned.setVisibility(View.VISIBLE);
        addOwned.setLayoutParams(layoutParams);
        addOwned.startAnimation(show_fab_1);
        addOwned.setClickable(true);}

    public void setTimer(){
        CountDownTimer timer = new CountDownTimer(4000, 100) {
            @Override
            public void onTick(long l) {

            }
            public void onFinish() {
                Animation hide_fab_1 = AnimationUtils.loadAnimation(getContext(), R.anim.hide_fab_1);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                        addOwned.getLayoutParams();
                addOwned.setLayoutParams(layoutParams);
                addOwned.startAnimation(hide_fab_1);
                addOwned.setClickable(false);
            }};
        timer.start();
    }

}
