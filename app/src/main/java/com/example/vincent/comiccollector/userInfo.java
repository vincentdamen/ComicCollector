package com.example.vincent.comiccollector;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class userInfo extends Fragment implements View.OnClickListener {
    public userInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
        View view= inflater.inflate(R.layout.fragment_user_info, container, false);
        FloatingActionButton Continue = view.findViewById(R.id.send);
        Continue.setOnClickListener(this);
        return view;
    }

    // checks if user is logged in.
    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent goToNextActivity = new Intent(getContext(), authentication.class);
            startActivity(goToNextActivity);}
    }

    @Override
    public void onClick(View view) {
        if(mainActivity.checkInternet(getContext())) {

        // sets required variables.
        EditText Name = getView().findViewById(R.id.username);
        EditText Age = getView().findViewById(R.id.age);

        // retrieves required information.
        String sName = Name.getText().toString();
        String sAge = Age.getText().toString();

        // checks if input boxes aren't empty.
        if (!Objects.equals(sName, "") & !Objects.equals(sAge, "")
                ){
            sendInfo(sName,sAge);
            Intent goToNextActivity = new Intent(getContext(), mainActivity.class);
            startActivity(goToNextActivity);
            getActivity().finish();
        }
    }
    }

    // sends userInfo to Firebase.
    public void sendInfo(final String Name, final String Age){
        // sets required variables.
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        // calls firebase to store values.
        user userInfo = new user(Name,Age,new ArrayList<comic>());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Users/"+user.getUid(),userInfo);
        database.updateChildren(childUpdates);

    }
}
