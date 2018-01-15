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

    // kijkt of de user is ingelogd
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

        // Hier worden de benodigde variabelen opgehaald
        EditText Name = getView().findViewById(R.id.username);
        EditText Age = getView().findViewById(R.id.age);

        // Hier wordt alle tekst opgehaald
        String sName = Name.getText().toString();
        String sAge = Age.getText().toString();

        // Hier wordt gekeken of de velden niet leeg zijn
        if (!Objects.equals(sName, "") & !Objects.equals(sAge, "")
                ){
            sendInfo(sName,sAge);
            Intent goToNextActivity = new Intent(getContext(), mainActivity.class);
            startActivity(goToNextActivity);
            getActivity().finish();
        }
    }

    // Hier wordt de userinfo opgeslagen
    public void sendInfo(final String Name, final String Age){
        // Hier worden de benodigde variabelen voorbereid
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        // Hier wordt firebase aangeroepen om informatie toe te schrijven
        user userInfo = new user(Name,Age,new ArrayList<comic>());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Users/"+user.getUid(),userInfo);
        database.updateChildren(childUpdates);

    }
}
