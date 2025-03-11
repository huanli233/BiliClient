package com.RobinNotBad.BiliClient.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.helper.sql.DownloadSqlHelper;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.Logu;
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
    public static boolean started;
    public static float percent = -1;
    public static String state;
    public static DownloadSection section;
    private static long firstDown;

    private boolean endByLimit = false;    //用于标记是否由于单线程限制而退出下载服务

    final String NOTIFY_CHANNEL_ID = "biliterminal_download";
    NotificationCompat.Builder statusBuilder, completionBuilder;
    NotificationManager notifyManager;

    private String exitMessage = null;

    private Timer toastTimer, notifyTimer;

    public DownloadService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();

        notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFY_CHANNEL_ID, "哔哩终端下载服务", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("哔哩终端下载服务");
            channel.setSound(null,null);
            channel.enableVibration(false);

            notifyManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, DownloadListActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,0);
        statusBuilder = new NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("下载视频中")
                .setProgress(100,0,false)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        completionBuilder = new NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setSound(null)
                .setVibrate(null)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MutatingSharedPrefs")
    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if(started) {
            endByLimit = true;    //用于标记，防止在onDestroy里把started设为false
            stopSelf();
            return super.onStartCommand(serviceIntent, flags, startId);
        }

        started = true;

        if(serviceIntent!=null) firstDown = serviceIntent.getLongExtra("first",-1);

        startNotifyProgress();

        CenterThreadPool.run(()->{
            boolean failed = false;
            while (!failed) {
                section = getFirst();
                if(section ==null) break;


                //获取视频链接
                try {
                    String url_video, url_danmaku;
                    try{
                        PlayerData data = section.toPlayerData();
                        PlayerApi.getVideo(data, true);
                        url_video = data.videoUrl;
                        url_danmaku = data.danmakuUrl;
                    } catch (JSONException e){
                        setState(section.id,"error");
                        notifyCompletion("下载链接获取失败：\n" + section.name_short, (int) section.id);
                        continue;
                    }


                    try {
                        setState(section.id,"downloading");
                        percent = 0;
                        refreshDownloadList();

                        File file_sign = null;
                        switch (section.type) {
                            case "video_single":  //单集视频
                                File path_single = section.getPath();

                                file_sign = new File(path_single,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                toastState("下载弹幕");
                                downDanmaku(url_danmaku, new File(path_single, "danmaku.xml"));

                                toastState("下载封面");
                                downFile(section.url_cover, new File(path_single, "cover.png"));

                                toastState("下载视频");
                                downFile(url_video, new File(path_single, "video.mp4"));

                                break;
                            case "video_multi":  //多集视频
                                File path_page = section.getPath();
                                File path_parent = path_page.getParentFile();

                                if(!path_page.exists()) path_page.mkdirs();

                                file_sign = new File(path_page,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                toastState("下载封面");
                                File cover_multi = new File(path_parent, "cover.png");
                                if (!cover_multi.exists()) downFile(section.url_cover, cover_multi);

                                toastState("下载弹幕");
                                downDanmaku(url_danmaku, new File(path_page, "danmaku.xml"));

                                toastState("下载视频");
                                downFile(url_video, new File(path_page, "video.mp4"));

                                break;
                        }

                        notifyCompletion("下载成功：\n" + section.name_short, (int) section.id);

                        if(file_sign!=null && file_sign.exists()) file_sign.delete();

                        deleteSection(section.id);

                        refreshLocalList();
                    } catch (RuntimeException e){
                        e.printStackTrace();
                        setState(section.id,"error");
                        exitMessage = "下载失败：" + e.getMessage();
                    }

                } catch (IOException e) {
                    exitMessage = "下载失败，网络或文件错误\n请手动前往下载列表页重启下载";
                    failed = true;
                    stopSelf();
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }

            refreshDownloadList();

            exitMessage = "全部下载完成";

            stopSelf();
        });

        return super.onStartCommand(serviceIntent, flags, startId);
    }

    private void fakeDownload(){
        setState(section.id,"downloading");
        percent = 0;
        refreshDownloadList();
        try {
            Thread.sleep(3000);
        }catch (Exception ignored){}


        deleteSection(section.id);
        refreshLocalList();
    }


    private void toastState(String newState){
        state = newState;
        if(toastTimer != null) toastTimer.cancel();
        toastTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(section == null) return;
                if(!started) this.cancel();

                String percentStr = String.format(Locale.CHINA,"%.2f",percent * 100);
                MsgUtil.showMsg(state + "："+ percentStr + "%\n" + section.name_short);
            }
        };
        toastTimer.schedule(timerTask,0,5000);
    }

    private void startNotifyProgress(){
        notifyTimer = new Timer();
        notifyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(section == null || notifyTimer == null) {
                    this.cancel();
                    return;
                }
                statusBuilder.setContentText(state + "：" + section.name_short);
                statusBuilder.setProgress(100, (int) (percent * 100),false);
                notifyManager.notify(1, statusBuilder.build());
            }
        }, 500,500);
    }

    private void notifyExit(String content){
        MsgUtil.showMsg(content);
        completionBuilder.setContentText(content);
        notifyManager.notify(1, completionBuilder.build());
    }


    private void notifyCompletion(String content, int id){
        MsgUtil.showMsg(content);
        completionBuilder.setContentText(content);
        notifyManager.notify(100 + id, completionBuilder.build());
    }

    private void refreshDownloadList(){
        if(DownloadListActivity.weakRef != null) {
            DownloadListActivity.weakRef.get().refreshList(true);
        }
    }

    private void refreshLocalList(){
        InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
        if (instance instanceof LocalListActivity && !instance.isDestroyed())
            ((LocalListActivity) (instance)).refresh();
    }

    private void downFile(String url, File file) throws IOException {
        Response response = NetWorkUtil.get(url);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
            inputStream = Objects.requireNonNull(response.body()).byteStream();
            fileOutputStream = new FileOutputStream(file);
            int len;
            byte[] bytes = new byte[1024 * 10];
            long TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                long CompleteFileSize = file.length();
                percent = 1.0f * CompleteFileSize / TotalFileSize;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件错误");
        } finally {
            if(inputStream!=null) inputStream.close();
            if(fileOutputStream!=null) fileOutputStream.close();
            if (response.body() != null) response.body().close();
            response.close();
        }
    }

    private void downDanmaku(String danmaku, File danmakuFile) throws IOException {
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
            if (bufferedSink != null) bufferedSink.close();
            if(response.body()!=null) response.body().close();
            response.close();
        }
    }

    @Override
    public void onDestroy() {
        if(!endByLimit) {
            started = false;
            percent = -1;
            state = null;

            if (toastTimer != null) toastTimer.cancel();
            toastTimer = null;

            if(notifyTimer != null) notifyTimer.cancel();
            notifyTimer = null;

            if (exitMessage == null) exitMessage = "下载服务已退出";
            notifyExit(exitMessage);

            if (section != null) {
                Logu.d("退出下载服务");
                FileUtil.deleteFolder(section.getPath());
                final long id = section.id;
                section = null;

                CenterThreadPool.run(() -> {
                    setState(id, "none");
                    refreshDownloadList();
                });
            }

        }
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
                decompresser.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }


    //以下为数据库操作方法

    public static DownloadSection getFirst() {
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getReadableDatabase();

            if (firstDown >= 0)
                cursor = database.rawQuery("select * from download where id=? limit 1", new String[]{String.valueOf(firstDown)});
            if (cursor == null)
                cursor = database.rawQuery("select * from download where state!=? limit 1", new String[]{"error"});

            firstDown = -1;

            if (cursor == null || cursor.getCount() == 0) return null;

            cursor.moveToFirst();
            return new DownloadSection(cursor);
        } catch (Exception e) {
            MsgUtil.err(e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (database != null) database.close();
        }
    }

    public static ArrayList<DownloadSection> getAll() {
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select * from download", null);
            if (cursor == null || cursor.getCount() == 0) return null;

            ArrayList<DownloadSection> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new DownloadSection(cursor));
            }
            return list;
        } catch (Exception e) {
            MsgUtil.err(e);
            return new ArrayList<>();
        } finally {
            if (cursor != null) cursor.close();
            if (database != null) database.close();
        }
    }

    public static ArrayList<DownloadSection> getAllExceptDownloading() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select * from download where state!=?", new String[]{"downloading"});
            if (cursor == null || cursor.getCount() == 0) return null;

            ArrayList<DownloadSection> list = new ArrayList<>();
            while (cursor.moveToNext()) {
                list.add(new DownloadSection(cursor));
            }
            cursor.close();
            database.close();
            return list;
        } catch (Exception e) {
            MsgUtil.err(e);
            return new ArrayList<>();
        } finally {
            if (cursor != null) cursor.close();
            if (database != null) database.close();
        }
    }

    public static void deleteSection(long id) {
        SQLiteDatabase database = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getWritableDatabase();
            database.execSQL("delete from download where id=?", new Object[]{id});
            database.close();
        } catch (Exception e) {
            MsgUtil.err(e);
        } finally {
            if (database != null) database.close();
        }
    }

    public static void clear() {
        SQLiteDatabase database = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getWritableDatabase();
            database.execSQL("delete from download", new Object[]{});
            database.close();
        } catch (Exception e) {
            MsgUtil.err(e);
        } finally {
            if (database != null) database.close();
        }
    }

    public static void setState(long id, String state) {
        SQLiteDatabase database = null;
        try {
            DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
            database = helper.getWritableDatabase();
            database.execSQL("update download set state=? where id=?", new Object[]{state, id});
            database.close();
        } catch (Exception e) {
            MsgUtil.err(e);
        } finally {
            if (database != null) database.close();
        }
    }

    //以下为外部调用方法
    public static void startDownload(String title, long aid, long cid, String cover, int qn){
        CenterThreadPool.run(()->{
            try {
                DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
                SQLiteDatabase database = helper.getWritableDatabase();
                database.execSQL("insert into download(type,state,aid,cid,qn,title,child,cover) values(?,?,?,?,?,?,?,?)",
                        new Object[]{"video_single","none", aid, cid, qn, title, "", cover});
                database.close();

                File path_single = FileUtil.getDownloadPath(title,null);
                path_single.mkdirs();

                File file_sign = new File(path_single,".DOWNLOADING");
                if(!file_sign.exists())file_sign.createNewFile();

                MsgUtil.showMsg("已添加下载");

                Context context = BiliTerminal.context;
                context.startService(new Intent(context, DownloadService.class));
            } catch (Exception e){
                MsgUtil.err(e);
            }
        });
    }

    public static void startDownload(String parent, String child, long aid, long cid, String cover, int qn){
        CenterThreadPool.run(()-> {
            try {
                DownloadSqlHelper helper = new DownloadSqlHelper(BiliTerminal.context);
                SQLiteDatabase database = helper.getWritableDatabase();
                database.execSQL("insert into download(type,state,aid,cid,qn,title,child,cover) values(?,?,?,?,?,?,?,?)",
                        new Object[]{"video_multi", "none", aid, cid, qn, parent, child, cover});
                database.close();


                File path_page = FileUtil.getDownloadPath(parent,child);
                path_page.mkdirs();

                File file_sign = new File(path_page,".DOWNLOADING");
                if(!file_sign.exists())file_sign.createNewFile();

                MsgUtil.showMsg("已添加下载");

                Context context = BiliTerminal.context;
                context.startService(new Intent(context, DownloadService.class));
            } catch (Exception e){
                MsgUtil.err(e);
            }
        });
    }

}