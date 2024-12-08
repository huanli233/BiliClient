package com.RobinNotBad.BiliClient.model;

import android.database.Cursor;

import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DownloadSection {
    public long id;
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
                name_short = name.substring(0, Math.min(8, name.length()))
                        + (name.length() > 7 ? "..." : "");
                break;
            case "video_multi":  //多集视频
                parent = task.getString("parent");

                name_short = parent.substring(0, Math.min(8, parent.length()))
                        + (parent.length() > 7 ? "..." : "")
                        + "-"
                        + name.substring(0, Math.min(8, name.length()))
                        + (name.length() > 7 ? "..." : "");
        }
        if(task.has("state")) state = task.getString("state");
        else state = "none";
    }

    //这东西不能加toJson，新的json和原来的不一样

    public DownloadSection(Cursor cursor){
        id = cursor.getInt(0);
        type = cursor.getString(1);
        state = cursor.getString(2);
        aid = cursor.getLong(3);
        cid = cursor.getLong(4);
        qn = cursor.getInt(5);
        name = cursor.getString(6);
        parent = cursor.getString(7);
        url_cover = cursor.getString(8);
        url_dm = cursor.getString(9);
        switch (type) {
            case "video_single":  //单集视频
                name_short = name.substring(0, Math.min(8, name.length()))
                        + (name.length() > 7 ? "..." : "");
                break;
            case "video_multi":  //多集视频
                name_short = parent.substring(0, Math.min(8, parent.length()))
                        + (parent.length() > 7 ? "..." : "")
                        + "-"
                        + name.substring(0, Math.min(8, name.length()))
                        + (name.length() > 7 ? "..." : "");
        }
    }

    public File getPath() {
        switch (type) {
            case "video_single":  //单集视频
                File path_single = new File(FileUtil.getDownloadPath(), name);
                if (!path_single.exists()) path_single.mkdirs();
                return path_single;

            case "video_multi":  //多集视频
                File path_multi = new File(FileUtil.getDownloadPath(), parent);
                if (!path_multi.exists()) path_multi.mkdirs();

                File path_page = new File(path_multi, name);
                if (!path_page.exists()) path_page.mkdirs();
                return path_page;

            default:
                return null;
        }
    }
}
