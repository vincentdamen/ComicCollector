package com.example.vincent.comiccollector;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class searchUsers extends Fragment {
    ArrayList<String> userName = new ArrayList<String>();
    ArrayList<String> uid = new ArrayList<String>();
    ListView list;
    EditText searchBar;
    listAdapterSearch adapter;
    public searchUsers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_users, container, false);
        searchBar = view.findViewById(R.id.searchBar);
        getRecentUsers(view);
        if(uid.isEmpty()){
        getUsers(true);
        Log.d("size",userName.size()+"");
        }
        searchBar.addTextChangedListener(new controlSearch());

        return view;
    }

    private class selectUser implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            addRecentVisited(i);
            SharedPreferences sharedPref = getActivity().getSharedPreferences("showUser", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("uid",uid.get(i));
            editor.apply();
            openCollectionView();
            searchBar.setText("");
        }
    }

    public void openCollectionView() {
        mainActivity.backAdministration(false,getContext());
        FragmentManager fm = getFragmentManager();
        collectionView fragment = new collectionView();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.addToBackStack(null).commit();
    }

    public void setList(View view){
        ListView list = view.findViewById(R.id.resultList);
        adapter = new listAdapterSearch(getContext(),userName,1);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new selectUser());
    }
    public void getRecentUsers(View view){
        SharedPreferences sharedPref = getContext().getSharedPreferences("recentVisited", Context.MODE_PRIVATE);
        int size = sharedPref.getInt("size", 0);
        userName.clear();
        uid.clear();
        for (int i = size+1; i >0 ; i--) {
            String name= sharedPref.getString("name_"+i,"deleted");
            String userId= sharedPref.getString("uid_"+i,"deleted");
            if (!Objects.equals(name, "deleted")){
                userName.add(name);
                uid.add(userId);
            }

        }

        setList(view);
    }
    public void addRecentVisited(int position) {
        SharedPreferences sharedPref1 = getContext().getSharedPreferences("recentVisited", Context.MODE_PRIVATE);
        Boolean removeOld=false;
        int toRemove=0;
        Log.d("size", userName.size()+"");
        int count = sharedPref1.getInt("size",0);
        for(int i=0;i<count+1;i++){
            if(Objects.equals(sharedPref1.getString("uid_" + i, "null"), uid.get(position))){
                toRemove=i;
                removeOld=true;
            }
        }
        SharedPreferences.Editor editor = sharedPref1.edit();
        if(removeOld){
            editor.remove("uid_"+toRemove);
            editor.remove("name_"+toRemove);
        }
        editor.putInt("size",(count+1));
        editor.putString("name_" + (count+1), userName.get(position));
        editor.putString("uid_" + (count+1), uid.get(position));

        editor.apply();

    }

    private class controlSearch implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length()==0){
                Log.d("test","true");
                getRecentUsers(getView());
            }else {
            getUsers(false);
            search(editable.toString());}

        }
    }

    public void search(final String editable){
        CountDownTimer timer = new CountDownTimer(500, 100) {
            @Override
            public void onTick(long l) {

            }
            public void onFinish() {
                updateResults(editable.toString());
                adapter.notifyDataSetChanged();
            }};
        timer.start();
    }

    public void updateResults(String s) {
        ArrayList<String> tempName = new ArrayList<String>();
        ArrayList<String> tempUid = new ArrayList<String>();
        for(int i=0;i<userName.size();i++){
            if(userName.get(i).toLowerCase().contains(s.toLowerCase())){
                tempName.add(userName.get(i));
                tempUid.add(uid.get(i));
            }
        }
        userName.clear();
        uid.clear();
        userName.addAll(tempName);
        uid.addAll(tempUid);
        }

    public void getUsers(final boolean onCreate){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        uid.clear();
        userName.clear();
        nDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            uid.add(snapshot.getKey());
                            userName.add(snapshot.child("name").getValue().toString());
                            if(onCreate){
                                setList(getView());
                                Log.d("size",userName.size()+"" );
                            }
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
