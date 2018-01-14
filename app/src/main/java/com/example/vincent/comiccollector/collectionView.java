package com.example.vincent.comiccollector;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class collectionView extends Fragment {
    GridView collectionGrid;
    private mainActivity mainActivity;
    ArrayList<comic> test = new ArrayList<comic>();
    public collectionView() {
        // Required empty public constructor
    }

    public void contactApi(String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mainActivity.createLink(query),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        test = mainActivity.JSONify(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        });
        queue.add(stringRequest);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_collection_view, container,
                false);
        contactApi("");
        collectionGrid = view.findViewById(R.id.collectionGrid);
        gridAdapter adapter = new gridAdapter(getContext(),test,1);
        return view;
    }

}
