package com.example.vincent.comiccollector;

import android.media.Image;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Vincent on 14-1-2018.
 */

public class comic {
    public int id;
    public String title;
    public double issueNumber;
    public String description;
    public String ThumbLink;
    public String ThumbExt;
    public ArrayList<String> characters;
    public ArrayList<String> creators;
    public ArrayList<String> series;
    public ArrayList<String> events;

    public comic(int id,
            String title,
            double issueNumber,
            String description,
            String ThumbLink,
            String ThumbExt,
            ArrayList<String> characters,
            ArrayList<String> creators,
            ArrayList<String> series,
            ArrayList<String> events){
        this.id=id;
        this.title=title;
        this.issueNumber=issueNumber;
        this.description=description;
        this.ThumbLink=ThumbLink;
        this.ThumbExt=ThumbExt;
        this.characters=characters;
        this.creators=creators;
        this.series=series;
        this.events=events;
    }

}
