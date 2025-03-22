package com.RobinNotBad.BiliClient.model;

import android.database.Cursor;

import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.io.File;

public class DownloadSection {
    public long id;
    public String type;
    public long aid;
    public long cid;
    public int qn;
    public String url_cover;
    public String title;
    public String child;
    public String name_short;
    public String state;

    public DownloadSection(){}

    public DownloadSection(Cursor cursor) {
        id = cursor.getInt(0);
        type = cursor.getString(1);
        state = cursor.getString(2);
        aid = cursor.getLong(3);
        cid = cursor.getLong(4);
        qn = cursor.getInt(5);
        title = cursor.getString(6);
        child = cursor.getString(7);
        url_cover = cursor.getString(8);

        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(title.substring(0, Math.min(8, title.length())));
        sBuilder.append(title.length() > 7 ? "..." : "");

        if (type.equals("video_multi")) {
            sBuilder.append("-");
            sBuilder.append(child.substring(0, Math.min(8, child.length())));
            sBuilder.append(child.length() > 7 ? "..." : "");
        }
        name_short = sBuilder.toString();

    }

    public File getPath() {
        if(type.contains("video")) {
            File path = FileUtil.getVideoDownloadPath(title,child);
            if(!path.exists()) path.mkdirs();
            return path;
        }
        else return FileUtil.getPicturePath();
    }

    public PlayerData toPlayerData(){
        PlayerData data = new PlayerData();
        data.aid = aid;
        data.cid = cid;
        data.title = title;
        data.qn = qn;
        data.mid = SharedPreferencesUtil.getLong("mid",0);
        return data;
    }
}
