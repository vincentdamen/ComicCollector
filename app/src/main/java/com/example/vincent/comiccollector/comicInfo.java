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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class comicInfo extends Fragment {
    // prepare some variables that are used in the whole class.
    mainActivity mainActivity;
    static ArrayList<Integer> scoreId= new ArrayList<Integer>();
    static String errorHandler = "Is not available. I think it is an awesome comic!";
    FloatingActionButton add;
    FloatingActionButton addOwned;
    int comicId;
    boolean owned;
    String condition;

    // the method creates the possibility to open the fragment with an included variable.
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
        owned = getArguments().getBoolean("collected");
        comicId = getArguments().getInt("comicId");
        getInfo(comicId+"",view);
        return view;
    }

    // retrieves the comic from the Marvel API.
    public void getInfo(final String query, final View view) {
        // hide the navbar and show the loading screen.
        tools.hideNavBar(getActivity());
        tools.setLoadingScreen(R.id.loadingScreen,R.id.headLayout,view);

        // start the Volley-request to contact the marvel API.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tools.createLink(query,0),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Strip the JSONarray.
                        ArrayList<comic> result = JSONify(response);

                        // set the view.
                        setView(result);

                        // shows the navbar and hides the loading screen.
                        tools.showNavBar(getActivity());
                        tools.removeLoadingscreen(R.id.loadingScreen,R.id.headLayout,view);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());

                // retry if there is an error.
                getInfo(query,view);
            }
        });
        queue.add(stringRequest);
    }

    // strips the String to an Arraylist
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

    // strips the result value into seperate comics.
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

    // save each comic into the class.
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

    // extract the correct description.
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

    // finds the main character from the comic.
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

    // places all the information into the view.
    public void setView(ArrayList<comic> info) {
        owned=getOwned();
        comic information = info.get(0);
        setScrollableText(R.id.description);
        String title= information.title.split(" \\(")[0];
        tools.setTextView(R.id.title,title,getView());
        tools.setTextView(R.id.year,information.year,getView());
        tools.setTextView(R.id.issue,stripIssueNumber(information),getView());
        tools.setTextView(R.id.pageCount,information.pageCount+"",getView());
        tools.setTextView(R.id.series,information.series,getView());
        tools.setTextView(R.id.mainCharacter,information.mainCharacter,getView());
        tools.setTextView(R.id.description,information.description,getView());
        tools.setImageView(R.id.cover,information.thumbLink+"."+information.thumbExt
                ,getView(),getContext());
        // checks if the comic is owned.
        if(owned){
            // changes the buttons, which makes it possible to edit the comic.
            condition = getLatestCondition();
            addOwned = getView().findViewById(R.id.edit);
            addOwned.setOnClickListener(new addComic());
            add.setOnClickListener(new editComics());
            add.setImageResource(R.drawable.ic_create_white_24dp);
            add.setOnLongClickListener(new showaddOwned());
            setScores(condition, getView());}

        else{
            add.setOnLongClickListener(null);
            add.setImageResource(R.drawable.ic_add_black_24dp);
            add.setOnClickListener(new addComic());
            clearScores(getView());
        }
    }

    // clears all the scores from the view.
    public void clearScores(View view) {
        scoreId = setScoreId();
        for(int id:scoreId){
            tools.setTextView(id,"",view);
        }
    }

    // retrieves the issueNumber from the comic.
    public String stripIssueNumber(comic information) {
        Double issue = information.issueNumber;
        String result = issue.toString().split("\\.")[0];
        return result;
    }

    // makes the summary scrollable.
    public void setScrollableText(int id){
        TextView textView = getView().findViewById(id);
        textView.setMovementMethod(new ScrollingMovementMethod());

    }

    // sets the scores into the view.
    public static void setScores(String condition, View view) {
        ArrayList<Double> scores = stripScores(condition);
        scoreId = setScoreId();
        int size = Math.min(scoreId.size(),scores.size());
        for(int i=0; i<size;i++){
            tools.setTextView(scoreId.get(i),scores.get(i).toString(),view);
        }
    }

    // sorts the scores by extracting them from the string.
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

    // prepares scoreId to easily place the scores in the view.
    public static ArrayList<Integer> setScoreId(){
        scoreId.clear();
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

    // updates the shared preference of the score.
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

    // check if the comic is owned or not.
    public boolean getOwned() {
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("newScores", Context.MODE_PRIVATE);
        owned=sharedPref1.getBoolean("status",owned);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.remove("status");
        editor.apply();
        return owned;
    }

    // sets the OnClickListener to add scores.
    private class addComic implements FloatingActionButton.OnClickListener {
        @Override
        public void onClick(View view) {
            if (mainActivity.checkInternet(getContext())) {

                // opens the addComicDialog.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                addComicDialog fragment3 = new addComicDialog().newInstance(comicId);
                fragment3.show(ft, "dialog");
                // prevents wrong handling of the ondismiss.
                fragment3.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getInfo(comicId + "",getView());
                    }
                });
            }
        }
    }

    // sets the OnClickListener to edit scores.
    private class editComics implements FloatingActionButton.OnClickListener {

        @Override
        public void onClick(View view) {
            if(mainActivity.checkInternet(getContext())) {
                // opens the editComics dialog.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                editComic fragment4 = new editComic().newInstance(comicId);
                fragment4.show(ft, "dialog");
                fragment4.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                getInfo(comicId+"",getView());
                }
            });
        }
        }
    }


    @Override
    public void onResume() {
        if(mainActivity.checkInternet(getContext())) {
            // checks if the comic is now owned.
            owned = getOwned();
            getInfo(comicId + "",getView());
            super.onResume();
        }
    }

    // sets the OnLongClickListener to start the animation to add more scores.
    private class showaddOwned implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            showAddButton();
            setTimer();
            return true;
        }
    }

    // sets the animation.
    public void showAddButton(){
        Animation show_fab_1 = AnimationUtils.loadAnimation(getContext(), R.anim.show_fab_1);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
        addOwned.getLayoutParams();
        addOwned.setVisibility(View.VISIBLE);
        addOwned.setLayoutParams(layoutParams);
        addOwned.startAnimation(show_fab_1);
        addOwned.setClickable(true);}

    // sets a timer to start reversed animation.
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
