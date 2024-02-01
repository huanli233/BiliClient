package com.RobinNotBad.BiliClient.activity.player;

import static android.media.AudioManager.STREAM_MUSIC;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.BiliClient;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.view.BatteryView;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import com.RobinNotBad.BiliClient.activity.player.BiliDanmukuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import com.RobinNotBad.BiliClient.R;

public class PlayerActivity extends AppCompatActivity implements IjkMediaPlayer.OnPreparedListener {
    private IDanmakuView mDanmakuView;
    private DanmakuContext mContext;
    private Timer progresstimer, autoHideTimer, sound, speedTimer, loadingShowTimer;
    private String url,cookie, danmaku, mode, title, ShowProgress;
    private boolean onLongClick= false;
    private int videoall;
    private int videonow;
    private RelativeLayout clickMgr,topCtrl,videoArea;
    private LinearLayout rightCtrlBar,speedEdit,bottomCtrl,loadingInfo;
    private IjkMediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture mSurfaceTexture;
    private float TotalFileSize;//视频文件总文件大小
    private float CompleteFileSize;//已经下完的文件大小
    private float DownProgress;//下载进度
    private Button ControlButton;
    private SeekBar progressBar, speedSeekBar;
    private float Screenwidth, Screenheight;
    private boolean ischanging, isdanmakushowing = false;
    private TextView timenow, alltime, showsound, Showtitle, loadingText0, loadingText1, speed, newSpeed,clock;
    private AudioManager audioManager;
    private ImageView danmaku_btn, circle, loop_btn, rotate_btn;
    private int dldFinish = 0;

    private final float[] speeds = {0.5F, 0.75F,1.0F,1.25F,1.5F,1.75F,2.0F,3.0F};
    private final String[] speedTexts = {"x 0.5","x 0.75","x 1.0","x 1.25","x 1.5","x 1.75","x 2.0","x 3.0"};

    private int lastProgress;
    private boolean dmkPlaying;

    private boolean downloading = false;

    private boolean prepared = false;

    private boolean finishWatching = false;

    private BatteryView batteryView;
    private BatteryManager manager;
    private SimpleDateFormat simpleDateFormat;
    private SharedPreferences videopref;
    private SharedPreferences interfacepref;
    private String minuteSTR;
    private String secondSTR;
    private boolean loop;

    private File workpath,cachepath,videoFile,danmakuFile,dldFolder;
    
    @Override
    public void onBackPressed() {
        if(!SharedPreferencesUtil.getBoolean("back_disable",false)) super.onBackPressed();
    }
    
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("加载", "加载");
        IjkMediaPlayer.loadLibrariesOnce(null);

        videopref = getSharedPreferences("videosetting", MODE_PRIVATE);
        interfacepref = getSharedPreferences("interfacesetting", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
        else{
            while (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        findview();//找到所有控件

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }

        loop = videopref.getBoolean("videoloop",false);
        Glide.with(this).load(R.mipmap.load).into(circle);

        if(SharedPreferencesUtil.getBoolean("player_privatepath",true)) workpath = getExternalFilesDir(null);
        else workpath = new File(Environment.getExternalStorageDirectory() + "/Video");
        if(!workpath.exists()) workpath.mkdirs();
        if(SharedPreferencesUtil.getBoolean("player_privatepath",true)) cachepath = getExternalCacheDir();
        else cachepath = new File(Environment.getExternalStorageDirectory() + "/Video/playerCache");
        if(!cachepath.exists()) cachepath.mkdirs();

        videoFile = new File(cachepath + "/video.mp4");
        danmakuFile = new File(cachepath + "/danmaku.xml");

        simpleDateFormat = new SimpleDateFormat("HH:mm");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mContext = DanmakuContext.create();
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, SharedPreferencesUtil.getInt("player_danmaku_maxline", 25));
        HashMap<Integer, Boolean> overlap = new HashMap<>();
        overlap.put(BaseDanmaku.TYPE_SCROLL_LR, SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap", true));
        overlap.put(BaseDanmaku.TYPE_FIX_BOTTOM, SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap", true));
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 1)
                .setDuplicateMergingEnabled(SharedPreferencesUtil.getBoolean("player_danmaku_mergeduplicate", false))
                .setScrollSpeedFactor(SharedPreferencesUtil.getFloat("player_danmaku_speed", 1.0f))
                .setScaleTextSize(SharedPreferencesUtil.getFloat("player_danmaku_size", 1.0f))//缩放值
                .setMaximumLines(maxLinesPair)
                .setDanmakuTransparency(SharedPreferencesUtil.getFloat("player_danmaku_transparency", 0.5f))
                .preventOverlapping(overlap);

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Screenwidth = displayMetrics.widthPixels;//获取屏宽
        Screenheight = displayMetrics.heightPixels;//获取屏高

        wearbili();
        setClickMgr();
        autohide();

        if(interfacepref.getBoolean("showRotateBtn",true)) rotate_btn.setVisibility(View.VISIBLE);
        else rotate_btn.setVisibility(View.GONE);

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if(fromUser){
                    int cgminute = position / 60000;
                    int cgsecond = position % 60000 / 1000;
                    String cgminStr;
                    String cgsecStr;
                    if(cgminute < 10) cgminStr = "0" + cgminute;
                    else cgminStr = String.valueOf(cgminute);
                    if(cgsecond < 10) cgsecStr = "0" + cgsecond;
                    else cgsecStr = String.valueOf(cgsecond);

                    runOnUiThread(() -> timenow.setText(cgminStr + ":" + cgsecStr));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ischanging = true;
                if (autoHideTimer != null) autoHideTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(progressBar.getProgress());
                    ischanging = false;
                    if (mDanmakuView != null && !mode.equals("1")) {
                        mDanmakuView.start(progressBar.getProgress());
                        mDanmakuView.pause();
                    }
                }
                autohide();
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if(fromUser) {
                    newSpeed.setText(speedTexts[position]);
                    speed.setText(speedTexts[position]);
                    mediaPlayer.setSpeed(speeds[position]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(speedTimer!=null) speedTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speedTimer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> speedEdit.setVisibility(View.GONE));
                    }
                };
                speedTimer.schedule(timerTask,200);
            }
        });

        clock.setText(simpleDateFormat.format(new Date(System.currentTimeMillis())));

        if(SharedPreferencesUtil.getBoolean("player_display",false)){
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    mSurfaceTexture = surfaceTexture;
                }
                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {}
                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {return false;}
                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {}
            });
        }

        new Thread(()->{
            switch (mode){
                case "0":
                    if (SharedPreferencesUtil.getBoolean("player_online", true)) {
                        Log.e("debug", "准备在线播放");
                        runOnUiThread(() -> {
                            loadingText0.setText("载入视频中");
                            loadingText1.setText("(≧∇≦)");
                        });
                        playing(url);
                    } else {
                        Log.e("debug", "开始下载视频");
                        downvideo(true);
                    }
                    downdanmu();
                    break;
                case "1":
                    playing(url);
                    break;
                case "2":
                    streamdanmaku(danmaku);
                    playing(url);
                    break;
            }
        }).start();

    }



    private void findview() {
        clickMgr = findViewById(R.id.clickMgr);
        circle = findViewById(R.id.circle);
        timenow = findViewById(R.id.timenow);
        alltime = findViewById(R.id.alltime);
        danmaku_btn = findViewById(R.id.danmaku_btn);
        loop_btn = findViewById(R.id.loop_btn);
        rotate_btn = findViewById(R.id.rotate_btn);
        ControlButton = findViewById(R.id.control);//找到播放控制按钮
        progressBar = findViewById(R.id.videoprogress);//找到视频进度条
        speed = findViewById(R.id.video_speed);
        loadingInfo = findViewById(R.id.loading_info);
        loadingText0 = findViewById(R.id.loading_text0);
        loadingText1 = findViewById(R.id.loading_text1);
        Showtitle = findViewById(R.id.showtitle);//找到标题控件
        showsound = findViewById(R.id.showsound);
        ControlButton = findViewById(R.id.control);
        videoArea = findViewById(R.id.videoArea);
        mDanmakuView = findViewById(R.id.sv_danmaku);
        topCtrl =  findViewById(R.id.Topcontrol);
        bottomCtrl = findViewById(R.id.Bottomcontrol);
        batteryView = findViewById(R.id.power);
        rightCtrlBar = findViewById(R.id.rightCtrlBar);
        speedEdit = findViewById(R.id.speedEdit);
        speedSeekBar = findViewById(R.id.speedSeekbar);
        newSpeed = findViewById(R.id.newSpeed);
        clock = findViewById(R.id.clock);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (SharedPreferencesUtil.getBoolean("player_display", false)) {
            textureView = new TextureView(this);
            videoArea.addView(textureView,params);
        }
        else {
            surfaceView = new SurfaceView(this);
            videoArea.addView(surfaceView,params);
        }
    }

    private void wearbili() {
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");//新增：模式  0=普通播放，1=本地无弹幕视频，2=本地有弹幕视频
        url = intent.getStringExtra("url");//视频链接
        danmaku = intent.getStringExtra("danmaku");//弹幕链接
        title = intent.getStringExtra("title");//视频标题
        if(intent.hasExtra("cookie")) cookie = intent.getStringExtra("cookie");
        else cookie = "";
        Log.e("cookie",cookie);
        if (danmaku != null)Log.e("弹幕",danmaku);
        if (url != null)Log.e("视频",url);
        if (title != null)Log.e("标题",title);
        if (mode != null)Log.e("mode",mode);
        if (title != null) {
            Showtitle.setText(title);
            String tmp = title.replace("|", "｜");
            tmp = tmp.replace(":", "：");
            tmp = tmp.replace("*", "﹡");
            tmp = tmp.replace("?", "？");
            tmp = tmp.replace("\"", "”");
            tmp = tmp.replace("<", "＜");
            tmp = tmp.replace(">", "＞");
            tmp = tmp.replace("/", "／");
            tmp = tmp.replace("\\", "＼");    //文件名里不能包含非法字符
            dldFolder = new File(workpath + "/" + tmp);
            Log.e("debug-存储路径", tmp);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickMgr(){
        //搞长按倍速累死个人
        //这个管普通点击
        clickMgr.setOnClickListener(view -> {
            clickUI();
            speedEdit.setVisibility(View.GONE);
        });

        //这个管长按开始
        clickMgr.setOnLongClickListener(view -> {
            if(videopref.getBoolean("longclick",false) && mediaPlayer!=null && (ControlButton.getText() == "| |")) {
                if (!onLongClick) {
                    hidecon();
                    Vibrator vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(20L);
                    mediaPlayer.setSpeed(3.0F);
                    speed.setText("x 3.0");
                    onLongClick = true;
                }
            }
            return false;
        });

        //这个管长按结束
        clickMgr.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP && onLongClick){
                onLongClick = false;
                mediaPlayer.setSpeed(speeds[speedSeekBar.getProgress()]);
                speed.setText(speedTexts[speedSeekBar.getProgress()]);
            }
            return false;
        });
    }


    private void autohide() {
        if (autoHideTimer != null) autoHideTimer.cancel();
        autoHideTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    rightCtrlBar.setVisibility(View.GONE);
                    topCtrl.setVisibility(View.GONE);
                    bottomCtrl.setVisibility(View.GONE);
                    speed.setVisibility(View.GONE);
                });
            }
        };
        autoHideTimer.schedule(timerTask, 4000);
    }

    @SuppressLint("SetTextI18n")
    private void showcon() {
        rightCtrlBar.setVisibility(View.VISIBLE);
        topCtrl.setVisibility(View.VISIBLE);
        bottomCtrl.setVisibility(View.VISIBLE);
        if(mediaPlayer != null) speed.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }

        clock.setText(simpleDateFormat.format(new Date(System.currentTimeMillis())));

        autohide();
    }

    private void clickUI(){
        if ((topCtrl.getVisibility()) == View.GONE) showcon();
        else hidecon();
    }

    private void hidecon(){
        if (autoHideTimer != null) autoHideTimer.cancel();
        rightCtrlBar.setVisibility(View.GONE);
        topCtrl.setVisibility(View.GONE);
        bottomCtrl.setVisibility(View.GONE);
        if(mediaPlayer != null) speed.setVisibility(View.GONE);
    }

    public void danmakucontrol(View view) {
        if(mode.equals("1")){
            Toast.makeText(this, "本视频无弹幕", Toast.LENGTH_SHORT).show();
        }
        else if (mediaPlayer != null && mDanmakuView != null) {
            if (isdanmakushowing) {
                mDanmakuView.setVisibility(View.GONE);
                danmaku_btn.setImageResource(R.mipmap.danmakuoff);
                isdanmakushowing = false;
            } else {
                mDanmakuView.setVisibility(View.VISIBLE);
                danmaku_btn.setImageResource(R.mipmap.danmakuon);
                isdanmakushowing = true;
            }
        }
    }

    public void loopcontrol(View view) {
        if(loop){
            loop = false;
            loop_btn.setImageResource(R.mipmap.loopoff);
        }
        else{
            loop = true;
            loop_btn.setImageResource(R.mipmap.loopon);
        }
    }

    private void adddanmaku() {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = "弹幕君准备完毕～(*≧ω≦)";
        danmaku.padding = 5;
        danmaku.priority = 1;
        danmaku.textColor = Color.WHITE;
        mDanmakuView.addDanmaku(danmaku);
    }

    private BaseDanmakuParser createParser(String stream) {
        if (stream == null) {
            return new BaseDanmakuParser() {
                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            assert loader != null;
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("debug","结束");
        if (autoHideTimer != null) autoHideTimer.cancel();
        if (sound != null) sound.cancel();
        if (progresstimer != null) progresstimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        if (mDanmakuView != null && !mode.equals("1")) mDanmakuView.release();
        if(loadingShowTimer != null) loadingShowTimer.cancel();

        if(dldFinish == 1){
            Log.e("存储目录",dldFolder.toString());
            Log.e("视频文件",videoFile.toString());
            Log.e("弹幕文件",danmakuFile.toString());
            dldFolder.mkdirs();
            videoFile.renameTo(new File(dldFolder + "/video.mp4"));
            danmakuFile.renameTo(new File(dldFolder + "/danmaku.xml"));
        }
        else {
            videoFile.delete();
            danmakuFile.delete();
        }

        Intent data = new Intent();
        data.putExtra("time", videonow);
        data.putExtra("isfin", finishWatching);
        setResult(0, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("debug","onPause");
        if(mediaPlayer!=null)mediaPlayer.pause();
        if (mDanmakuView != null && !mode.equals("1")) mDanmakuView.pause();
        ControlButton.setText("▶");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e("debug","onResume");
    }

    private void downvideo(boolean mode) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(url)
                        .addHeader("Connection","close")
                        .addHeader("User-Agent", "Mozilla/5.0 BiliDroid/1.1.1 (bbcallen@gmail.com)")
                        .addHeader("Referer", "https://www.bilibili.com/")
                        .addHeader("Range", "bytes=0-")
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(()-> Toast.makeText(PlayerActivity.this, "视频下载失败", Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                        FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
                        int len;
                        byte[] bytes = new byte[1024 * 10];
                        TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
                        while ((len = inputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, len);
                            CompleteFileSize = (float) videoFile.length();
                            DownProgress = (CompleteFileSize / TotalFileSize) * 100;
                            ShowProgress = DownProgress + "%";
                            runOnUiThread(() -> {
                                loadingText0.setText("下载视频中");
                                loadingText1.setText(ShowProgress);
                            });
                        }
                        inputStream.close();
                        fileOutputStream.close();
                        Log.e("debug","下载完成");
                        runOnUiThread(() -> {
                            loadingInfo.setVisibility(View.GONE);
                        });

                        if(mode) playing(videoFile.toString());
                        else {
                            runOnUiThread(() -> Toast.makeText(PlayerActivity.this,"下载完成",Toast.LENGTH_SHORT).show());
                            dldFinish = 1;
                            downloading = false;
                        }
                    }
                });
            }
        }.start();
    }

    private void playing(String nowurl) {
        Log.e("debug","创建播放器");
        Log.e("debug-url",nowurl);


        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", (SharedPreferencesUtil.getBoolean("player_codec",true) ? 1 : 0));

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", (SharedPreferencesUtil.getBoolean("player_audio",false) ? 1 : 0));

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        //mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        //mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 512*10);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch",1);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);



        //这个坑死我！请允许我为解决此问题而大大地兴奋一下ohhhhhhhhhhhhhhhhhhhhhhhhhhhh
        //ijkplayer是自带一个useragent的，要把默认的改掉才能用！
        if(mode.equals("0")) {
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", "Mozilla/5.0 BiliDroid/1.1.1 (bbcallen@gmail.com)");
            Log.e("debug","设置ua");
        }


        Log.e("debug","准备设置显示");
        if (SharedPreferencesUtil.getBoolean("player_display", false)){            //Texture
            Log.e("debug","使用texture模式");
            Timer textureTimer = new Timer();
            textureTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e("debug","循环检测");
                    if(mSurfaceTexture != null){
                        Surface surface = new Surface(mSurfaceTexture);
                        mediaPlayer.setSurface(surface);
                        MPPrepare(nowurl);
                        this.cancel();
                    }
                }
            },0,200);

        }
        else{
            Log.e("debug","使用surface模式");
            Log.e("debug","获取surfaceHolder");
            SurfaceHolder surfaceHolder = surfaceView.getHolder();       //Surface
            Log.e("debug","添加callback");
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    if(prepared) {
                        Log.e("debug", "重新设置Holder");
                        mediaPlayer.setDisplay(surfaceHolder);
                        mediaPlayer.seekTo(progressBar.getProgress());
                    }
                    else {
                        mediaPlayer.setDisplay(surfaceHolder);
                        MPPrepare(nowurl);
                    }
                }
                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    Log.e("debug", "Holder没了");
                    mediaPlayer.setDisplay(null);
                }
            });
        }
    }

    private void MPPrepare(String nowurl){
        mediaPlayer.setOnPreparedListener(this);

        try {
            mediaPlayer.setAudioStreamType(STREAM_MUSIC);
            if(mode.equals("0")) {
                Map<String,String> headers = new HashMap<>();
                headers.put("Connection","Keep-Alive");
                headers.put("Referer","https://www.bilibili.com/");
                headers.put("Origin","https://www.bilibili.com/");
                if(!cookie.equals("")) headers.put("Cookie",cookie);
                mediaPlayer.setDataSource(nowurl, headers);
            }
            else mediaPlayer.setDataSource(nowurl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(iMediaPlayer -> {
            finishWatching = true;
            if(loop){
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                if(!mode.equals("1") && mDanmakuView != null) {
                    mDanmakuView.seekTo(0L);
                    mDanmakuView.resume();          //别问为啥要seekto+resume而不是start(0)，问就是我也不知道！！！反正bug修好了！！！  ----RobinNotBad
                }
                Log.e("debug","循环播放");
            }
            else {
                if(!mode.equals("1") && mDanmakuView != null) mDanmakuView.pause();
                ControlButton.setText("▶");
            }
        });

        mediaPlayer.setOnErrorListener((iMediaPlayer, what, extra) -> {
            String EReport = "播放器可能遇到错误！\n错误码：" + what + "\n附加：" + extra;
            Log.e("ERROR",EReport);
            //Toast.makeText(PlayerActivity.this, EReport, Toast.LENGTH_LONG).show();
            return false;
        });

        mediaPlayer.setOnSeekCompleteListener(iMediaPlayer -> {
            if(ControlButton.getText() == "| |") {
                if (!mode.equals("1") && mDanmakuView != null) {
                    mDanmakuView.resume();
                }
            }
        });

        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> progressBar.setSecondaryProgress(percent * videoall / 100));

        if(mode.equals("0")) mediaPlayer.setOnInfoListener((mp, what, extra) -> {
            if(what == IMediaPlayer.MEDIA_INFO_BUFFERING_START){
                runOnUiThread(() -> {
                    loadingInfo.setVisibility(View.VISIBLE);
                    loadingText0.setText("正在缓冲");
                    showLoadingSpeed();
                    if (!mode.equals("1") && dmkPlaying) {
                        mDanmakuView.pause();
                        dmkPlaying = false;
                    }
                });
            } else if(what == IMediaPlayer.MEDIA_INFO_BUFFERING_END){
                runOnUiThread(() -> {
                    if(loadingShowTimer!=null)loadingShowTimer.cancel();
                    loadingInfo.setVisibility(View.GONE);
                });
            }

            return false;
        });

        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.prepareAsync();
        Log.e("debug","开始准备");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPrepared(IMediaPlayer mediaPlayer) {
        prepared = true;
        videoall = (int) mediaPlayer.getDuration();

        changeVideoSize(mediaPlayer.getVideoWidth(),mediaPlayer.getVideoHeight());

        if (!mode.equals("1")) {
            danmaku_btn.setImageResource(R.mipmap.danmakuon);
            isdanmakushowing = true;
            mDanmakuView.start();
        } else {
            danmaku_btn.setImageResource(R.mipmap.danmakuoff);
            isdanmakushowing = false;
        }
        if (interfacepref.getBoolean("showDanmakuBtn", true))
            danmaku_btn.setVisibility(View.VISIBLE);
        else danmaku_btn.setVisibility(View.GONE);
        //原作者居然把旋转按钮命名为danmaku_btn，也是没谁了...我改过来了  ----RobinNotBad
        //他大抵是觉得能用就行


        if (loop) loop_btn.setImageResource(R.mipmap.loopon);
        else loop_btn.setImageResource(R.mipmap.loopoff);
        if (interfacepref.getBoolean("showLoopBtn", true))
            loop_btn.setVisibility(View.VISIBLE);
        else loop_btn.setVisibility(View.GONE);


        progressBar.setMax(videoall);
        int minutes = videoall / 60000;
        int seconds = videoall % 60000 / 1000;
        String totalMinSTR;
        if (minutes < 10) totalMinSTR = "0" + minutes;
        else totalMinSTR = String.valueOf(minutes);
        String totalSecSTR;
        if (seconds < 10) totalSecSTR = "0" + seconds;
        else totalSecSTR = String.valueOf(seconds);

        loadingInfo.setVisibility(View.GONE);

        ControlButton.setText("| |");
        alltime.setText(totalMinSTR + ":" + totalSecSTR);

        if (rightCtrlBar.getVisibility() == View.VISIBLE) {
            speed.setVisibility(View.VISIBLE);
        }


        progresschange();

        mediaPlayer.start();

    }

    private void showLoadingSpeed(){
        loadingShowTimer = new Timer();
        loadingShowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                float tcpSpeed = mediaPlayer.getTcpSpeed() / 1024f;
                String text = String.format(Locale.CHINA, "%.1f", tcpSpeed) + "KB/s";
                runOnUiThread(()->loadingText1.setText(text));
            }
        },0,500);
    }

    private void changeVideoSize(int width, int height) {
        Log.e("debug-改变视频区域大小","开始");
        Log.e("wid", width + "");
        Log.e("hei", height + "");
        if (interfacepref.getBoolean("round", false)) {
            float video_mul = (float) height / (float) width;
            double sqrt = Math.sqrt(((double) Screenwidth * (double) Screenwidth) / ((((double) height * (double) height) / ((double) width * (double) width)) + 1));
            int show_height = (int) (sqrt * video_mul + 0.5);
            runOnUiThread(()-> {
                videoArea.setLayoutParams(new RelativeLayout.LayoutParams((int) (sqrt + 0.5), show_height));
                Log.e("debug-改变视频区域大小","完成");
            });
        }
        else {
            float multiplewidth = Screenwidth / width;
            float multipleheight = Screenheight / height;
            float endhi1 = height * multipleheight;
            float endwi1 = width * multipleheight;
            float endhi2 = height * multiplewidth;
            float endwi2 = width * multipleheight;
            int showheight;
            int showwidth;
            if (endhi1 <= Screenheight && endwi1 <= Screenwidth) {
                showheight = (int) (endhi1 + 0.5);
                showwidth = (int) (endwi1 + 0.5);
            } else {
                showheight = (int) (endhi2 + 0.5);
                showwidth = (int) (endwi2 + 0.5);
            }
            runOnUiThread(()-> {
                videoArea.setLayoutParams(new RelativeLayout.LayoutParams(showwidth, showheight));
                Log.e("debug-改变视频区域大小","完成");
            });
        }
    }


    private void progresschange() {
        progresstimer = new Timer();
        TimerTask task = new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (mediaPlayer != null && !ischanging) {
                    videonow = (int) mediaPlayer.getCurrentPosition();
                    if (lastProgress != videonow) {               //检测进度是否在变动
                        lastProgress = videonow;
                        progressBar.setProgress(videonow);
                        if (!mode.equals("1") && !dmkPlaying) {
                            mDanmakuView.resume();
                            dmkPlaying = true;
                        }
                        int minute = videonow / 60000;
                        int second = videonow % 60000 / 1000;
                        if (minute < 10) minuteSTR = "0" + minute;
                        else minuteSTR = String.valueOf(minute);
                        if (second < 10) secondSTR = "0" + second;
                        else secondSTR = String.valueOf(second);
                        runOnUiThread(() -> {
                            timenow.setText(minuteSTR + ":" + secondSTR);
                        });
                    }
                }
            }
        };
        progresstimer.schedule(task, 0, 200);
    }

    private void downdanmu() {
        OkHttpClient okHttpClient = new OkHttpClient();
        runOnUiThread(() -> {
            loadingText0.setText("装填弹幕中");
            loadingText1.setText("(≧∇≦)");
        });
        Request request = new Request.Builder().url(danmaku)
                .addHeader("Connection","close")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(PlayerActivity.this, "弹幕文件获取失败！", Toast.LENGTH_SHORT).show());
                //请求失败
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                BufferedSink bufferedSink = null;
                try {
                    if (!danmakuFile.exists()) danmakuFile.createNewFile();
                    Sink sink = Okio.sink(danmakuFile);
                    byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
//                    Log.d("length", String.valueOf(decompressBytes.length));
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                    bufferedSink.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }
                }
                streamdanmaku(danmakuFile.toString());
            }
        });
    }

    private void streamdanmaku(String danmakuPath) {
        Log.e("debug","streamdanmaku");
        Log.e("debug",danmakuPath);
        if (mDanmakuView != null) {
            BaseDanmakuParser mParser = createParser(danmakuPath);
            mDanmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    adddanmaku();
//                    Log.e("准备","弹幕准备完毕");
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {
                    if(mediaPlayer!=null) {
                        timer.update(mediaPlayer.getCurrentPosition());
                    }
                }
                @Override
                public void danmakuShown(BaseDanmaku danmaku) {}
                @Override
                public void drawingFinished() {}
            });
            mDanmakuView.prepare(mParser, mContext);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
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

    public void controlvideo(View view) {
        if(!downloading) {
            if (ControlButton.getText().toString().equals("| |")) {
                ControlButton.setText("▶");
                if (mediaPlayer != null) mediaPlayer.pause();
                if (autoHideTimer != null) autoHideTimer.cancel();
                if (mDanmakuView != null && !mode.equals("1")) mDanmakuView.pause();
            } else {
                if (mediaPlayer != null) {
                    if (mDanmakuView != null && !mode.equals("1")) {
                        if (videonow >= videoall - 250) {     //别问为啥有个>=，问就是这TM都能有误差，视频停止时并不是播放到最后一帧,可以多或者少出来几十甚至上百个毫秒...  ----RobinNotBad
                            mediaPlayer.seekTo(0);
                            mDanmakuView.seekTo(0L);
                            mDanmakuView.resume();
                            Log.e("debug", "播完重播");
                        } else mDanmakuView.start(mediaPlayer.getCurrentPosition());
                    }
                    mediaPlayer.start();
                    ControlButton.setText("| |");
                }
            }
        }
        else Toast.makeText(PlayerActivity.this,"稍安勿躁，正在下载中~",Toast.LENGTH_SHORT).show();
        autohide();
    }

    @SuppressLint("SetTextI18n")
    public void addsound(View view) {
        autoHideTimer.cancel();
        int soundnow = audioManager.getStreamVolume(STREAM_MUSIC);
        int maxsound = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        int added = maxsound;
        if (soundnow != maxsound) {
            added = soundnow + 1;
        }
        audioManager.setStreamVolume(STREAM_MUSIC, added, 0);
        float show = (float) (added) / (float) maxsound * 100;
        runOnUiThread(() -> {
            showsound.setVisibility(View.VISIBLE);
            showsound.setText("音量：" + (int) show + "%");
        });
        hidesound();
        autohide();
    }

    @SuppressLint("SetTextI18n")
    public void cutsound(View view) {
        autoHideTimer.cancel();
        int soundnow = audioManager.getStreamVolume(STREAM_MUSIC);
        int maxsound = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        int added = 0;
        if (soundnow != 0) {
            added = soundnow - 1;
        }
        audioManager.setStreamVolume(STREAM_MUSIC, added, 0);
        float show = (float) added / (float) maxsound * 100;
        runOnUiThread(() -> {
            showsound.setVisibility(View.VISIBLE);
            showsound.setText("音量：" + (int) show + "%");
        });
        hidesound();
        autohide();
    }

    private void hidesound() {
        if (sound != null) sound.cancel();
        sound = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> showsound.setVisibility(View.GONE));
            }
        };
        sound.schedule(timerTask, 3000);
    }

    public void rotate(View view){
        //if (mediaPlayer != null && videoall != 0) mediaPlayer.pause();
        //if (mDanmakuView != null && !mode.equals("1")) mDanmakuView.pause();
        //ControlButton.setText("▶");
        Log.e("debug","点击旋转按钮");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("debug","开始旋转屏幕");

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Screenwidth = displayMetrics.widthPixels;//获取屏宽
        Screenheight = displayMetrics.heightPixels;//获取屏高
        if (mediaPlayer != null) {
            changeVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        }

        Log.e("debug","旋转屏幕结束");
    }

    public void finish(View view) {
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("debug","onNewIntent");
        finish();
    }

    public void videospeed(View view) {
        speedEdit.setVisibility(View.VISIBLE);
    }
}