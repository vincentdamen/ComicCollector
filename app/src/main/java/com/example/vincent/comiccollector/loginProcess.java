package com.example.vincent.comiccollector;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class loginProcess extends DialogFragment implements View.OnClickListener{


    // Checkt of de input in je mail edittext een mail adres en je lengte van je password
    public boolean CheckInfo (CharSequence target,CharSequence password) {
        return !(target == null || password == null) && password.length() > 6 &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public loginProcess() {
    }

    // Dit maak de fragment view en  zorgt dat je niet weggaat als je buiten de dialog klikt
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_process, container,
                false);
        getDialog().setCanceledOnTouchOutside(false);

        // Dit benoemd de Login knop en zet een OnClickListener
        Button login =  view.findViewById(R.id.buttonLogin);
        login.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        // Checkt welke id het is
        switch (view.getId()){
            case(R.id.buttonLogin):

                // Hier worden de benodigde variabelen benoemd
                EditText mail =  (EditText) getDialog().findViewById(R.id.emailLogin);
                EditText password =(EditText) getDialog().findViewById(R.id.passwordLogin);

                // Hier wordt de input opgehaald
                String email = mail.getText().toString();
                String passwords = password.getText().toString();

                // Hier wordt gecheckt of de input niet leeg is
                if(email.length()>0 & passwords.length()>0){
                    if (CheckInfo(email,passwords)) {
                        checkLogin(email,passwords);
                    }

                    // Hier worden errors aan de user gecommuniceerd
                    else{
                        TextView errorBlock = getDialog().findViewById(R.id.errorBlock);
                        errorBlock.setText(R.string.wrongInput);
                        errorBlock.setTextColor(getResources().getColor(R.color.error));
                    }
                }else{
                    TextView errorBlock = getDialog().findViewById(R.id.errorBlock);
                    errorBlock.setText(R.string.error_empty_fields);
                    errorBlock.setTextColor(getResources().getColor(R.color.error));
                }
                break;
        }
    }

    // Hier wordt ingelogd
    public void checkLogin(String email, String Passwords){
        // Hier wordt firebase opgehaald en gekeken of de inloggegevens kloppen
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, Passwords)
                .addOnCompleteListener( getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // als Login correct is
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getContext(), mainActivity.class);
                            getActivity().finish();
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            TextView errorBlock = getDialog().findViewById(R.id.errorBlock);
                            errorBlock.setText(R.string.loginFailed);
                            errorBlock.setTextColor(getResources().getColor(R.color.error));
                        }

                    }
                });
    }
}