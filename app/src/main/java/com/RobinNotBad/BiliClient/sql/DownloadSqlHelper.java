package com.RobinNotBad.BiliClient.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DownloadSqlHelper extends SQLiteOpenHelper {
    public DownloadSqlHelper(@Nullable Context context) {
        super(context, "download.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table download(id INTEGER primary key autoincrement," +
                "type VARCHAR(10)," +
                "state VARCHAR(10)," +
                "aid BIGINT," +
                "cid BIGINT," +
                "qn INTEGER," +
                "name TEXT," +
                "parent TEXT," +
                "cover TEXT," +
                "danmaku TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
