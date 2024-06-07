package com.RobinNotBad.BiliClient.helper;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import com.RobinNotBad.BiliClient.activity.TutorialActivity;
import com.RobinNotBad.BiliClient.model.CustomText;
import com.RobinNotBad.BiliClient.model.Tutorial;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

//这个Helper是给教程使用的
//因为不通用(只适用教程)就放进Helper了

public class TutorialHelper {
    public static Tutorial loadTutorial(XmlResourceParser xml){
        try{
            xml.next();
            int eventType = xml.getEventType();
            
            boolean isInName = false;
            boolean isInDescrption = false;
            boolean isInType = false;
            boolean isInImg = false;
            boolean isInContentItem = false;
            boolean isInContentItemType = false;
            boolean isInContentItemText = false;
            boolean isInContentItemStyle = false;
            boolean isInContentItemColor = false;
            
            Tutorial turtorial = new Tutorial();
            List<CustomText> content = new ArrayList<>();
            CustomText item = null;
            
            while(eventType != XmlPullParser.END_DOCUMENT){
                //听我说，写的可能有点屎，但能用就行
                if(eventType == XmlPullParser.START_TAG){
                    switch(xml.getName()){
                        case "name":
                            isInName = true;
                            break;
                        case "description":
                            isInName = true;
                            break;
                        case "img":
                            isInImg = true;
                            break;
                        case "type":
                            if(isInContentItem) isInContentItemType = true;
                            else isInType = true;
                            break;
                        case "item":
                            isInContentItem = true;
                            item = new CustomText();
                            break;
                        case "text":
                            isInContentItemText = true;
                            break;
                        case "style":
                            isInContentItemStyle = true;
                            break;
                        case "color":
                            isInContentItemColor = true;
                            break;
                    }
                }
                
                else if(eventType == XmlPullParser.END_TAG){
                    switch(xml.getName()){
                        case "name":
                            isInName = false;
                            break;
                        case "description":
                            isInName = false;
                            break;
                        case "img":
                            isInImg = false;
                            break;
                        case "type":
                            if(isInContentItem) isInContentItemType = false;
                            else isInType = false;
                            break;
                        case "item":
                            isInContentItem = false;
                            content.add(item);
                            item = null;
                            break;
                        case "text":
                            isInContentItemText = false;
                            break;
                        case "style":
                            isInContentItemStyle = false;
                            break;
                        case "color":
                            isInContentItemColor = false;
                            break;
                    }
                }
                
                else if(eventType == XmlPullParser.TEXT){
                    if(isInName) turtorial.name = xml.getText();
                    else if(isInDescrption) turtorial.description = xml.getText();
                    else if(isInImg) turtorial.imgid = xml.getText();
                    else if(isInType) turtorial.type = Integer.valueOf(xml.getText());
                    else if(item != null){
                        if(isInContentItemType) item.type = Integer.valueOf(xml.getText());
                        else if(isInContentItemText) item.text = xml.getText();
                        else if(isInContentItemStyle) item.style = xml.getText();
                        else if(isInContentItemColor) item.color = xml.getText();
                    }
                }
                
                xml.next();
                eventType = xml.getEventType();
            }
            turtorial.content = content;
            return turtorial;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<Tutorial> loadTutorials(List<XmlResourceParser> xmls){
        List<Tutorial> tutorials = new ArrayList<>();
        for(XmlResourceParser parser : xmls) tutorials.add(loadTutorial(parser));
        return tutorials;
    }
    
    public static SpannableStringBuilder loadText(List<CustomText> texts){
        SpannableStringBuilder str = new SpannableStringBuilder("");
        for(CustomText text : texts){
            if(text.type == 0){
                str.append(text.text);
                switch(text.style){
                    case "bold": //粗体
                        str.setSpan(new StyleSpan(Typeface.BOLD),str.length() - text.text.length(),str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        break;
                    case "italic": //斜体
                        str.setSpan(new StyleSpan(Typeface.ITALIC),str.length() - text.text.length(),str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        break;
                    case "underline": //下划线
                        str.setSpan(new UnderlineSpan(),str.length() - text.text.length(),str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        break;
                    case "strike": //删除线
                        str.setSpan(new StrikethroughSpan(),str.length() - text.text.length(),str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        break;
                }
                str.setSpan(new ForegroundColorSpan(Color.parseColor(text.color)),str.length() - text.text.length(),str.length(),Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            else if(text.type == 1) str.append("\n");
        }
        return str;
    }
    
    public static void show(int xml_res_id,Context context){
        Intent intent = new Intent(context,TutorialActivity.class);
        intent.putExtra("xml_id",xml_res_id);
        context.startActivity(intent);
    }
}
