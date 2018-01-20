package com.example.vincent.comiccollector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
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

public class mainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_collection:
                    openFragment();
                    return true;
                case R.id.navigation_browse:
                    return true;
                case R.id.navigation_search:
                    return true;
                case R.id.navigation_settings:
                    FirebaseAuth.getInstance().signOut();
                    onStart();
                    return true;
            }
            return false;
        }};


    public boolean checkLogin(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent goToNextActivity = new Intent(getBaseContext(), authentication.class);
            startActivity(goToNextActivity);
            finish();
            return false;
        }
    return true;}


    public void openFragment(){
        FragmentManager fm = getSupportFragmentManager();
        collectionView fragment = new collectionView();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.addToBackStack(null).commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkLogin()) {
            setContentView(R.layout.activity_main);
            openFragment();
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        checkLogin();
        backAdministration(true,getApplicationContext());
    }

    public static void backAdministration(boolean home,Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("home", home);
        editor.apply();
    }

    public boolean checkBackAdministration(){
        SharedPreferences sharedPref = getApplication().getSharedPreferences("home",Context.MODE_PRIVATE);
        boolean state = sharedPref.getBoolean("home",true);
        return state;
    }
    @Override
    public void onBackPressed() {
        // Hier worden de benodigde variabelen benoemd
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);
        if (!checkBackAdministration()) {
            super.onBackPressed();
        }
        else{
            finish();
        }
    }

}
