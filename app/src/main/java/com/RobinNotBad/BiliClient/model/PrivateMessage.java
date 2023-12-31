package com.RobinNotBad.BiliClient.model;
import com.RobinNotBad.BiliClient.model.PrivateMessage;

public class PrivateMessage {
    
    public String content = "";
    public int type = 0;
    public long time = 0;
    public long uid = 0;
    public String name ="";
    
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_VIDEO = 7;
    public static final int TYPE_PIC = 2;
    public static final int TYPE_TIP = 5;
    
    public PrivateMessage(long uid,int type,String content,long time,String name){
        this.uid = uid;
        this.type = type;
        this.content = content;
        this.time = time;
        this.name = name;
    }
}
