package com.example.vincent.comiccollector;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class mainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_collection:
                    mTextMessage.setText(R.string.my_collection);
                    return true;
                case R.id.navigation_browse:
                    mTextMessage.setText(R.string.browse);
                    return true;
                case R.id.navigation_search:
                    mTextMessage.setText(R.string.search);
                    return true;
                case R.id.navigation_settings:
                    mTextMessage.setText(R.string.settings);
                    return true;

            }
            return false;
        }
    };
    public String createLink(String query){
        // Creates the link to access the Marvel API
        String link = getString(R.string.apiLink)+query;
        String timeStamp = System.currentTimeMillis()/1000 + "" ;
        String privateKey = getString(R.string.privateKey);
        String publicKey = getString(R.string.publicKey);
        String hash = createHash(publicKey,privateKey,timeStamp);

        return combineLink(link,publicKey,timeStamp,hash);
    }
    public String combineLink(String link,String publicKey, String timeStamp, String hash){
        // Creates the full link
        return link + "&ts="+timeStamp+"&apikey="+publicKey+"&hash="+hash;
    }
    public String createHash(String publicKey,String privateKey, String timeStamp){
        // Creates the hash code to validate the keys
        String combination = timeStamp+ privateKey+ publicKey;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(combination.getBytes(),0,combination.length());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "Something went wrong";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        String url = createLink("");
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextMessage.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextMessage.setText(error.toString());
            }
        });
        queue.add(stringRequest);

    }

}
