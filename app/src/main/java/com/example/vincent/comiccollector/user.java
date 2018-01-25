package com.example.vincent.comiccollector;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vincent on 15-1-2018.
 */

public class user {
    // Dit zijn de variabele die in deze class zitten
    public String name;
    public String age;
    public ArrayList<comic> collection;

    public user(String name, String age, ArrayList<comic> collection){
        this.name = name;
        this.age = age;
        this.collection = collection;
    }
    public user(){};

    // Hiermee wordt informatie opgeslagen van nieuwe gebruikers
    @Exclude
    public Map<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("name",name);
        result.put("age",age);
        result.put("collection",collection);

        return result;
    }

}
