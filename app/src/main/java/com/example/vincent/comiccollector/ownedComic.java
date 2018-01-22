package com.example.vincent.comiccollector;



public class ownedComic {
    public int comicId;
    public int owned;
    public String condition;
    public String thumbExt;
    public String thumbLink;
    public String title;

    public ownedComic(int comicId,int owned, String condition, String thumbExt
            , String thumbLink, String title){
        this.comicId = comicId;
        this.owned=owned;
        this.condition = condition;
        this.thumbExt = thumbExt;
        this.thumbLink = thumbLink;
        this.title=title;
    }
    public ownedComic(){}

}
