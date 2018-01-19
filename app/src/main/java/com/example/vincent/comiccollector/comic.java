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


    public comic(int id,
            String title,
            double issueNumber,
            String description,
            String ThumbLink,
            String ThumbExt){
        this.id=id;
        this.title=title;
        this.issueNumber=issueNumber;
        this.description=description;
        this.ThumbLink=ThumbLink;
        this.ThumbExt=ThumbExt;

    }

}
