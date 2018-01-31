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
    // checks if the input is correct.
    public boolean checkInfo (CharSequence target,CharSequence password) {
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
        getDialog().setCanceledOnTouchOutside(false);
        // sets variable and sets listener.
        Button register =  view.findViewById(R.id.registerButton);
        register.setOnClickListener(this);
        return view;
    }
    @Override
    public void onClick(View view) {
        if(mainActivity.checkInternet(getContext())) {

            // checks the id.
            switch (view.getId()){
                case(R.id.registerButton):

                    // sets the required variables.
                    EditText mail =  (EditText) getDialog().findViewById(R.id.emailRegister);
                    EditText password =(EditText) getDialog().findViewById(R.id.passwordRegister);
                    EditText control =(EditText) getDialog().findViewById(R.id.passwordControl);

                    // retrieve the info from the input boxes.
                    String controls = control.getText().toString();
                    String email = mail.getText().toString();
                    String passwords = password.getText().toString();

                    // checks if the boxes aren't empty.
                    if(email.length()>0 & passwords.length()>0 & control.length()>0 &
                            Objects.equals(passwords, controls)){

                        // checks if the input is okey.
                        if (checkInfo(email,passwords)) {

                            // creates the account.
                            createAccount(email,passwords);
                        }
                        else{
                            // notify the errors to the user.
                            TextView errorBlock = getDialog().findViewById(R.id.errorR);
                            errorBlock.setText(R.string.wrongInput);
                            errorBlock.setTextColor(getResources().getColor(R.color.error));
                        }
                    }else{
                        // notify the errors to the user.
                        TextView errorBlock = getDialog().findViewById(R.id.errorR);
                        errorBlock.setText(R.string.error_empty_fields);
                        errorBlock.setTextColor(getResources().getColor(R.color.error));
                    }
                    break;
                }
        }
    }

    // the user is registered and logged in.
    public void createAccount(final String email, final String Passwords){
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, Passwords)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkLogin(email,Passwords);
                            startPersonalize();
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

    // user is logged in and will be send to extra info fragment.
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

    // open extra info fragment.
    public void startPersonalize(){
        getDialog().dismiss();
        FragmentManager fm = getFragmentManager();
        userInfo fragment = new userInfo();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameTarget, fragment);
        ft.commit();

    }
}

