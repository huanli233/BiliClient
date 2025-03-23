package com.RobinNotBad.BiliClient.activity.player;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.RobinNotBad.BiliClient.util.NetWorkUtil.USER_AGENT_WEB;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.api.DanmakuApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.event.SnackEvent;
import com.RobinNotBad.BiliClient.model.Subtitle;
import com.RobinNotBad.BiliClient.model.SubtitleLink;
import com.RobinNotBad.BiliClient.ui.widget.BatteryView;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerActivity extends Activity implements IjkMediaPlayer.OnPreparedListener {
    private boolean destroyed = false;

    private IjkMediaPlayer ijkPlayer;
    private IDanmakuView mDanmakuView;
    private DanmakuContext mContext;

    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture mSurfaceTexture;

    private SubtitleLink[] subtitleLinks = null;
    private Subtitle[] subtitles = null;
    private int subtitle_curr_index, subtitle_count;

    private RelativeLayout layout_control, layout_top, layout_video, layout_card_bg;
    private LinearLayout layout_speed, right_control, right_second, loading_info, bottom_buttons;
    private LinearLayout card_subtitle, card_danmaku_send;

    private ImageView img_loading;
    private ImageButton btn_control, btn_danmaku, btn_loop, btn_rotate, btn_menu, btn_subtitle, btn_danmaku_send;
    private SeekBar seekbar_progress, seekbar_speed;
    private TextView text_progress, text_online, text_volume, loading_text0, loading_text1, text_speed, text_newspeed;
    public TextView text_title, text_subtitle;

    private Timer progressTimer, speedTimer, loadingTimer, onlineTimer, surfaceTimer;
    private String video_url, danmaku_url;

    private boolean isPlaying, isPrepared, hasDanmaku,
            isOnlineVideo, isLiveMode, isSeeking, isDanmakuVisible;
    private boolean menu_opened = false;

    private int video_all, video_now, video_now_last;
    private long progress_history;
    private String progress_str;

    private int screen_width, screen_height;
    private int video_width, video_height;

    private AudioManager audioManager;

    private com.RobinNotBad.BiliClient.activity.player.ScaleGestureDetector scaleGestureDetector;
    private ViewScaleGestureListener scaleGestureListener;
    private float previousX, previousY;
    private boolean gesture_moved, gesture_scaled, gesture_click_disabled;
    private float video_origX, video_origY;
    private long timestamp_click;
    private boolean onLongClick = false;

    private final float[] speed_values = {0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 1.75F, 2.0F, 3.0F};
    private final String[] speed_strs = {"x 0.5", "x 0.75", "x 1.0", "x 1.25", "x 1.5", "x 1.75", "x 2.0", "x 3.0"};

    private boolean finishWatching = false;
    private boolean loop_enabled;

    private BatteryView batteryView;
    private BatteryManager batteryManager;

    private File danmakuFile;

    private boolean screen_landscape, screen_round;

    public String online_number = "0";

    private long aid, cid, mid;

    @Override
    public void onBackPressed() {
        if (!SharedPreferencesUtil.getBoolean("back_disable", false)) super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }

    private boolean getExtras() {
        Intent intent = getIntent();
        if(intent == null) return false;

        video_url = intent.getStringExtra("url");//视频链接
        danmaku_url = intent.getStringExtra("danmaku");//弹幕链接
        String title = intent.getStringExtra("title");//视频标题

        if(video_url == null) return false;
        if(danmaku_url != null) Logu.v("弹幕", danmaku_url);
        Logu.v("视频", video_url);
        Logu.v("标题", title);
        text_title.setText(title);

        aid = intent.getLongExtra("aid", 0);
        cid = intent.getLongExtra("cid", 0);
        mid = intent.getLongExtra("mid", 0);

        progress_history = intent.getIntExtra("progress", 0);

        isLiveMode = intent.getBooleanExtra("live_mode", false);
        isOnlineVideo = video_url.contains("http");
        hasDanmaku = !danmaku_url.equals("");
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logu.v("加载", "加载");
        super.onCreate(savedInstanceState);

        screen_landscape = SharedPreferencesUtil.getBoolean("player_autolandscape", false) || SharedPreferencesUtil.getBoolean("ui_landscape", false);
        if(SharedPreferencesUtil.getBoolean("dev_player_rotate_software",false) && screen_landscape) {
            MsgUtil.showMsg("不支持默认横屏！");
            screen_landscape = false;
        }
        else {
            setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_player);
        findview();
        if(!getExtras()) {
            finish();
            return;
        }

        initUI();

        IjkMediaPlayer.loadLibrariesOnce(null);

        ijkPlayer = new IjkMediaPlayer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryView.setPower(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        } else batteryView.setVisibility(View.GONE);

        loop_enabled = SharedPreferencesUtil.getBoolean("player_loop", false);
        Glide.with(this).load(R.mipmap.load).into(img_loading);

        File cachepath = getCacheDir();
        if (!cachepath.exists()) cachepath.mkdirs();
        danmakuFile = new File(cachepath, "danmaku.xml");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setVideoGestures();
        autohideReset();

        initSeekbars();

        if(isLiveMode){
            btn_control.setVisibility(View.INVISIBLE); //暂停的话可能会出一些bug，那就别暂停了，卡住就退出重进吧（
            seekbar_progress.setVisibility(View.GONE);
            seekbar_progress.setEnabled(false);
            streamDanmaku(null); //用来初始化一下弹幕层
            danmuSocketConnect();
        }

        layout_control.postDelayed(() -> CenterThreadPool.run(() -> {    //等界面加载完成
            if(isLiveMode) {
                runOnUiThread(()-> btn_menu.setVisibility(View.GONE));
                setDisplay();
                return;
            }

            runOnUiThread(() -> {
                loading_text0.setText("装填弹幕中");
                loading_text1.setText("(≧∇≦)");
            });
            if(isOnlineVideo) {
                downdanmu();
                if(SharedPreferencesUtil.getBoolean("player_subtitle_autoshow", true)) downSubtitle(false);
            }
            else {
                runOnUiThread(() -> {
                    btn_subtitle.setVisibility(View.GONE);
                    btn_danmaku_send.setVisibility(View.GONE);
                });
                streamDanmaku(danmaku_url);
            }

            if(!destroyed) setDisplay();
        }), 60);
    }


    private void findview() {
        layout_control = findViewById(R.id.control_layout);
        layout_top = findViewById(R.id.top);
        right_control = findViewById(R.id.right_control);
        right_second = findViewById(R.id.right_second);
        layout_card_bg = findViewById(R.id.card_bg);
        card_subtitle = findViewById(R.id.subtitle_card);
        card_danmaku_send = findViewById(R.id.danmaku_send_card);

        loading_info = findViewById(R.id.loading_info);

        img_loading = findViewById(R.id.circle);
        text_progress = findViewById(R.id.text_progress);
        text_online = findViewById(R.id.text_online);
        btn_danmaku = findViewById(R.id.danmaku_btn);
        btn_loop = findViewById(R.id.loop_btn);
        btn_rotate = findViewById(R.id.rotate_btn);
        btn_menu = findViewById(R.id.menu_btn);
        btn_danmaku_send = findViewById(R.id.danmaku_send_btn);
        btn_subtitle = findViewById(R.id.subtitle_btn);
        btn_control = findViewById(R.id.button_video);
        seekbar_progress = findViewById(R.id.videoprogress);
        loading_text0 = findViewById(R.id.loading_text0);
        loading_text1 = findViewById(R.id.loading_text1);
        text_title = findViewById(R.id.text_title);
        text_volume = findViewById(R.id.showsound);
        layout_video = findViewById(R.id.videoArea);
        mDanmakuView = findViewById(R.id.sv_danmaku);
        batteryView = findViewById(R.id.battery);

        text_speed = findViewById(R.id.text_speed);
        layout_speed = findViewById(R.id.layout_speed);
        seekbar_speed = findViewById(R.id.seekbar_speed);
        text_newspeed = findViewById(R.id.text_newspeed);
        bottom_buttons = findViewById(R.id.bottom_buttons);

        text_subtitle = findViewById(R.id.text_subtitle);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setVideoGestures() {
        if (SharedPreferencesUtil.getBoolean("player_scale", true)) {
            scaleGestureListener = new ViewScaleGestureListener(layout_video);
            scaleGestureDetector = new ScaleGestureDetector(this, scaleGestureListener);

            boolean doublemove_enabled = SharedPreferencesUtil.getBoolean("player_doublemove", true);  //是否启用双指移动

            layout_control.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                int pointerCount = event.getPointerCount();
                boolean singleTouch = pointerCount == 1;
                boolean doubleTouch = pointerCount == 2;

                //Logu.v("gesture", event.getEventTime() + "");
                scaleGestureDetector.onTouchEvent(event);
                boolean gesture_scaling = scaleGestureListener.scaling;

                if (!gesture_scaled && gesture_scaling) gesture_scaled = true;

                //Logu.v("gesture", (scaling ? "scaled-yes" : "scaled-no"));

                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        if (singleTouch) {
                            if (gesture_scaling) {
                                videoMoveBy(0, 0);    //防止单指缩放出框
                            } else if (!(gesture_scaled && !doublemove_enabled)) {
                                float currentX = event.getX(0);  //单指移动
                                float currentY = event.getY(0);
                                float deltaX = currentX - previousX;
                                float deltaY = currentY - previousY;
                                if (deltaX != 0f || deltaY != 0f) {
                                    videoMoveBy(deltaX, deltaY);
                                    previousX = currentX;
                                    previousY = currentY;
                                }
                            }
                        }
                        if (doubleTouch && doublemove_enabled) {
                            float currentX = (event.getX(0) + event.getX(1)) / 2;
                            float currentY = (event.getY(0) + event.getY(1)) / 2;
                            float deltaX = currentX - previousX;
                            float deltaY = currentY - previousY;
                            if (deltaX != 0f || deltaY != 0f) {
                                videoMoveBy(deltaX, deltaY);
                                previousX = currentX;
                                previousY = currentY;
                            }
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:
                        if (singleTouch) {  //如果是单指按下，设置起始位置为当前手指位置
                            previousX = event.getX(0);
                            previousY = event.getY(0);
                            //Logu.v("gesture", "touch_start:" + previousX + "," + previousY);
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (doubleTouch) {  //如果是双指按下，设置起始位置为两指连线的中心点
                            previousX = (event.getX(0) + event.getX(1)) / 2;
                            previousY = (event.getY(0) + event.getY(1)) / 2;
                            //Logu.v("gesture","double_touch");
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (doubleTouch) {
                            int index = event.getActionIndex();  //actionIndex是抬起来的手指位置
                            previousX = event.getX((index == 0 ? 1 : 0));
                            previousY = event.getY((index == 0 ? 1 : 0));
                            //Logu.v("gesture","single_touch");
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        //Logu.v("gesture","touch_stop");
                        if (onLongClick) {
                            onLongClick = false;
                            ijkPlayer.setSpeed(speed_values[seekbar_speed.getProgress()]);
                            mDanmakuView.setSpeed(speed_values[seekbar_speed.getProgress()]);
                            text_speed.setText(speed_strs[seekbar_speed.getProgress()]);
                        }
                        if (gesture_moved) gesture_moved = false;
                        if (gesture_scaled) gesture_scaled = false;
                        break;
                }

                if (!gesture_click_disabled && (gesture_moved || gesture_scaled)) {
                    gesture_click_disabled = true;
                    hidecon.run();
                }

                return false;
            });
        } else {
            layout_control.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && onLongClick) {
                    onLongClick = false;
                    ijkPlayer.setSpeed(speed_values[seekbar_speed.getProgress()]);
                    mDanmakuView.setSpeed(speed_values[seekbar_speed.getProgress()]);
                    text_speed.setText(speed_strs[seekbar_speed.getProgress()]);
                }
                return false;
            });
        }

        //这个管普通点击
        layout_control.setOnClickListener(view -> {
            if (gesture_click_disabled) gesture_click_disabled = false;
            else clickUI();
        });
        //这个管长按开始
        layout_control.setOnLongClickListener(view -> {
            if (SharedPreferencesUtil.getBoolean("player_longclick", true) && ijkPlayer != null && (isPlaying) && (!isLiveMode)) {
                if (!onLongClick && !gesture_click_disabled) {
                    hidecon.run();
                    ijkPlayer.setSpeed(3.0F);
                    mDanmakuView.setSpeed(3.0f);
                    text_speed.setText("x 3.0");
                    onLongClick = true;
                    Logu.v("gesture", "longclick_down");
                    return true;
                }
                return false;
            }
            return false;
        });

    }


    private void autohideReset() {
        layout_control.removeCallbacks(hidecon);
        layout_control.postDelayed(hidecon,4000);
    }

    private void clickUI() {
        long now_timestamp = System.currentTimeMillis();
        if (now_timestamp - timestamp_click < 300) {
            if (SharedPreferencesUtil.getBoolean("player_scale", true) && scaleGestureListener.can_reset) {
                scaleGestureListener.can_reset = false;
                layout_video.setX(video_origX);
                layout_video.setY(video_origY);
                layout_video.setScaleX(1.0f);
                layout_video.setScaleY(1.0f);
            } else if (!isLiveMode) {
                if (isPlaying) playerPause();
                else playerResume();
                showcon();
            }
        } else {
            timestamp_click = now_timestamp;
            if ((layout_top.getVisibility()) == View.GONE) showcon();
            else hidecon.run();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showcon() {
        right_control.setVisibility(View.VISIBLE);
        layout_top.setVisibility(View.VISIBLE);
        bottom_buttons.setVisibility(View.VISIBLE);
        seekbar_progress.setVisibility(View.VISIBLE);
        seekbar_progress.setEnabled(false);
        seekbar_progress.postDelayed(progressbarEnable,200);
        if (isPrepared && !isLiveMode) text_speed.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView.setPower(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }
        if(screen_round) {
            text_progress.setGravity(Gravity.NO_GRAVITY);
            text_progress.setPadding(ToolsUtil.dp2px(24f),0,0,0);
            if(onlineTimer != null) text_online.setVisibility(View.VISIBLE);
        }

        autohideReset();
    }

    private final Runnable progressbarEnable = () -> seekbar_progress.setEnabled(true);

    private final Runnable hidecon = () -> {
        right_control.setVisibility(View.GONE);
        layout_top.setVisibility(View.GONE);
        bottom_buttons.setVisibility(View.GONE);
        seekbar_progress.setVisibility(View.GONE);
        if (isPrepared) text_speed.setVisibility(View.GONE);
        if(screen_round) {
            text_progress.setGravity(Gravity.CENTER);
            text_progress.setPadding(0,0,0,ToolsUtil.dp2px(8f));
            if(onlineTimer != null) text_online.setVisibility(View.GONE);
        }
        if(menu_opened) btn_menu.performClick();
    };


    private void setDisplay() {
        Logu.v("创建播放器");
        Logu.v("url", video_url);


        runOnUiThread(() -> loading_text0.setText("初始化播放"));

        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", (SharedPreferencesUtil.getBoolean("player_codec", true) ? 1 : 0));
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", (SharedPreferencesUtil.getBoolean("player_audio", false) ? 1 : 0));
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 2);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);

        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);

        //这个坑死我！请允许我为解决此问题而大大地兴奋一下ohhhhhhhhhhhhhhhhhhhhhhhhhhhh
        //ijkplayer是自带一个useragent的，要把默认的改掉才能用！
        if (isOnlineVideo) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 15 * 1000 * 1000);
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", USER_AGENT_WEB);
            Logu.v("设置ua");
        }

        Logu.v("准备设置显示");
        if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 26)) {            //Texture
            Logu.v("使用texture模式");
            surfaceTimer = new Timer();
            surfaceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Logu.v("循环检测");
                    if (mSurfaceTexture != null) {
                        this.cancel();
                        Surface surface = new Surface(mSurfaceTexture);
                        ijkPlayer.setSurface(surface);
                        MPPrepare(video_url);
                        Logu.v("设置surfaceTexture成功！");
                    }
                }
            }, 0, 200);
        }
        else {
            Logu.v("使用surface模式");
            SurfaceHolder surfaceHolder = surfaceView.getHolder();       //Surface
            Logu.v("获取surfaceHolder成功！");
            surfaceTimer = new Timer();
            surfaceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Logu.v("循环检测");
                    if (!surfaceHolder.isCreating()) {
                        this.cancel();
                        Logu.v("定时器结束！");
                        ijkPlayer.setDisplay(surfaceHolder);
                        Logu.v("设置surfaceHolder成功！");
                        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                                if(!destroyed) {
                                    Logu.v("surface", "重新设置Holder");
                                    ijkPlayer.setDisplay(surfaceHolder);
                                    if (isPrepared) {
                                        ijkPlayer.seekTo(seekbar_progress.getProgress());
                                    }
                                }
                            }

                            @Override
                            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                            }

                            @Override
                            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                                Logu.v("surface", "Holder没了");
                                if(isPrepared && !destroyed) ijkPlayer.setDisplay(null);
                            }
                        });
                        Logu.v("添加callback成功！");
                        MPPrepare(video_url);
                    }
                }
            }, 0, 200);
        }
    }

    private void MPPrepare(String nowurl) {
        ijkPlayer.setOnPreparedListener(this);

        if (isLiveMode) runOnUiThread(() -> loading_text0.setText("载入直播中"));
        else runOnUiThread(() -> loading_text0.setText("载入视频中"));
        try {
            if (isOnlineVideo) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", "https://www.bilibili.com/");
                headers.put("Cookie", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
                ijkPlayer.setDataSource(nowurl, headers);
            } else ijkPlayer.setDataSource(nowurl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ijkPlayer.setOnCompletionListener(iMediaPlayer -> {
            finishWatching = true;
            if (loop_enabled) {
                ijkPlayer.seekTo(0);
                ijkPlayer.start();
            } else {
                isPlaying = false;
                if (hasDanmaku) mDanmakuView.pause();
                btn_control.setImageResource(R.drawable.btn_player_play);
            }
        });

        ijkPlayer.setOnErrorListener((iMediaPlayer, what, extra) -> {
            String EReport = "播放器可能遇到错误！\n错误码：" + what + "\n附加：" + extra;
            Logu.e("ijk-err", EReport);
            //Toast.makeText(PlayerActivity.this, EReport, Toast.LENGTH_LONG).show();
            return false;
        });

        ijkPlayer.setOnBufferingUpdateListener((mp, percent) -> seekbar_progress.setSecondaryProgress(percent * video_all / 100));

        if(isOnlineVideo || isLiveMode) ijkPlayer.setOnInfoListener((mp, what, extra) -> {
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                runOnUiThread(() -> {
                    loading_info.setVisibility(View.VISIBLE);
                    loading_text0.setText("正在缓冲");
                    showLoadingSpeed();
                    if (isPlaying) mDanmakuView.pause();
                });
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                runOnUiThread(() -> {
                    if (loadingTimer != null) loadingTimer.cancel();
                    loading_info.setVisibility(View.GONE);
                    if (isPlaying) mDanmakuView.start(ijkPlayer.getCurrentPosition());
                });
            }

            return false;
        });

        ijkPlayer.setScreenOnWhilePlaying(true);
        ijkPlayer.prepareAsync();
        Logu.v("开始准备");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPrepared(IMediaPlayer mediaPlayer) {
        if(destroyed){
            mediaPlayer.release();
            return;
        }

        isPrepared = true;
        video_all = (int) mediaPlayer.getDuration();

        changeVideoSize();

        if(isLiveMode || hasDanmaku) mDanmakuView.start();
        if (SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn", true)) {
            isDanmakuVisible = !SharedPreferencesUtil.getBoolean("pref_switch_danmaku",true);
            //这里设置值是反的，因为下面直接调用监听器点击按钮
            btn_danmaku.setOnClickListener(view -> {
                if (isDanmakuVisible) mDanmakuView.hide();
                else mDanmakuView.show();
                btn_danmaku.setImageResource((isDanmakuVisible ? R.mipmap.danmakuoff : R.mipmap.danmakuon));
                isDanmakuVisible = !isDanmakuVisible;
                SharedPreferencesUtil.putBoolean("pref_switch_danmaku",isDanmakuVisible);
            });
            btn_danmaku.performClick();

            btn_danmaku.setVisibility(View.VISIBLE);
        } else btn_danmaku.setVisibility(View.GONE);
        //原作者居然把旋转按钮命名为danmaku_btn，也是没谁了...我改过来了  ----RobinNotBad
        //他大抵是觉得能用就行

        if (!isLiveMode) {
            if (loop_enabled) btn_loop.setImageResource(R.mipmap.loopon);
            else btn_loop.setImageResource(R.mipmap.loopoff);
            btn_loop.setOnClickListener(view -> {
                btn_loop.setImageResource((loop_enabled ? R.mipmap.loopoff : R.mipmap.loopon));
                loop_enabled = !loop_enabled;
            });
            btn_loop.setVisibility(View.VISIBLE);
        }


        seekbar_progress.setMax(video_all);
        progress_str = ToolsUtil.toTime(video_all / 1000);

        if (SharedPreferencesUtil.getBoolean("player_from_last", true) && !isLiveMode) {
            if (progress_history > 6 && ((video_all / 1000) - progress_history) > 6) { //阈值
                mediaPlayer.seekTo(progress_history * 1000);
                runOnUiThread(() -> MsgUtil.showMsg("已从上次的位置播放"));
            }
        }

        loading_info.setVisibility(View.GONE);
        isPlaying = true;
        btn_control.setImageResource(R.drawable.btn_player_pause);

        text_speed.setVisibility(layout_top.getVisibility());
        if (isLiveMode) text_speed.setVisibility(View.GONE);
        text_speed.setOnClickListener(view -> layout_speed.setVisibility(View.VISIBLE));
        layout_speed.setOnClickListener(view -> layout_speed.setVisibility(View.GONE));

        progressChange();
        onlineChange();

        mediaPlayer.start();

        btn_control.setOnClickListener(view -> controlVideo());
        btn_subtitle.setOnClickListener(view -> CenterThreadPool.run(() -> downSubtitle(true)));
    }

    private void showLoadingSpeed() {
        loadingTimer = new Timer();
        loadingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String text = String.format(Locale.CHINA, "%.1f", ijkPlayer.getTcpSpeed() / 1024f) + "KB/s";
                runOnUiThread(() -> loading_text1.setText(text));
            }
        }, 0, 500);
    }

    private void changeVideoSize() {
        if(!isPrepared || ijkPlayer == null) return;
        int width = ijkPlayer.getVideoWidth();
        int height = ijkPlayer.getVideoHeight();
        Logu.v("screen", screen_width + "x" + screen_height);
        Logu.v("video", width + "x" + height);

        if (SharedPreferencesUtil.getBoolean("player_ui_round", false)) {
            float video_mul = (float) height / (float) width;
            double sqrt = Math.sqrt(screen_width * screen_width / ((double)(height * height) / (width * width) + 1));
            video_height = (int) (sqrt * video_mul + 0.5);
            video_width = (int) (sqrt + 0.5);
        } else {
            int width_case1 = width * screen_height / height;
            int height_case2 = height * screen_width / width;

            if (width_case1 <= screen_width) {
                video_width = width_case1;
                video_height = screen_height;
            } else {
                video_width = screen_width;
                video_height = height_case2;
            }
        }

        runOnUiThread(() -> {
            layout_video.setLayoutParams(new RelativeLayout.LayoutParams(video_width, video_height));
            Logu.v("改变视频区域大小", video_width + "x" + video_height);
            video_origX = (screen_width - video_width) / 2f;
            video_origY = (screen_height - video_height) / 2f;

            layout_video.postDelayed(() -> {
                layout_video.setX(video_origX);
                layout_video.setY(video_origY);
                Logu.v("改变视频位置", ((screen_width - video_width) / 2) + "," + ((screen_height - video_height) / 2));
            }, 60);  //别问为什么，问就是必须这么写，要等上面的绘制完成
        });
    }


    private void progressChange() {
        progressTimer = new Timer();
        TimerTask task = new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (isPrepared && isPlaying && !isSeeking) {
                    video_now = (int) ijkPlayer.getCurrentPosition();
                    if (video_now_last != video_now) {               //检测进度是否在变动
                        video_now_last = video_now;
                        float curr_sec = video_now / 1000f;
                        runOnUiThread(() -> {
                            if (isLiveMode) {
                                text_progress.setText(ToolsUtil.toTime((int) curr_sec));
                                text_online.setText(online_number);
                            }
                            else {
                                seekbar_progress.setProgress(video_now);
                                //progressBar上有一个onProgressChange的监听器，文字更改在那里
                            }
                        });
                        if(subtitles != null) showSubtitle(curr_sec);
                        else runOnUiThread(()->text_subtitle.setVisibility(View.GONE));
                    }
                }
            }
        };
        progressTimer.schedule(task, 0, 250);
    }

    private void onlineChange() {
        if(!SharedPreferencesUtil.getBoolean("player_show_online", false) || isLiveMode || aid==0 || cid==0) return;

        onlineTimer = new Timer();
        TimerTask task = new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (ijkPlayer != null) {
                    try {
                        online_number = VideoInfoApi.getWatching(aid, cid);
                        runOnUiThread(()->{
                            if (!online_number.isEmpty())
                                text_online.setText(online_number + "人在看");
                            else text_online.setText("");
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            MsgUtil.err(e);
                            text_online.setVisibility(View.GONE);
                        });
                        this.cancel();
                    }
                }
            }
        };
        onlineTimer.schedule(task, 0, 5000);
    }

    private void getSubtitle(String subtitle_url){
        if (subtitle_url == null || subtitle_url.isEmpty()) return;
        try {
            subtitles = PlayerApi.getSubtitle(subtitle_url);
            subtitle_count = subtitles.length;
            subtitle_curr_index = 0;
            runOnUiThread(()-> btn_subtitle.setImageResource(R.mipmap.subtitle_on));
        } catch (Exception e){
            MsgUtil.err(e);
        }
    }

    private void showSubtitle(float curr_sec) {
        Subtitle subtitle_curr = subtitles[subtitle_curr_index];

        boolean need_adjust = true;
        boolean need_show = true;

        while (need_adjust) {
            if (curr_sec < subtitle_curr.from) {  //进度在当前字幕的起始位置之前
                //如果不是第一条字幕，且进度在上一条字幕的结束位置之前，那么字幕前移一位
                //否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitle_curr_index != 0 && curr_sec < subtitles[subtitle_curr_index - 1].to) {
                    subtitle_curr_index--;
                }
                else {
                    need_adjust = false;
                    need_show = false;
                }
            }
            else if (curr_sec > subtitle_curr.to) {  //在当前字幕的结束位置之后
                //如果不是最后一条字幕，且进度在下一条字幕的开始位置之后，那么字幕后移一位
                //否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitle_curr_index+1 < subtitle_count && curr_sec > subtitles[subtitle_curr_index + 1].from) {
                    subtitle_curr_index++;
                }
                else {
                    need_adjust = false;
                    need_show = false;
                }
            }
            else need_adjust = false;  //在当前字幕的时间段内，则退出校准
        }

        if(need_show) runOnUiThread(()->{
            text_subtitle.setText(subtitles[subtitle_curr_index].content);
            text_subtitle.setVisibility(View.VISIBLE);
        });
        else runOnUiThread(()->text_subtitle.setVisibility(View.GONE));
    }

    private int subtitle_selected = -1;
    private void downSubtitle(boolean from_btn){
        try {
            if(subtitleLinks == null) subtitleLinks = PlayerApi.getSubtitleLinks(aid, cid);

            if(subtitleLinks.length == 1){
                if(from_btn) MsgUtil.showMsg("本视频无字幕");
                return;
            }

            boolean ai_not_only = (subtitleLinks.length > 2 || (subtitleLinks.length == 2 && !subtitleLinks[0].isAI));
            boolean ai_allowed = (from_btn || SharedPreferencesUtil.getBoolean("player_subtitle_ai_allowed", false));

            if(ai_not_only || ai_allowed) {
                if(subtitle_selected == -1) subtitle_selected = subtitleLinks.length;

                runOnUiThread(()->{
                    RecyclerView subtitleRecycler = findViewById(R.id.subtitle_list);
                    SubtitleAdapter adapter = new SubtitleAdapter();
                    adapter.setData(subtitleLinks);
                    adapter.setSelectedItemIndex(subtitle_selected);
                    adapter.setOnItemClickListener(index -> {
                        layout_card_bg.setVisibility(View.GONE);
                        card_subtitle.setVisibility(View.GONE);
                        subtitle_selected = index;
                        
                        if(subtitleLinks[index].id == -1) {
                            subtitles = null;
                            btn_subtitle.setImageResource(R.mipmap.subtitle_off);
                        }
                        else CenterThreadPool.run(() -> getSubtitle(subtitleLinks[index].url));
                    });
                    subtitleRecycler.setLayoutManager(new CustomLinearManager(this, LinearLayoutManager.HORIZONTAL, false));
                    subtitleRecycler.setHasFixedSize(true);
                    subtitleRecycler.setAdapter(adapter);
                    layout_card_bg.setVisibility(View.VISIBLE);
                    card_subtitle.setVisibility(View.VISIBLE);
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            MsgUtil.err(e);
        }
    }

    private void downdanmu() {
        if (danmaku_url.isEmpty()) return;
        try {
            Response response = NetWorkUtil.get(danmaku_url, NetWorkUtil.webHeaders);
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
            streamDanmaku(danmakuFile.toString());
        } catch (Exception e) {
            runOnUiThread(() -> MsgUtil.err(e));
        }
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

        assert loader != null;
        loader.load(stream);
        BaseDanmakuParser parser = new BiliDanmukuParser();
        parser.sharedPreferences = SharedPreferencesUtil.getSharedPreferences();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    private void streamDanmaku(String danmakuPath) {
        Logu.v("danmaku", "stream");

        mContext = DanmakuContext.create();
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, SharedPreferencesUtil.getInt("player_danmaku_maxline", 15));
        HashMap<Integer, Boolean> overlap = new HashMap<>();
        overlap.put(BaseDanmaku.TYPE_SCROLL_LR, SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap", true));
        overlap.put(BaseDanmaku.TYPE_FIX_BOTTOM, SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap", true));
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 1)
                .setDuplicateMergingEnabled(SharedPreferencesUtil.getBoolean("player_danmaku_mergeduplicate", false))
                .setScrollSpeedFactor(SharedPreferencesUtil.getFloat("player_danmaku_speed", 1.0f))
                .setScaleTextSize(SharedPreferencesUtil.getFloat("player_danmaku_size", 0.7f))//缩放值
                .setMaximumLines(maxLinesPair)
                .setDanmakuTransparency(SharedPreferencesUtil.getFloat("player_danmaku_transparency", 0.5f))
                .preventOverlapping(overlap);

        BaseDanmakuParser mParser = createParser(danmakuPath);

        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                Logu.v("danmaku", "prepared");
                addDanmaku("弹幕君准备完毕～(*≧ω≦)", Color.WHITE);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                // 不需要if(isPlaying)，因为本来就为了让弹幕跟随ijkPlayer的时间停止而停止
                timer.update(ijkPlayer.getCurrentPosition()); //实时同步弹幕和播放器时间
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {
            }

            @Override
            public void drawingFinished() {
            }
        });
        mDanmakuView.enableDanmakuDrawingCache(true);
        mDanmakuView.prepare(mParser, mContext);
    }

    public void addDanmaku(String text, int color) {
        addDanmaku(text, color, 25, 1, 0);
    }

    public void addDanmaku(String text, int color, int textSize, int type, int backgroundColor) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(type);
        if (text == null || danmaku == null || ijkPlayer == null) return;
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.priority = 1;
        danmaku.textColor = color;
        danmaku.backgroundColor = backgroundColor;
        danmaku.textSize = textSize * (mContext.getDisplayer().getDensity() - 0.6f);
        danmaku.time = mDanmakuView.getCurrentTime() + 100;
        mDanmakuView.addDanmaku(danmaku);
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
        if (isPlaying) {
            playerPause();
        } else {
            isPlaying = true;
            // 因为弹幕实时同步，不需要自行设置弹幕时间了
            if (video_now >= video_all - 250) {     //别问为啥有个>=，问就是这TM都能有误差，视频停止时并不是播放到最后一帧,可以多或者少出来几十甚至上百个毫秒...  ----RobinNotBad
                ijkPlayer.seekTo(0);
                // mDanmakuView.seekTo(0L);
                mDanmakuView.resume();
                Logu.v("播完重播");
            } // else mDanmakuView.start(ijkPlayer.getCurrentPosition());
            ijkPlayer.start();
            btn_control.setImageResource(R.drawable.btn_player_pause);
        }
        autohideReset();
    }

    @SuppressLint("SetTextI18n")
    public void changeVolume(Boolean add_or_cut) {
        int volumeNow = audioManager.getStreamVolume(STREAM_MUSIC);
        int volumeMax = audioManager.getStreamMaxVolume(STREAM_MUSIC);
        int volumeNew = volumeNow + (add_or_cut ? 1 : -1);
        if (volumeNew >= 0 && volumeNew <= volumeMax) {
            audioManager.setStreamVolume(STREAM_MUSIC, volumeNew, 0);
            volumeNow = volumeNew;
        }
        int show = (int) ((float) volumeNow / (float) volumeMax * 100);

        text_volume.setVisibility(View.VISIBLE);
        text_volume.setText("音量：" + show + "%");

        text_volume.removeCallbacks(hideVolume);
        text_volume.postDelayed(hideVolume, 3000);
        autohideReset();
    }

    private final Runnable hideVolume = () -> text_volume.setVisibility(View.GONE);


    /**
     * 软件旋屏，给某些特殊设备用的。
     * 终端屎山又增高啦
     */
    private void softwareRotate(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = screen_landscape ? displayMetrics.heightPixels : displayMetrics.widthPixels;
        screen_height = screen_landscape ? displayMetrics.widthPixels : displayMetrics.heightPixels;

        ViewGroup root_layout = findViewById(R.id.root_layout);
        ViewGroup.LayoutParams params = root_layout.getLayoutParams();
        params.width = screen_width;
        params.height = screen_height;

        if(isPrepared && !destroyed) runOnUiThread(()->{
            root_layout.setLayoutParams(params);
            root_layout.setPivotX(0);
            root_layout.setPivotY(0);
            root_layout.setX(screen_landscape ? screen_height : 0);
            root_layout.setRotation(screen_landscape ? 90 : 0);
            if(SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 26)){
                if(textureView != null){
                    Matrix matrix = new Matrix();
                    textureView.getTransform(matrix);
                    matrix.postRotate(0);
                    textureView.setTransform(matrix);
                }
            }
            else{
                MsgUtil.showMsg("请切换为TextureView才能支持软件旋屏！");
            }
        });
        changeVideoSize();
    }


    private void videoMoveBy(float dx, float dy) {
        float x = dx + layout_video.getX();
        float y = dy + layout_video.getY();

        float width_delta = 0.5f * video_width * (layout_video.getScaleX() - 1f);
        float height_delta = 0.5f * video_height * (layout_video.getScaleY() - 1f);
        float video_x_min = video_origX - width_delta;
        float video_x_max = video_origX + width_delta;
        float video_y_min = video_origY - height_delta;
        float video_y_max = video_origY + height_delta;

        if (x < video_x_min) x = video_x_min;
        if (x > video_x_max) x = video_x_max;
        if (y < video_y_min) y = video_y_min;
        if (y > video_y_max) y = video_y_max;

        if (layout_video.getX() != x || layout_video.getY() != y) {
            //Logu.v("gesture","moveto:" + x + "," + y);
            layout_video.setX(x);
            layout_video.setY(y);
            if (!gesture_moved && (Math.abs(video_origX - x) > 5f || Math.abs(video_origY - y) > 5f)) {
                gesture_moved = true;
            }
        }
    }

    private void playerPause() {
        isPlaying = false;
        if (ijkPlayer != null && isPrepared) ijkPlayer.pause();
        // 这里不需要pause()，实时同步弹幕时间之后，暂停视频弹幕会自行停住不动，pause()反而会导致弹幕卡住，无法通过start()重新滚动
        // if (hasDanmaku) mDanmakuView.pause();
        if (btn_control != null) btn_control.setImageResource(R.drawable.btn_player_play);
    }

    private void playerResume() {
        isPlaying = true;
        if (ijkPlayer != null && isPrepared) {
            ijkPlayer.start();
        //    if (hasDanmaku) mDanmakuView.start(ijkPlayer.getCurrentPosition());
        }
        if (btn_control != null) btn_control.setImageResource(R.drawable.btn_player_pause);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logu.v("开始旋转屏幕");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;//获取屏宽
        screen_height = displayMetrics.heightPixels;//获取屏高
        changeVideoSize();

        Logu.v("旋转屏幕结束");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logu.v("onNewIntent");
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logu.v("onPause");
        if (!SharedPreferencesUtil.getBoolean("player_background", false)) {
            playerPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logu.v("onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logu.v("onStop");
    }


    WebSocket liveWebSocket = null;

    @Override
    protected void onDestroy() {
        if(!isFinishing()) return;    //貌似有些设备会先调用一下onDestroy，头大……

        Logu.v("结束");
        if (eventBusInit) {
            EventBus.getDefault().unregister(this);
            eventBusInit = false;
        }
        destroyed = true;

        cancelAllTimers();

        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
        if (ijkPlayer != null) {
            ijkPlayer.release();
            ijkPlayer = null;
        }


        if (danmakuFile != null && danmakuFile.exists()) danmakuFile.delete();

        if (liveWebSocket != null) {
            liveWebSocket.close(1000, "");
            liveWebSocket = null;
        }

        setRequestedOrientation(SharedPreferencesUtil.getBoolean("ui_landscape", false) ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onDestroy();
    }

    private void cancelAllTimers(){
        if (surfaceTimer != null) {
            surfaceTimer.cancel();
            surfaceTimer = null;
        }
        if (progressTimer != null) {
            progressTimer.cancel();
            progressTimer = null;
        }
        if (onlineTimer != null) {
            onlineTimer.cancel();
            onlineTimer = null;
        }
        if (loadingTimer != null) {
            loadingTimer.cancel();
            loadingTimer = null;
        }
        layout_control.removeCallbacks(hidecon);
        text_volume.removeCallbacks(hideVolume);
        seekbar_progress.removeCallbacks(progressbarEnable);
    }

    OkHttpClient okHttpClient;

    private void danmuSocketConnect() {
        CenterThreadPool.run(() -> {
            try {
                String url = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?type=0&id=" + aid;
                ArrayList<String> mHeaders = new ArrayList<>() {{
                    add("Cookie");
                    add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
                    add("Referer");
                    add("https://live.bilibili.com/" + aid);
                    add("Origin");
                    add("https://live.bilibili.com");
                    add("User-Agent");
                    add(USER_AGENT_WEB);
                }};
                Response response = NetWorkUtil.get(url, mHeaders);
                JSONObject data = new JSONObject(Objects.requireNonNull(response.body()).string()).getJSONObject("data");
                JSONObject host = data.getJSONArray("host_list").getJSONObject(0);

                url = "wss://" + host.getString("host") + ":" + host.getInt("wss_port") + "/sub";
                Logu.v("连接WebSocket", url);

                okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .header("Cookie", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""))
                        .header("Origin", "https://live.bilibili.com")
                        .header("User-Agent", USER_AGENT_WEB)
                        .build();

                PlayerDanmuClientListener listener = new PlayerDanmuClientListener();
                listener.mid = mid;
                listener.roomid = aid;
                listener.key = data.getString("token");
                listener.playerActivity = this;

                liveWebSocket = okHttpClient.newWebSocket(request, listener);
//                okHttpClient.dispatcher().executorService().shutdown();
            } catch (Exception e) {
                MsgUtil.showMsg("直播弹幕连接失败");
                e.printStackTrace();
            }
        });
    }

    private void initUI(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;//获取屏宽
        screen_height = displayMetrics.heightPixels;//获取屏高

        if (SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn", true))
            btn_rotate.setVisibility(View.VISIBLE);
        else btn_rotate.setVisibility(View.GONE);

        screen_round = SharedPreferencesUtil.getBoolean("player_ui_round", false);
        if (screen_round) {
            int padding = (int) (screen_width * 0.03);

            LinearLayout.LayoutParams progressParams = (LinearLayout.LayoutParams) seekbar_progress.getLayoutParams();
            progressParams.leftMargin = padding*4;
            progressParams.rightMargin = padding*4;
            seekbar_progress.setLayoutParams(progressParams);

            text_online.setPadding(0,0,padding*3,0);
            text_progress.setPadding(padding*3,0,0,0);

            bottom_buttons.setPadding(padding, 0, padding, padding);

            right_control.setPadding(0, 0, padding, 0);

            RelativeLayout.LayoutParams danmakuParams = (RelativeLayout.LayoutParams) mDanmakuView.getLayoutParams();
            danmakuParams.setMargins(0,padding * 3,0,padding * 3);
            mDanmakuView.setLayoutParams(danmakuParams);

            text_subtitle.setMaxWidth((int) (screen_width * 0.65));

            layout_top.setPadding(padding*7, padding, padding*7, 0);

            LinearLayout clockLayout = findViewById(R.id.clock_layout);
            clockLayout.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams clockLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clockLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            clockLayout.setLayoutParams(clockLayoutParams);

            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleParams.addRule(RelativeLayout.BELOW, R.id.clock_layout);
            titleParams.topMargin = padding / 2;
            text_title.setLayoutParams(titleParams);
            text_title.setGravity(Gravity.CENTER);

            TextView textClock = findViewById(R.id.clock);
            LinearLayout.LayoutParams textClockParams = (LinearLayout.LayoutParams) textClock.getLayoutParams();
            textClockParams.leftMargin = padding / 2;
            textClockParams.topMargin = padding / 4;
            textClock.setLayoutParams(textClockParams);
        }

        if ((!SharedPreferencesUtil.getBoolean("player_show_online", false)) || aid==0 || cid==0)
            text_online.setVisibility(View.GONE);

        layout_top.setOnClickListener(view -> finish());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 19)) {
            textureView = new TextureView(this);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    Logu.v("surfacetexture","available");
                    mSurfaceTexture = surfaceTexture;
                    if(isPrepared && ijkPlayer!=null) ijkPlayer.setSurface(new Surface(surfaceTexture));
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    Logu.v("surfacetexture","sizechanged");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                    Logu.v("surfacetexture","destroyed");
                    mSurfaceTexture = null;
                    if(ijkPlayer != null) ijkPlayer.setSurface(null);
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {}
            });
            layout_video.addView(textureView, params);
        } else {
            surfaceView = new SurfaceView(this);
            layout_video.addView(surfaceView, params);
        }

        btn_rotate.setOnClickListener(view -> {
            Logu.v("点击旋转按钮");
            screen_landscape = !screen_landscape;
            if(SharedPreferencesUtil.getBoolean("dev_player_rotate_software",false)) softwareRotate();
            else setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        findViewById(R.id.button_sound_add).setOnClickListener(view -> changeVolume(true));
        findViewById(R.id.button_sound_cut).setOnClickListener(view -> changeVolume(false));

        btn_menu.setOnClickListener(view -> {
            if(menu_opened) {
                right_second.setVisibility(View.GONE);
                btn_menu.setImageResource(R.mipmap.morehide);
            }
            else {
                right_second.setVisibility(View.VISIBLE);
                btn_menu.setImageResource(R.mipmap.moreshow);
            }
            menu_opened = !menu_opened;
        });

        layout_card_bg.setOnClickListener(view -> {
            layout_card_bg.setVisibility(View.GONE);
            card_subtitle.setVisibility(View.GONE);
            card_danmaku_send.setVisibility(View.GONE);
        });
        btn_danmaku_send.setOnClickListener(view -> {
            layout_card_bg.setVisibility(View.VISIBLE);
            card_danmaku_send.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.danmaku_send).setOnClickListener(view1 -> {
            EditText editText = findViewById(R.id.danmaku_send_edit);
            if(editText.getText().toString().isEmpty()){
                MsgUtil.showMsg("不能发送空弹幕喵");
            } else {
                layout_card_bg.setVisibility(View.GONE);
                card_danmaku_send.setVisibility(View.GONE);

                CenterThreadPool.run(() -> {
                    try {
                        MsgUtil.showMsg("正在发送~");

                        int result = DanmakuApi.sendVideoDanmakuByAid(cid, editText.getText().toString(), aid, video_now, ToolsUtil.getRgb888(Color.WHITE), 1);

                        if(result == 0){
                            MsgUtil.showMsg("发送成功喵~");
                            runOnUiThread(() -> {
                                addDanmaku(editText.getText().toString(), Color.WHITE);
                                editText.setText("");
                            });
                        } else MsgUtil.showMsg("发送失败：" + result);
                    }catch (Exception e){
                        e.printStackTrace();
                        MsgUtil.err(e);
                    }
                });
            }
        });
    }

    private void initSeekbars(){
        seekbar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                runOnUiThread(() -> {
                    if (!isLiveMode)
                        text_progress.setText(ToolsUtil.toTime(position / 1000) + "/" + progress_str);
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                if (isPrepared && !destroyed) {
                    ijkPlayer.seekTo(seekbar_progress.getProgress());
                    autohideReset();
                }
            }
        });

        seekbar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if (fromUser) {
                    text_newspeed.setText(speed_strs[position]);
                    text_speed.setText(speed_strs[position]);
                    ijkPlayer.setSpeed(speed_values[position]);
                    mDanmakuView.setSpeed(speed_values[position]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (speedTimer != null) speedTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                speedTimer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> layout_speed.setVisibility(View.GONE));
                    }
                };
                speedTimer.schedule(timerTask, 200);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPrepared) switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                controlVideo();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ijkPlayer.seekTo(ijkPlayer.getCurrentPosition() - 10000L);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ijkPlayer.seekTo(ijkPlayer.getCurrentPosition() + 10000L);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                changeVolume(true);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                changeVolume(false);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean eventBusInit = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this);
            Logu.v("event","register");
            eventBusInit = true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SnackEvent event) {
        if (isFinishing()) return;
        Logu.v("event","onEvent");
        MsgUtil.toast(event.getMessage());  //由于Theme.Black不支持，只能这样用了
    }

    protected boolean eventBusEnabled() {
        return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true);
    }

    @Override
    public void finish() {
        if(isPlaying) playerPause();
        if(video_now!=0) {
            Intent result = new Intent();
            result.putExtra("progress", video_now);
            setResult(RESULT_OK, result);
        }
        else setResult(RESULT_CANCELED);
        super.finish();
    }
}