package com.RobinNotBad.BiliClient.model;

import android.content.ContentValues;

import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class DownloadSection {
    public String type;
    public long aid;
    public long cid;
    public int qn;
    public String url_dm;
    public String url_cover;
    public String name;
    public String parent;
    public String name_short;
    public String state;

    public DownloadSection(){}
    public DownloadSection(JSONObject task) throws JSONException {
        type = task.getString("type");
        aid = task.getLong("aid");
        cid = task.getLong("cid");
        qn = task.getInt("qn");
        url_cover = task.getString("url_cover");
        url_dm = task.getString("url_dm");
        name = ToolsUtil.stringToFile(task.getString("name"));
        switch (type) {
            case "video_single":  //单集视频
                name_short = name.substring(0, Math.min(6, name.length()))
                        + (name.length() > 5 ? "..." : "");
                break;
            case "video_multi":  //多集视频
                parent = task.getString("parent");

                name_short = parent.substring(0, Math.min(6, parent.length()))
                        + (parent.length() > 5 ? "..." : "")
                        + "-"
                        + name.substring(0, Math.min(6, name.length()))
                        + (name.length() > 5 ? "..." : "");
        }
        if(task.has("state")) state = task.getString("state");
        else state = "none";
    }

    //这东西不能加toJson，新的json和原来的不一样
}
