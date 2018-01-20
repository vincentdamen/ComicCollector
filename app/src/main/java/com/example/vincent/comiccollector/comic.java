package com.example.vincent.comiccollector;


public class comic {
    public int id;
    public String title;
    public double issueNumber;
    public String description;
    public String ThumbLink;
    public String ThumbExt;
    public int pageCount;
    public String series;
    public String year;
    public String mainCharacter;


    public comic(int id,
            String title,
            double issueNumber,
            String description,
            String ThumbLink,
            String ThumbExt,
            int pageCount,
            String series,
            String year,
            String mainCharacter){
        this.id=id;
        this.title=title;
        this.issueNumber=issueNumber;
        this.description=description;
        this.ThumbLink=ThumbLink;
        this.ThumbExt=ThumbExt;
        this.pageCount=pageCount;
        this.series=series;
        this.year=year;
        this.mainCharacter=mainCharacter;
    }

}
