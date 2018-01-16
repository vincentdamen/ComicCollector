package com.example.vincent.comiccollector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class loginMenu extends Fragment implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.login):
                openDialog(false);
                break;
            case (R.id.register):
                openDialog(true);
                break;
        }
    }


    public void openDialog(boolean register) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (register) {
            registerProcess fragment = new registerProcess();
            fragment.show(ft, "dialog");
        } else {
            loginProcess fragment = new loginProcess();
            fragment.show(ft, "dialog");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_menu, container, false);
        Button login = (Button) view.findViewById(R.id.login);
        Button register = (Button) view.findViewById(R.id.register);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        return view;
    }
}
