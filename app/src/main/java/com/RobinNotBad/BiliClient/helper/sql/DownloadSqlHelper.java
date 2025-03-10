package com.RobinNotBad.BiliClient.helper.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.util.MsgUtil;

public class DownloadSqlHelper extends SQLiteOpenHelper {
    public DownloadSqlHelper(@Nullable Context context) {
        super(context, "download.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table download(id INTEGER primary key autoincrement," +
                "type TEXT," +
                "state TEXT," +
                "aid BIGINT," +
                "cid BIGINT," +
                "qn INTEGER," +
                "title TEXT," +
                "child TEXT," +
                "cover TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion) try {
            db.execSQL("drop table if exists download");
            onCreate(db);
        } catch (Throwable e){
            MsgUtil.err(e);
        }
    }
}
