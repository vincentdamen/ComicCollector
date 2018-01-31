package com.example.vincent.comiccollector;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public class tools {
    // sets required variables.
    static String apiLink = "http://gateway.marvel.com/v1/public/comics";
    static String limit = "?limit=50";
    static String privateKey =  "160ba682255404c4190a9076642cacae20f9f4cf";
    static String publicKey = "25119df35812e08ad556e1341e548b06";
    static String backUpLink = "http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available/portrait_uncanny.jpg";
    static String deadLink = "http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available.jpg";

    // Creates the link to access the Marvel API
    public static String createLink(String query,int type) {
        String link = placeQuery(query,type);
        String timeStamp = System.currentTimeMillis() / 1000 + "";
        String combination = timeStamp + privateKey + publicKey;
        String hash = createHash(combination);
        return combineLink(link, timeStamp, hash);
    }

    // hides navbar
    public static void hideNavBar(Activity activity){
        final BottomNavigationView navigation =
                (BottomNavigationView) activity.findViewById(R.id.navigation);
        navigation.setVisibility(View.GONE);
    }

    // shows navbar.
    public static void showNavBar(Activity activity) {
        final BottomNavigationView navigation =
                (BottomNavigationView) activity.findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);
    }

    // shows loadingscreen and hides the content.
    public static void setLoadingScreen(int containerLoading, int containerContent, View view){
        ConstraintLayout loading = view.findViewById(containerLoading);
        ConstraintLayout content = view.findViewById(containerContent);
        loading.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }

    // hides loadingscreen and shows the content.
    public static void removeLoadingscreen(int containerLoading, int containerContent, View view){
        ConstraintLayout loading = view.findViewById(containerLoading);
        ConstraintLayout content = view.findViewById(containerContent);
        loading.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    // check type of query.
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

    // creates link for Volley.
    public static String combineLink(String link, String timeStamp, String hash) {
        // Creates the full link
        return link + "&ts=" + timeStamp + "&apikey=" + publicKey +
                "&hash=" + hash;
    }

    // creates hash code, to verify identity.
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

    // sets textview.
    public static void setTextView(Integer id, String input,View view) {
        TextView textView = view.findViewById(id);
        textView.setText(input);
    }

    // sets imageview.
    public static void setImageView(Integer id, String link, View view, Context context) {
        ImageView imageView = view.findViewById(id);

        if(Objects.equals(link, deadLink)){
            link = backUpLink;
        }
        Glide.with(context).load(link).into(imageView);
    }

}
