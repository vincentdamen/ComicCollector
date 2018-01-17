package com.example.vincent.comiccollector;


import android.content.Context;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class collectionView extends Fragment {
    GridView collectionGrid;
    ArrayList<ownedComic> test = new ArrayList<ownedComic>();
    public collectionView() {
        // Required empty public constructor
    }
    public void getCollection(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();
        nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<ownedComic> collection = saveCollection(dataSnapshot);
                        gridAdapter adapter = new gridAdapter(getContext(),collection,1);
                        GridView gridView = getView().findViewById(R.id.collectionGrid);
                        gridView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public ArrayList<ownedComic> saveCollection(DataSnapshot dataSnapshot) {
        ArrayList<ownedComic> collectionList = new ArrayList<ownedComic>();
        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
            ownedComic comicBook = noteDataSnapshot.getValue(ownedComic.class);
            collectionList.add(comicBook);
        }
        return collectionList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_collection_view, container,
                false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCollection();

    }
}
