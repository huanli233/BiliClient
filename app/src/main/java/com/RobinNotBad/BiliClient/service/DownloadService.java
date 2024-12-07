package com.RobinNotBad.BiliClient.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.sql.DownloadSqlHelper;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadService extends Service {
    public static float percent;
    public static boolean started;
    public static DownloadSection downloadingSection;
    public static short count_finish;
    private static int firstDown;

    private Timer toastTimer;
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(downloadingSection == null)this.cancel();
            MsgUtil.showMsg("下载进度："+ String.format(Locale.CHINA,"%.2f",percent * 100) + "%\n" + downloadingSection.name_short);
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MutatingSharedPrefs")
    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if(started) {
            stopSelf();
            return super.onStartCommand(serviceIntent, flags, startId);
        }

        started = true;
        count_finish = 0;
        MsgUtil.showMsg("下载服务已启动");
        toastTimer = new Timer();
        toastTimer.schedule(timerTask,5000,5000);

        firstDown = serviceIntent.getIntExtra("first",-1);

        CenterThreadPool.run(()->{
            while (true) {
                downloadingSection = getFirst();
                if(downloadingSection==null) break;

                try {
                    String url;
                    try{
                        url = PlayerApi.getVideo(downloadingSection.aid, downloadingSection.cid, downloadingSection.qn, true).first;
                    } catch (JSONException e){
                        MsgUtil.showMsg("下载链接获取失败");
                        setState(downloadingSection.id,"error");
                        continue;
                    }


                    try {
                        setState(downloadingSection.id,"downloading");
                        refreshDownloadList();

                        MsgUtil.showMsg("开始下载：\n" + downloadingSection.name_short);

                        File file_sign = null;
                        switch (downloadingSection.type) {
                            case "video_single":  //单集视频
                                File path_single = new File(FileUtil.getDownloadPath(this), downloadingSection.name);
                                if (!path_single.exists()) path_single.mkdirs();

                                file_sign = new File(path_single,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                String url_dm_single = downloadingSection.url_dm;
                                downdanmu(url_dm_single, new File(path_single, "danmaku.xml"));

                                String url_cover_single = downloadingSection.url_cover;
                                download(url_cover_single, new File(path_single, "cover.png"));

                                download(url, new File(path_single, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + downloadingSection.name_short);
                                break;
                            case "video_multi":  //多集视频
                                String parent = downloadingSection.parent;
                                File path_parent = new File(FileUtil.getDownloadPath(this), parent);
                                if(!path_parent.exists()) path_parent.mkdirs();

                                File path_page = new File(path_parent, downloadingSection.name);
                                if(!path_page.exists()) path_page.mkdirs();

                                file_sign = new File(path_page,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                String url_cover_multi = downloadingSection.url_cover;
                                File cover_multi = new File(path_parent, "cover.png");
                                if (!cover_multi.exists()) download(url_cover_multi, cover_multi);

                                String url_dm_page = downloadingSection.url_dm;
                                downdanmu(url_dm_page, new File(path_page, "danmaku.xml"));

                                download(url, new File(path_page, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + downloadingSection.name_short);
                                count_finish++;
                                break;
                        }

                        if(file_sign!=null && file_sign.exists()) file_sign.delete();

                        deleteSection(downloadingSection.id);

                        downloadingSection = null;
                        refreshLocalList();
                        count_finish++;
                    } catch (RuntimeException e){
                        MsgUtil.showMsg("下载失败："+e.getMessage());
                        e.printStackTrace();
                        setState(downloadingSection.id,"error");
                    }

                } catch (IOException e) {
                    MsgUtil.showMsg("下载失败，网络错误\n请手动前往下载列表页重启下载");
                    stopSelf();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            refreshDownloadList();
            if(count_finish != 0) MsgUtil.showMsg("全部下载完成");
            stopSelf();
        });

        return super.onStartCommand(serviceIntent, flags, startId);
    }

    private void refreshDownloadList(){
        if(DownloadListActivity.weakRef != null) {
            DownloadListActivity.weakRef.get().refreshList();
        }
    }

    private void refreshLocalList(){
        InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
        if (instance instanceof LocalListActivity && !instance.isDestroyed())
            ((LocalListActivity) (instance)).refresh();
    }

    private void download(String url, File file) throws IOException {
        Response response = NetWorkUtil.get(url);
        try {
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
            InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            byte[] bytes = new byte[1024 * 10];
            long TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                long CompleteFileSize = file.length();
                percent = 1.0f * CompleteFileSize / TotalFileSize;
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件错误");
        }
    }

    private void downdanmu(String danmaku, File danmakuFile) throws IOException{
            Response response = NetWorkUtil.get(danmaku);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                else {
                    danmakuFile.delete();
                    danmakuFile.createNewFile();
                }
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (IOException e) {
                throw new RuntimeException("文件错误");
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
    }



    @Override
    public void onDestroy() {
        if(downloadingSection!=null) {
            CenterThreadPool.run(()->{
                setState(downloadingSection.id, "none");
                downloadingSection = null;
            });
        }
        started = false;
        if(toastTimer!=null) toastTimer.cancel();
        percent = 0;
        count_finish = 0;
        super.onDestroy();
    }

    public static byte[] decompress(byte[] data) {
        byte[] output;
        Inflater decompresser = new Inflater(true);//这个true是关键
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }


    //以下为数据库操作方法

    public static DownloadSection getFirst(){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getReadableDatabase();

            Cursor cursor = null;

            if(firstDown>=0) cursor = database.rawQuery("select * from download where id==? limit 1",new String[]{String.valueOf(firstDown)});
            if(cursor==null) cursor = database.rawQuery("select * from download where state!=? limit 1",new String[]{"error"});

            firstDown = -1;

            if(cursor == null || cursor.getCount() == 0)return null;

            cursor.moveToFirst();
            DownloadSection downloadSection = new DownloadSection(cursor);
            cursor.close();
            database.close();
            return downloadSection;
        } catch (Exception e){
            MsgUtil.err(e);
            return null;
        }
    }

    public static ArrayList<DownloadSection> getAll(){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from download",null);
            if(cursor == null || cursor.getCount() == 0)return null;

            ArrayList<DownloadSection> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new DownloadSection(cursor));
            }
            cursor.close();
            database.close();
            return list;
        } catch (Exception e){
            MsgUtil.err(e);
            return new ArrayList<>();
        }
    }

    public static ArrayList<DownloadSection> getAllExceptDownloading(){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from download where state!=?",new String[]{"downloading"});
            if(cursor == null || cursor.getCount() == 0)return null;

            ArrayList<DownloadSection> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new DownloadSection(cursor));
            }
            cursor.close();
            database.close();
            return list;
        } catch (Exception e){
            MsgUtil.err(e);
            return new ArrayList<>();
        }
    }

    public static void deleteSection(long id){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.execSQL("delete from download where id=?", new Object[]{id});
            database.close();
        } catch (Exception e){
            MsgUtil.err(e);
        }
    }

    public static void clear(){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.execSQL("delete from download", new Object[]{});
            database.close();
        } catch (Exception e){
            MsgUtil.err(e);
        }
    }

    public static void setState(long id, String state){
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.execSQL("update download set state=? where id=?", new Object[]{state,id});
            database.close();
        } catch (Exception e){
            MsgUtil.err(e);
        }
    }

    //以下为外部调用方法
    public static void startDownload(String title, long aid, long cid, String danmaku, String cover, int qn){
        CenterThreadPool.run(()->{
            try {
                DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
                SQLiteDatabase database = helper.getWritableDatabase();
                database.execSQL("insert into download(type,state,aid,cid,qn,name,parent,cover,danmaku) values(?,?,?,?,?,?,?,?,?)",
                        new Object[]{"video_single","none", aid, cid, qn, title, "", cover, danmaku});
                database.close();

                File path_single = new File(FileUtil.getDownloadPath(BiliTerminal.context), title);
                if (!path_single.exists()) path_single.mkdirs();

                File file_sign = new File(path_single,".DOWNLOADING");
                if(!file_sign.exists())file_sign.createNewFile();

                MsgUtil.showMsg("已添加下载");
            } catch (Exception e){
                MsgUtil.err(e);
            }
        });
    }

    public static void startDownload(String parent, String child, long aid, long cid, String danmaku, String cover, int qn){
        CenterThreadPool.run(()-> {
            try {
                DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
                SQLiteDatabase database = helper.getWritableDatabase();
                database.execSQL("insert into download(type,state,aid,cid,qn,name,parent,cover,danmaku) values(?,?,?,?,?,?,?,?,?)",
                        new Object[]{"video_multi", "none", aid, cid, qn, child, parent, cover, danmaku});
                database.close();

                File path_parent = new File(FileUtil.getDownloadPath(BiliTerminal.context), parent);
                if(!path_parent.exists()) path_parent.mkdirs();

                File path_page = new File(path_parent, child);
                if(!path_page.exists()) path_page.mkdirs();

                File file_sign = new File(path_page,".DOWNLOADING");
                if(!file_sign.exists())file_sign.createNewFile();

                MsgUtil.showMsg("已添加下载");
            } catch (Exception e){
                MsgUtil.err(e);
            }
        });
    }

}