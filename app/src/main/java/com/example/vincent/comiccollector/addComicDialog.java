package com.example.vincent.comiccollector;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.support.v4.app.DialogFragment;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class addComicDialog extends DialogFragment {
    ArrayList<comic> newComic;
    comicInfo comicInfo;
    SeekBar slider;
    int amount;
    ArrayList<Integer> inputIds= new ArrayList<Integer>();
    public addComicDialog newInstance(int comicId) {
        addComicDialog f = new addComicDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("comicId", comicId);
        f.setArguments(args);
        return f;
    }

    public addComicDialog() {
        // Required empty public constructor
    }

    public void getComic(final String query) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String link = comicInfo.createLink(query, 0);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        newComic = comicInfo.JSONify(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
                getComic(query);
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_add_comic_dialog, container, false);
        final int comicId = getArguments().getInt("comicId");
        getComic(comicId + "");
        getDialog().setCanceledOnTouchOutside(false);
        slider = view.findViewById(R.id.amountBar);
        slider.setOnSeekBarChangeListener(new seekbarManager());

        return view;
    }
    public class seekbarManager implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            amount=i;
            comicInfo.setTextView(R.id.amount,i+ "",getView());
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //visbilityInput(amount);}
            inputIds=getInputIds();
            visbilityInput(inputIds,amount);

        }
    }

    public void visbilityInput(ArrayList<Integer> inputIds,int i) {
        for (int n =0;n<i;i++){
            setVisibility(inputIds.get(n));
        }
        for(int n=5;n>i;n--) {
            EditText input = getView().findViewById(n);
            input.setVisibility(View.GONE);
            input.setText("00");
        }

    }
    public ArrayList<Integer> getInputIds(){
        inputIds.add(R.id.condition1);
        inputIds.add(R.id.condition2);
        inputIds.add(R.id.condition3);
        inputIds.add(R.id.condition4);
        inputIds.add(R.id.condition5);
        return inputIds;
    }
    public void setVisibility(int id){
        EditText input = getView().findViewById(id);
        input.setVisibility(View.VISIBLE);
        input.setText("3.2");
    }
}
