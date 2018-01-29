package com.example.vincent.comiccollector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import java.util.Objects;

public class mainActivity extends AppCompatActivity {
    comicInfo comicInfo;
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_collection:
                    openCollection();
                    return true;
                case R.id.navigation_browse:
                    openBrowse();
                    return true;
                case R.id.navigation_search:
                    openSearch();
                    return true;
                case R.id.navigation_settings:
                    signOut();
                    onStart();
                    return false;
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


    public void openCollection(){

        FragmentManager fm = getSupportFragmentManager();
        collectionView fragment = new collectionView();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.commit();
    }

    public void openBrowse(){
        FragmentManager fm = getSupportFragmentManager();
        browseComic fragment1 = new browseComic();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment1);
        ft.commit();
    }
    public void openSearch(){
        FragmentManager fm = getSupportFragmentManager();
        searchUsers fragment1 = new searchUsers();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment1);
        ft.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkLogin()) {
            setContentView(R.layout.activity_main);
            openCollection();
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
    public void reverseBackAdministration(){
        SharedPreferences sharedPref1 = this.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putBoolean("home", !checkBackAdministration());
        editor.apply();
    }
    public static void saveOffset(String offset,Context context){
        SharedPreferences sharedPref1 = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putString("offset", offset);
        editor.apply();
    }
    public static String getOffset(Context context){
        SharedPreferences sharedPref =  context.getSharedPreferences("home",Context.MODE_PRIVATE);
        return sharedPref.getString("offset","null");
    }
    public static  void removeOffset(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        sharedPref.edit().remove("offset").apply();
    }
    public boolean checkBackAdministration(){
        SharedPreferences sharedPref = getApplication().getSharedPreferences("home",Context.MODE_PRIVATE);
        boolean state = sharedPref.getBoolean("home",true);
        return state;
    }

    public void signOut() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mainActivity.this,R.style.AlertDialogCustom)).create();
        alertDialog.setMessage("Do you really want to leave your collection  behind?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Stay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        onStart();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Hier worden de benodigde variabelen benoemd
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);
        if (!checkBackAdministration()) {
            super.onBackPressed();
            reverseBackAdministration();
        }
        else{
            removeOffset(getApplicationContext());
            finish();
        }
    }




}
