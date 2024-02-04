package com.RobinNotBad.BiliClient.activity.player;

import static android.media.AudioManager.STREAM_MUSIC;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.view.BatteryView;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import master.flame.danmaku.danmaku.parser.IDataSource;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerActivity extends AppCompatActivity implements IjkMediaPlayer.OnPreparedListener {
    private IDanmakuView mDanmakuView;
    private DanmakuContext mContext;
    private Timer progresstimer, autoHideTimer, sound, speedTimer, loadingShowTimer;
    private String url;
    private String danmaku;
    private int mode;
    private boolean onLongClick= false;
    private int videoall;
    private int videonow;
    private ConstraintLayout control_layout,top_control,bottom_control,speed_layout;
    private RelativeLayout videoArea;
    private LinearLayout right_control, loading_info;
    private IjkMediaPlayer ijkPlayer;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture mSurfaceTexture;
    private Button control_btn;
    private SeekBar progressBar, speed_seekbar;
    private float Screenwidth, Screenheight;
    private boolean ischanging, isdanmakushowing = false;
    private TextView text_now, text_all, showsound, text_title, loading_text0, loading_text1, text_speed, text_newspeed;
    private AudioManager audioManager;
    private ImageView danmaku_btn, circle, loop_btn, rotate_btn;

    private final float[] speeds = {0.5F, 0.75F,1.0F,1.25F,1.5F,1.75F,2.0F,3.0F};
    private final String[] speedTexts = {"x 0.5","x 0.75","x 1.0","x 1.25","x 1.5","x 1.75","x 2.0","x 3.0"};

    private int lastProgress;
    private boolean dmkPlaying;

    private boolean prepared = false;

    private boolean needSurfaceHolder = true;

    private boolean finishWatching = false;

    private BatteryView batteryView;
    private BatteryManager manager;
    private String minuteSTR;
    private String secondSTR;
    private boolean loop;

    private File danmakuFile;
    
    @Override
    public void onBackPressed() {
        if(!SharedPreferencesUtil.getBoolean("back_disable",false)) super.onBackPressed();
    }
    
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("加载", "加载");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_new);
        findview();
        getExtras();

        if(mode==-1) {
            loading_text0.setText("预览中");
            loading_text1.setText("点击上方标题栏退出");
            return;
        }

        IjkMediaPlayer.loadLibrariesOnce(null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        } else batteryView.setVisibility(View.GONE);

        loop = SharedPreferencesUtil.getBoolean("player_loop",false);
        Glide.with(this).load(R.mipmap.load).into(circle);

        File cachepath = getExternalCacheDir();
        if(!cachepath.exists()) cachepath.mkdirs();
        danmakuFile = new File(cachepath,"danmaku.xml");

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

        setClickMgr();
        autohide();

        if(SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn",true)) rotate_btn.setVisibility(View.VISIBLE);
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

                    runOnUiThread(() -> text_now.setText(cgminStr + ":" + cgsecStr));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ischanging = true;
                if (autoHideTimer != null) autoHideTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (ijkPlayer != null) {
                    ijkPlayer.seekTo(progressBar.getProgress());
                    ischanging = false;
                    if (mDanmakuView != null && mode!=1) {
                        mDanmakuView.start(progressBar.getProgress());
                        mDanmakuView.pause();
                    }
                }
                autohide();
            }
        });

        speed_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if(fromUser) {
                    text_newspeed.setText(speedTexts[position]);
                    text_speed.setText(speedTexts[position]);
                    ijkPlayer.setSpeed(speeds[position]);
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
                        runOnUiThread(() -> speed_layout.setVisibility(View.GONE));
                    }
                };
                speedTimer.schedule(timerTask,200);
            }
        });

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
            switch (mode) {
                case 0:
                    Log.e("debug", "准备在线播放");
                    runOnUiThread(() -> {
                        loading_text0.setText("装填弹幕中");
                        loading_text1.setText("(≧∇≦)");
                    });
                    downdanmu();
                    runOnUiThread(() -> {
                        loading_text0.setText("载入视频中");
                        loading_text1.setText("(≧∇≦)");
                    });
                    setDisplay(url);
                    break;
                case 1:
                    setDisplay(url);
                    break;
                case 2:
                    streamdanmaku(danmaku);
                    setDisplay(url);
                    break;
            }
        }).start();

    }



    private void findview() {
        control_layout = findViewById(R.id.control_layout);
        top_control = findViewById(R.id.top_control);
        bottom_control = findViewById(R.id.bottom_control);
        right_control = findViewById(R.id.right_control);

        loading_info = findViewById(R.id.loading_info);

        circle = findViewById(R.id.circle);
        text_now = findViewById(R.id.text_now);
        text_all = findViewById(R.id.text_all);
        danmaku_btn = findViewById(R.id.danmaku_btn);
        loop_btn = findViewById(R.id.loop_btn);
        rotate_btn = findViewById(R.id.rotate_btn);
        control_btn = findViewById(R.id.button_video);
        progressBar = findViewById(R.id.videoprogress);
        loading_text0 = findViewById(R.id.loading_text0);
        loading_text1 = findViewById(R.id.loading_text1);
        text_title = findViewById(R.id.text_title);
        showsound = findViewById(R.id.showsound);
        videoArea = findViewById(R.id.videoArea);
        mDanmakuView = findViewById(R.id.sv_danmaku);
        batteryView = findViewById(R.id.battery);

        text_speed = findViewById(R.id.text_speed);
        speed_layout = findViewById(R.id.layout_speed);
        speed_seekbar = findViewById(R.id.seekbar_speed);
        text_newspeed = findViewById(R.id.text_newspeed);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (SharedPreferencesUtil.getBoolean("player_display", false)) {
            textureView = new TextureView(this);
            videoArea.addView(textureView,params);
        }
        else {
            surfaceView = new SurfaceView(this);
            videoArea.addView(surfaceView,params);
        }

        top_control.setOnClickListener(view -> finish());
        rotate_btn.setOnClickListener(view -> rotate());

        ImageButton sound_add = findViewById(R.id.button_sound_add);
        ImageButton sound_cut = findViewById(R.id.button_sound_cut);
        sound_add.setOnClickListener(view -> changeSound(true));
        sound_cut.setOnClickListener(view -> changeSound(false));

    }

    private void getExtras() {
        Intent intent = getIntent();
        mode = intent.getIntExtra("mode",0);//新增：模式  0=普通播放，1=本地无弹幕视频，2=本地有弹幕视频
        url = intent.getStringExtra("url");//视频链接
        danmaku = intent.getStringExtra("danmaku");//弹幕链接
        String title = intent.getStringExtra("title");//视频标题
        if (danmaku != null)Log.e("弹幕",danmaku);
        if (url != null)Log.e("视频",url);
        if (title != null)Log.e("标题", title);
        Log.e("mode", String.valueOf(mode));
        if (title != null) text_title.setText(title);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickMgr(){
        //搞长按倍速累死个人
        //这个管普通点击
        control_layout.setOnClickListener(view -> clickUI());

        //这个管长按开始
        control_layout.setOnLongClickListener(view -> {
            if(SharedPreferencesUtil.getBoolean("player_longclick",false) && ijkPlayer !=null && (control_btn.getText() == "| |")) {
                if (!onLongClick) {
                    hidecon();
                    Vibrator vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(20L);
                    ijkPlayer.setSpeed(3.0F);
                    text_speed.setText("x 3.0");
                    onLongClick = true;
                }
            }
            return true;
        });

        //这个管长按结束
        control_layout.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP && onLongClick){
                onLongClick = false;
                ijkPlayer.setSpeed(speeds[speed_seekbar.getProgress()]);
                text_speed.setText(speedTexts[speed_seekbar.getProgress()]);
                return true;
            }
            return false;
        });
    }


    private void autohide() {
        if (autoHideTimer != null) autoHideTimer.cancel();
        autoHideTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {runOnUiThread(() -> {
                right_control.setVisibility(View.GONE);
                top_control.setVisibility(View.GONE);
                bottom_control.setVisibility(View.GONE);
                text_speed.setVisibility(View.GONE);
            });
            }
        };
        autoHideTimer.schedule(timerTask, 4000);
    }

    private void clickUI(){
        if ((top_control.getVisibility()) == View.GONE) showcon();
        else hidecon();
    }

    @SuppressLint("SetTextI18n")
    private void showcon() {
        right_control.setVisibility(View.VISIBLE);
        top_control.setVisibility(View.VISIBLE);
        bottom_control.setVisibility(View.VISIBLE);
        if(prepared) text_speed.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }

        autohide();
    }

    private void hidecon(){
        if (autoHideTimer != null) autoHideTimer.cancel();
        right_control.setVisibility(View.GONE);
        top_control.setVisibility(View.GONE);
        bottom_control.setVisibility(View.GONE);
        if(prepared) text_speed.setVisibility(View.GONE);
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
        if (ijkPlayer != null) ijkPlayer.release();
        if (mDanmakuView != null && mode!=1) mDanmakuView.release();
        if(loadingShowTimer != null) loadingShowTimer.cancel();

        if(danmakuFile != null && danmakuFile.exists()) danmakuFile.delete();

        Intent data = new Intent();
        data.putExtra("time", videonow);
        data.putExtra("isfin", finishWatching);
        setResult(0, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("debug","onPause");
        if(ijkPlayer!=null && prepared) ijkPlayer.pause();
        if (mDanmakuView != null && mode!=1) mDanmakuView.pause();
        if(control_btn!=null) control_btn.setText("▶");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e("debug","onResume");
    }

    private void setDisplay(String nowurl) {
        Log.e("debug","创建播放器");
        Log.e("debug-url",nowurl);

        ijkPlayer = new IjkMediaPlayer();
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", (SharedPreferencesUtil.getBoolean("player_codec",true) ? 1 : 0));
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", (SharedPreferencesUtil.getBoolean("player_audio",false) ? 1 : 0));
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch",1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        //这个坑死我！请允许我为解决此问题而大大地兴奋一下ohhhhhhhhhhhhhhhhhhhhhhhhhhhh
        //ijkplayer是自带一个useragent的，要把默认的改掉才能用！
        if(mode==0) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", ConfInfoApi.USER_AGENT_WEB);
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
                        ijkPlayer.setSurface(surface);
                        MPPrepare(nowurl);
                        Log.e("debug","设置surfaceTexture成功！");
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
                    if(needSurfaceHolder) {
                        ijkPlayer.setDisplay(surfaceHolder);
                        needSurfaceHolder = false;
                    }
                    if(prepared) {
                        Log.e("debug", "重新设置Holder");
                        ijkPlayer.seekTo(progressBar.getProgress());
                    }
                    else {MPPrepare(nowurl);}
                }
                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    Log.e("debug", "Holder没了");
                    needSurfaceHolder = true;
                    ijkPlayer.setDisplay(null);
                }
            });
            Timer textureTimer = new Timer();
            textureTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e("debug","循环检测");
                    if(!needSurfaceHolder) {
                        Log.e("debug","循环检测被打断");
                        this.cancel();
                    }
                    if(!surfaceHolder.isCreating()){
                        needSurfaceHolder = false;
                        ijkPlayer.setDisplay(surfaceHolder);
                        MPPrepare(nowurl);
                        Log.e("debug","设置surfaceHolder成功！");
                        this.cancel();
                    }
                }
            },0,200);
        }
    }

    private void MPPrepare(String nowurl){
        ijkPlayer.setOnPreparedListener(this);

        try {
            if(mode==0) {
                Log.e("debug","播放B站在线视频！");
                Map<String,String> headers = new HashMap<>();
                headers.put("Referer","https://www.bilibili.com/");
                headers.put("Cookie",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
                ijkPlayer.setDataSource(nowurl, headers);
            }
            else ijkPlayer.setDataSource(nowurl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ijkPlayer.setOnCompletionListener(iMediaPlayer -> {
            finishWatching = true;
            if(loop){
                ijkPlayer.seekTo(0);
                ijkPlayer.start();
                if(mode!=1 && mDanmakuView != null) {
                    mDanmakuView.seekTo(0L);
                    mDanmakuView.resume();          //别问为啥要seekto+resume而不是start(0)，问就是我也不知道！！！反正bug修好了！！！  ----RobinNotBad
                }
                Log.e("debug","循环播放");
            }
            else {
                if(mode!=1 && mDanmakuView != null) mDanmakuView.pause();
                control_btn.setText("▶");
            }
        });

        ijkPlayer.setOnErrorListener((iMediaPlayer, what, extra) -> {
            String EReport = "播放器可能遇到错误！\n错误码：" + what + "\n附加：" + extra;
            Log.e("ERROR",EReport);
            //Toast.makeText(PlayerActivity.this, EReport, Toast.LENGTH_LONG).show();
            return false;
        });

        ijkPlayer.setOnSeekCompleteListener(iMediaPlayer -> {
            if(control_btn.getText() == "| |") {
                if (mode!=1 && mDanmakuView != null) {
                    mDanmakuView.resume();
                }
            }
        });

        ijkPlayer.setOnBufferingUpdateListener((mp, percent) -> progressBar.setSecondaryProgress(percent * videoall / 100));

        if(mode==0) ijkPlayer.setOnInfoListener((mp, what, extra) -> {
            if(what == IMediaPlayer.MEDIA_INFO_BUFFERING_START){
                runOnUiThread(() -> {
                    loading_info.setVisibility(View.VISIBLE);
                    loading_text0.setText("正在缓冲");
                    showLoadingSpeed();
                    if (mode!=1 && dmkPlaying) {
                        mDanmakuView.pause();
                        dmkPlaying = false;
                    }
                });
            } else if(what == IMediaPlayer.MEDIA_INFO_BUFFERING_END){
                runOnUiThread(() -> {
                    if(loadingShowTimer!=null)loadingShowTimer.cancel();
                    loading_info.setVisibility(View.GONE);
                });
            }

            return false;
        });

        ijkPlayer.setScreenOnWhilePlaying(true);
        ijkPlayer.prepareAsync();
        Log.e("debug","开始准备");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPrepared(IMediaPlayer mediaPlayer) {
        prepared = true;
        videoall = (int) mediaPlayer.getDuration();

        changeVideoSize(mediaPlayer.getVideoWidth(),mediaPlayer.getVideoHeight());

        if (SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn", true)) {
            if (mode!=1) {
                danmaku_btn.setImageResource(R.mipmap.danmakuon);
                isdanmakushowing = true;
                mDanmakuView.start();
            } else {
                danmaku_btn.setImageResource(R.mipmap.danmakuoff);
                isdanmakushowing = false;
            }
            danmaku_btn.setOnClickListener(view -> {
                if(mode==1) Toast.makeText(this, "本视频无弹幕", Toast.LENGTH_SHORT).show();
                else if (mDanmakuView != null) {
                    mDanmakuView.setVisibility((isdanmakushowing ? View.GONE : View.VISIBLE));
                    danmaku_btn.setImageResource((isdanmakushowing ? R.mipmap.danmakuoff : R.mipmap.danmakuon));
                    isdanmakushowing = !isdanmakushowing;
                }
            });
            danmaku_btn.setVisibility(View.VISIBLE);
        }
        else danmaku_btn.setVisibility(View.GONE);
        //原作者居然把旋转按钮命名为danmaku_btn，也是没谁了...我改过来了  ----RobinNotBad
        //他大抵是觉得能用就行

        if (SharedPreferencesUtil.getBoolean("player_ui_showLoopBtn", true)) {
            if (loop) loop_btn.setImageResource(R.mipmap.loopon);
            else loop_btn.setImageResource(R.mipmap.loopoff);
            loop_btn.setOnClickListener(view -> {
                loop_btn.setImageResource((loop ? R.mipmap.loopoff : R.mipmap.loopon));
                loop = !loop;
            });
            loop_btn.setVisibility(View.VISIBLE);
        }
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


        loading_info.setVisibility(View.GONE);
        control_btn.setText("| |");
        text_all.setText(totalMinSTR + ":" + totalSecSTR);

        text_speed.setVisibility(top_control.getVisibility());
        text_speed.setOnClickListener(view -> speed_layout.setVisibility(View.VISIBLE));
        speed_layout.setOnClickListener(view -> speed_layout.setVisibility(View.GONE));


        progresschange();

        mediaPlayer.start();

        control_btn.setOnClickListener(view -> controlVideo());
    }

    private void showLoadingSpeed(){
        loadingShowTimer = new Timer();
        loadingShowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                float tcpSpeed = ijkPlayer.getTcpSpeed() / 1024f;
                String text = String.format(Locale.CHINA, "%.1f", tcpSpeed) + "KB/s";
                runOnUiThread(()-> loading_text1.setText(text));
            }
        },0,500);
    }

    private void changeVideoSize(int width, int height) {
        Log.e("debug-改变视频区域大小","开始");
        Log.e("wid", width + "");
        Log.e("hei", height + "");
        if (SharedPreferencesUtil.getBoolean("player_ui_round", false)) {
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
                if (ijkPlayer != null && !ischanging) {
                    videonow = (int) ijkPlayer.getCurrentPosition();
                    if (lastProgress != videonow) {               //检测进度是否在变动
                        lastProgress = videonow;
                        progressBar.setProgress(videonow);
                        if (mode!=1 && !dmkPlaying) {
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
                            text_now.setText(minuteSTR + ":" + secondSTR);
                        });
                    }
                }
            }
        };
        progresstimer.schedule(task, 0, 200);
    }

    private void downdanmu() {
        try {
            Response response = NetWorkUtil.get(danmaku, ConfInfoApi.webHeaders);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
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
        }catch (Exception e){
            MsgUtil.err(e,this);
        }
    }

    private void streamdanmaku(String danmakuPath) {
        Log.e("debug","streamdanmaku");
        Log.e("debug",danmakuPath);
        if (mDanmakuView != null) {
            BaseDanmakuParser mParser = createParser(danmakuPath);
            mDanmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {adddanmaku();}
                @Override
                public void updateTimer(DanmakuTimer timer) {
                    if(prepared) timer.update(ijkPlayer.getCurrentPosition());
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

    public void controlVideo() {
        if (control_btn.getText().toString().equals("| |")) {
            control_btn.setText("▶");
            ijkPlayer.pause();
            if (autoHideTimer != null) autoHideTimer.cancel();
            if (mDanmakuView != null && mode!=1) mDanmakuView.pause();
        } else {
            if (mDanmakuView != null && mode!=1) {
                if (videonow >= videoall - 250) {     //别问为啥有个>=，问就是这TM都能有误差，视频停止时并不是播放到最后一帧,可以多或者少出来几十甚至上百个毫秒...  ----RobinNotBad
                    ijkPlayer.seekTo(0);
                    mDanmakuView.seekTo(0L);
                    mDanmakuView.resume();
                    Log.e("debug", "播完重播");
                } else mDanmakuView.start(ijkPlayer.getCurrentPosition());
            }
            ijkPlayer.start();
            control_btn.setText("| |");
        }
        autohide();
    }

    @SuppressLint("SetTextI18n")
    public void changeSound(Boolean add_or_cut) {
        autoHideTimer.cancel();
        int soundnow = audioManager.getStreamVolume(STREAM_MUSIC);
        int maxsound = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        int added = maxsound;
        if (soundnow != maxsound) {
            added = soundnow + (add_or_cut ? 1 : -1);
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

    public void rotate(){
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
        if (ijkPlayer != null) {
            changeVideoSize(ijkPlayer.getVideoWidth(), ijkPlayer.getVideoHeight());
        }

        Log.e("debug","旋转屏幕结束");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("debug","onNewIntent");
        finish();
    }

}