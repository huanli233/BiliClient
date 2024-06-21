package com.RobinNotBad.BiliClient.model;

public class Opus {
    public static final int TYPE_DYNAMIC = 1;
    public static final int TYPE_ARTICLE = 2;
    
    public String content;
    public String cover;
    public long opusId;
    public String timeText;
    public String title;
    
    public int type;
    public long parsedId;

    public Opus(int type,long id){
        this.type = type;
        this.parsedId = id;
    }
    public Opus(){
        
    }
}
