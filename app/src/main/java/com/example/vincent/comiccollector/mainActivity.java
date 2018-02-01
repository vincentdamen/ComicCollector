package com.example.vincent.comiccollector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class mainActivity extends AppCompatActivity {
    // handles the bottom navigation.
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_collection:
                    if(checkInternet(getApplicationContext())){
                        openCollection();
                        return true;}
                case R.id.navigation_browse:
                    if(checkInternet(getApplicationContext())){
                        openBrowse();
                        return true;}
                case R.id.navigation_search:
                    if(checkInternet(getApplicationContext())){
                        openSearch();
                        return true;}
                case R.id.navigation_settings:
                    signOut();
                    onStart();
                    return false;
            }
            return false;
        }};

    // checks if the user is logged in.
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

    // opens the collection view.
    public void openCollection(){
        SharedPreferences sharedPref = this.getSharedPreferences("showUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("uid");
        editor.apply();
        FragmentManager fm = getSupportFragmentManager();
        collectionView fragment = new collectionView();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.commit();
    }

    // opens the browse comics.
    public void openBrowse(){
        FragmentManager fm = getSupportFragmentManager();
        browseComic fragment1 = new browseComic();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment1);
        ft.commit();
    }

    // opens search user.
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
        // checks login.
        if (checkLogin()) {
            setContentView(R.layout.activity_main);
            checkInternet(getApplicationContext());
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

    // manages the backnavigation.
    public static void backAdministration(boolean home,Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("home", home);
        editor.apply();
    }

    // flips the action of the backnavigation.
    public void reverseBackAdministration(){
        SharedPreferences sharedPref1 = this.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putBoolean("home", !checkBackAdministration());
        editor.apply();
    }

    // saves the offset in shared preferences.
    public static void saveOffset(String offset,Context context){
        SharedPreferences sharedPref1 = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref1.edit();
        editor.putString("offset", offset);
        editor.apply();
    }

    // retrieves the offset from shared preferences.
    public static String getOffset(Context context){
        SharedPreferences sharedPref =  context.getSharedPreferences("home",Context.MODE_PRIVATE);
        return sharedPref.getString("offset","null");
    }

    // removes the offset from shared preferences.
    public static  void removeOffset(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("home",Context.MODE_PRIVATE);
        sharedPref.edit().remove("offset").apply();
    }

    // checks the current state of the backnavigation.
    public boolean checkBackAdministration(){
        SharedPreferences sharedPref = getApplication().getSharedPreferences("home",Context.MODE_PRIVATE);
        boolean state = sharedPref.getBoolean("home",true);
        return state;
    }
    // logout dialog.
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

    // checks if there's an internet connection.
    public static Boolean checkInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            Toast.makeText(context, R.string.noInternet,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setVisibility(View.VISIBLE);
        // if not true, then don't kill the app.
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
