package com.example.vincent.comiccollector;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import static android.content.ContentValues.TAG;


public class registerProcess extends DialogFragment implements View.OnClickListener{
    // Hier wordt de email en het password gecheckt op de requirements
    public boolean CheckInfo (CharSequence target,CharSequence password) {
        return !(target == null || password == null) && password.length() > 6 &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public registerProcess() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_process, container, false);
        // Dit voorkomt dat je weg kan klikken op het scherm
        getDialog().setCanceledOnTouchOutside(false);
        Button register =  view.findViewById(R.id.registerButton);
        register.setOnClickListener(this);
        return view;
    }
    @Override
    public void onClick(View view) {
        // Dit regelt de onclick van de aanmeldknop
        switch (view.getId()){
            case(R.id.registerButton):
                // Hier worden de benodigde variabelen opgehaald
                EditText mail =  (EditText) getDialog().findViewById(R.id.emailRegister);
                EditText password =(EditText) getDialog().findViewById(R.id.passwordRegister);
                EditText control =(EditText) getDialog().findViewById(R.id.passwordControl);

                // Hier wordt alle tekst opgehaald
                String controls = control.getText().toString();
                String email = mail.getText().toString();
                String passwords = password.getText().toString();

                // Hier wordt gekeken of de velden niet leeg zijn
                if(email.length()>0 & passwords.length()>0 & control.length()>0 &
                        Objects.equals(passwords, controls)){

                    // Hier wordt gekeken of ze aan de voorwaarden voldoen
                    if (CheckInfo(email,passwords)) {

                        // Hier wordt het account gecreeerd
                        createAccount(email,passwords);
                    }

                    else{

                        // Hier wordt errors verteld aan de user
                        TextView errorBlock = getDialog().findViewById(R.id.errorR);
                        errorBlock.setText(R.string.wrongInput);
                        errorBlock.setTextColor(getResources().getColor(R.color.error));
                    }
                }else{

                    // Hier wordt errors verteld aan de user
                    TextView errorBlock = getDialog().findViewById(R.id.errorR);
                    errorBlock.setText(R.string.error_empty_fields);
                    errorBlock.setTextColor(getResources().getColor(R.color.error));
                }
                break;
        }
    }


    // Hier wordt de user geregisteerd in firebase en wordt ingelogd
    public void createAccount(final String email, final String Passwords){
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, Passwords)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkLogin(email,Passwords);
                            StartPersonalize();
                            // Sign in success, update UI with the signed-in user's information


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    // Hier wordt gebuiker ingelogd en doorgestuurd naar de extra info fragment
    public void checkLogin(String email, String Passwords){
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, Passwords)
                .addOnCompleteListener( getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                        } else {
                            // If sign in fails, display a message to the user.
                            TextView errorBlock = getDialog().findViewById(R.id.errorR);
                            errorBlock.setText(R.string.loginFailed);
                            errorBlock.setTextColor(getResources().getColor(R.color.error));
                        }

                    }
                });
    }
    // Dit start de personalisatie van de user
    public void StartPersonalize(){
        getDialog().dismiss();
        FragmentManager fm = getFragmentManager();
        userInfo fragment = new userInfo();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameTarget, fragment);
        ft.commit();

    }
}

