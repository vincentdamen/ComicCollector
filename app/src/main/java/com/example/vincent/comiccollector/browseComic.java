package com.example.vincent.comiccollector;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;



/**
 * A simple {@link Fragment} subclass.
 */
public class browseComic extends Fragment {
    // prepare some variables that are used in the whole class.
    mainActivity mainActivity;
    collectionView collectionView;
    ArrayList<ownedComic> transposed;
    ArrayList<ownedComic> collection;
    comicInfo comicInfo;
    String offset;

    public browseComic() {
    }

    // returns a random offset to generate random lists.
    public String randomOffset(){
        Random rand = new Random();
        return rand.nextInt(41007) + 1+"";
    }

    // retrieves the 50 random comics from the Marvel API.
    public void getComics(final String query, final View view){
        // hide the navbar and show the loading screen.
        tools.hideNavBar(getActivity());
        tools.setLoadingScreen(R.id.loadingScreen,R.id.content,view);

        // start the Volley-request to contact the marvel API.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String link = tools.createLink(query,1);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Strip the JSONarray.
                        ArrayList<comic> result = comicInfo.JSONify(response);

                        // prepares the comics to be displayed.
                        transposed = prepareComic(result);

                        // sets the gridAdapter.
                        gridAdapter adapter = new gridAdapter(getContext(),transposed,1);
                        GridView gridView = getView().findViewById(R.id.browseGrid);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new viewComic());
                        gridView.setOnItemLongClickListener(new checkTitle());

                        // shows the navbar and hides the loading screen.
                        tools.showNavBar(getActivity());
                        tools.removeLoadingscreen(R.id.loadingScreen,R.id.content,view);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
                // retry if there is an error.
                getComics(query,view);
            }
        });
        queue.add(stringRequest);
    }

    // retrieves the collection from FireBase.
    public void getCollection(){
        // sets the required variables.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference nDatabase = database.getReference("Users");
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser userId = mAuth.getCurrentUser();

        nDatabase.child(userId.getUid()).child("collection").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // saves the collection to the variable.
                        collection = collectionView.saveCollection(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("cancel error", databaseError.getMessage());
                    }
                });
    }

    // sets the OnLongClickListener for each item.
    private class checkTitle implements AdapterView.OnItemLongClickListener{

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            // show the title in a toast in a longPress.
            CharSequence text = transposed.get(i).title;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
            return true;
        }
    }

    // sets the OnItemClickListener for each item, which opens the comicInfo.
    private class viewComic implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // checks the internet connection.
            if (mainActivity.checkInternet(getContext())) {
                boolean owned = false;
                for (ownedComic comic : collection) {
                    if (comic.comicId == transposed.get(i).comicId) {
                        owned = true;
                        openInfo(comic.comicId, comic.condition, owned);
                    }
                }
                if (!owned) {
                    int comicId = transposed.get(i).comicId;
                    String condition = transposed.get(i).condition;
                    openInfo(comicId, condition, owned);
                }
            }
        }
    }

    // prepares the comics to be displayed like the collection view.
    public ArrayList<ownedComic> prepareComic(ArrayList<comic> input) {
        transposed = new ArrayList<ownedComic>();
        for(comic comic:input){
            ownedComic transformer = new ownedComic(comic.id,
                    "",
                    comic.thumbExt,
                    comic.thumbLink,
                    comic.title);
            transposed.add(transformer);
            }
        return transposed;
    }

    // opens the comicInfo fragment and sends the required info along.
    public void openInfo(int comicId,String condition,Boolean owned) {
        mainActivity.backAdministration(false,getContext());
        FragmentManager fm = getFragmentManager();
        comicInfo fragment = new comicInfo().newInstance(owned,comicId,condition);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.targetFrame, fragment);
        ft.addToBackStack(null).commit();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_browse_comic, container, false);

        // sets the action button.
        FloatingActionButton reload  =view.findViewById(R.id.reload);
        reload.setOnClickListener(new reloadGrid());

        // gets the collection.
        getCollection();

        // checks if there is already an offset, otherwise it will create one.
        String oldOffset= mainActivity.getOffset(getContext());
        if(!Objects.equals(oldOffset, "null")){
            offset = oldOffset;}
        else{
            offset = randomOffset();
            mainActivity.saveOffset(offset,getContext());}

        // retrieves the comics.
        getComics(offset,view);
        mainActivity.backAdministration(true,getContext());
        return view;
    }

    @Override
    public void onResume() {
        // checks internet connection.
        if (mainActivity.checkInternet(getContext())) {
            // sets the view.
            getComics(offset,getView());
            mainActivity.backAdministration(true, getContext());
            super.onResume();
        }
    }

    // reloads the grid to show new comics.
    private class reloadGrid implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(mainActivity.checkInternet(getContext())) {
                offset=randomOffset();
                getComics(offset,getView());
                mainActivity.saveOffset(offset,getContext());}
        }
    }
}
