package com.example.vincent.comiccollector;



public class ownedComic {
    public int comicId;
    public String condition;
    public String thumbExt;
    public String thumbLink;

    public ownedComic(int comicId, String condition, String thumbExt, String thumbLink){
        this.comicId = comicId;
        this.condition = condition;
        this.thumbExt = thumbExt;
        this.thumbLink = thumbLink;
    }
    public ownedComic(){}

}
