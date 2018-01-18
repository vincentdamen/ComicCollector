package com.example.vincent.comiccollector;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class authentication extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        FragmentManager fm = getSupportFragmentManager();
        loginMenu fragment = new loginMenu();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameTarget , fragment);
        ft.commit();
    }


    @Override
    public void onBackPressed() {
        // Deze override voorkomt dat je terug kan geen naar de app haar home screen
        finish();
    }
}
