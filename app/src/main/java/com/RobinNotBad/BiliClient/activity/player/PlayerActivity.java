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
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.RobinNotBad.BiliClient.adapter.video.MediaEpisodeAdapter;
import com.RobinNotBad.BiliClient.api.DanmakuApi;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.event.SnackEvent;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.model.Subtitle;
import com.RobinNotBad.BiliClient.model.SubtitleLink;
import com.RobinNotBad.BiliClient.ui.widget.BatteryView;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

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
    private IjkMediaPlayer ijkPlayer;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture mSurfaceTexture;
    private boolean firstSurfaceHolder = true;

    private Subtitle[] subtitles = null;
    private int subtitle_curr_index, subtitle_count;

    private IDanmakuView mDanmakuView;
    private DanmakuContext mContext;

    private RelativeLayout control_layout, top_control, videoArea, card_bg;
    private LinearLayout bottom_control, layout_speed, right_control, right_second, loading_info, bottom_buttons;
    private LinearLayout card_subtitle, card_danmaku_send;

    private ImageView circle_loading;
    private ImageButton control_btn, danmaku_btn, loop_btn, rotate_btn, menu_btn, subtitle_btn, danmaku_send_btn;
    private SeekBar progressBar, speed_seekbar;
    private TextView text_progress, text_online, text_volume, loading_text0, loading_text1, text_speed, text_newspeed;
    public TextView text_title, text_subtitle;

    private Timer progressTimer, autoHideTimer, volumeTimer, speedTimer, loadingTimer, onlineTimer;
    private String video_url, danmaku_url;

    private boolean isPlaying, isPrepared, hasDanmaku,
            isOnlineVideo, isLiveMode, isSeeking, isDanmakuVisible;

    private int videoall, videonow, videonow_last;
    private long lastProgress;

    private float screen_width, screen_height;
    private int video_width, video_height;

    private AudioManager audioManager;

    private com.RobinNotBad.BiliClient.activity.player.ScaleGestureDetector scaleGestureDetector;
    private ViewScaleGestureListener scaleGestureListener;
    private float previousX, previousY;
    private boolean gesture_moved, gesture_scaling, gesture_scaled, click_disabled;
    private float video_origX, video_origY;
    private long click_timestamp;
    private boolean onLongClick = false;

    private final float[] speed_values = {0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 1.75F, 2.0F, 3.0F};
    private final String[] speed_strs = {"x 0.5", "x 0.75", "x 1.0", "x 1.25", "x 1.5", "x 1.75", "x 2.0", "x 3.0"};

    private boolean finishWatching = false;
    private boolean loop;

    private BatteryView batteryView;
    private BatteryManager manager;

    private File danmakuFile;

    private boolean screen_landscape = false;
    private boolean screen_round;

    public String online_number = "0";
    private String progress_all_str;

    private long aid, cid, mid;
    private String bvid;

    private boolean destroyed = false;

    private boolean menu_open = false;

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
        if(danmaku_url != null) Log.d("弹幕", danmaku_url);
        Log.d("视频", video_url);
        Log.d("标题", title);
        text_title.setText(title);

        bvid = intent.getStringExtra("bvid");
        aid = intent.getLongExtra("aid", 0);
        cid = intent.getLongExtra("cid", 0);
        mid = intent.getLongExtra("mid", 0);

        lastProgress = intent.getIntExtra("progress", 0);

        isLiveMode = intent.getBooleanExtra("live_mode", false);
        isOnlineVideo = video_url.contains("http");
        hasDanmaku = !danmaku_url.equals("");
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("加载", "加载");
        super.onCreate(savedInstanceState);

        screen_landscape = SharedPreferencesUtil.getBoolean("player_autolandscape", false) || SharedPreferencesUtil.getBoolean("ui_landscape", false);
        setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_player);
        findview();
        if(!getExtras()) {
            finish();
            return;
        }

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;//获取屏宽
        screen_height = displayMetrics.heightPixels;//获取屏高

        if (SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn", true))
            rotate_btn.setVisibility(View.VISIBLE);
        else rotate_btn.setVisibility(View.GONE);

        screen_round = SharedPreferencesUtil.getBoolean("player_ui_round", false);
        if (screen_round) {
            int padding = ToolsUtil.dp2px(8);

            LinearLayout.LayoutParams param_progress = (LinearLayout.LayoutParams) progressBar.getLayoutParams();
            param_progress.leftMargin = padding*4;
            param_progress.rightMargin = padding*4;
            progressBar.setLayoutParams(param_progress);

            text_online.setPadding(0,0,padding*3,0);
            text_progress.setPadding(padding*3,0,0,0);

            top_control.setPadding(padding*5, padding*2, padding*5, 0);
            text_title.setGravity(Gravity.CENTER);

            bottom_buttons.setPadding(padding, 0, padding, padding);

            right_control.setPadding(0, 0, padding, 0);

            findViewById(R.id.cl_1).setVisibility(View.GONE);
        }

        if ((!SharedPreferencesUtil.getBoolean("show_online", true)) || aid==0 || cid==0)
            text_online.setVisibility(View.GONE);

        IjkMediaPlayer.loadLibrariesOnce(null);

        ijkPlayer = new IjkMediaPlayer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        } else batteryView.setVisibility(View.GONE);

        loop = SharedPreferencesUtil.getBoolean("player_loop", false);
        Glide.with(this).load(R.mipmap.load).into(circle_loading);

        File cachepath = getCacheDir();
        if (!cachepath.exists()) cachepath.mkdirs();
        danmakuFile = new File(cachepath, "danmaku.xml");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setVideoGestures();
        autohide();


        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                runOnUiThread(() -> {
                    if (!isLiveMode)
                        text_progress.setText(ToolsUtil.toTime(position / 1000) + "/" + progress_all_str);
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
                if (autoHideTimer != null) autoHideTimer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPrepared) {
                    ijkPlayer.seekTo(progressBar.getProgress());
                    isSeeking = false;
                }
                autohide();
            }
        });

        speed_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        if(isLiveMode){
            control_btn.setVisibility(View.INVISIBLE); //暂停的话可能会出一些bug，那就别暂停了，卡住就退出重进吧（
            progressBar.setVisibility(View.GONE);
            progressBar.setEnabled(false);
            streamdanmaku(null); //用来初始化一下弹幕层
            danmuSocketConnect();
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> CenterThreadPool.run(() -> {    //等界面加载完成
            if(isLiveMode) {
                setDisplay();
                return;
            }

            runOnUiThread(() -> {
                loading_text0.setText("装填弹幕中");
                loading_text1.setText("(≧∇≦)");
            });
            if(isOnlineVideo) {
                downdanmu();
                if(!SharedPreferencesUtil.getBoolean("player_subtitle_autoshow", false)) downsubtitle(false);
            }
            else {
                runOnUiThread(() -> subtitle_btn.setVisibility(View.GONE));
                streamdanmaku(danmaku_url);
            }

            if(!destroyed) setDisplay();
        }), 60);
    }


    private void findview() {
        control_layout = findViewById(R.id.control_layout);
        top_control = findViewById(R.id.top);
        bottom_control = findViewById(R.id.bottom_control);
        right_control = findViewById(R.id.right_control);
        right_second = findViewById(R.id.right_second);
        card_bg = findViewById(R.id.card_bg);
        card_subtitle = findViewById(R.id.subtitle_card);
        card_danmaku_send = findViewById(R.id.danmaku_send_card);

        loading_info = findViewById(R.id.loading_info);

        circle_loading = findViewById(R.id.circle);
        text_progress = findViewById(R.id.text_progress);
        text_online = findViewById(R.id.text_online);
        danmaku_btn = findViewById(R.id.danmaku_btn);
        loop_btn = findViewById(R.id.loop_btn);
        rotate_btn = findViewById(R.id.rotate_btn);
        menu_btn = findViewById(R.id.menu_btn);
        danmaku_send_btn = findViewById(R.id.danmaku_send_btn);
        subtitle_btn = findViewById(R.id.subtitle_btn);
        control_btn = findViewById(R.id.button_video);
        progressBar = findViewById(R.id.videoprogress);
        loading_text0 = findViewById(R.id.loading_text0);
        loading_text1 = findViewById(R.id.loading_text1);
        text_title = findViewById(R.id.text_title);
        text_volume = findViewById(R.id.showsound);
        videoArea = findViewById(R.id.videoArea);
        mDanmakuView = findViewById(R.id.sv_danmaku);
        batteryView = findViewById(R.id.battery);

        text_speed = findViewById(R.id.text_speed);
        layout_speed = findViewById(R.id.layout_speed);
        speed_seekbar = findViewById(R.id.seekbar_speed);
        text_newspeed = findViewById(R.id.text_newspeed);
        bottom_buttons = findViewById(R.id.bottom_buttons);

        text_subtitle = findViewById(R.id.text_subtitle);

        top_control.setOnClickListener(view -> finish());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (SharedPreferencesUtil.getBoolean("player_display", BiliTerminal.getSystemSdk() < 19)) {
            textureView = new TextureView(this);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    mSurfaceTexture = surfaceTexture;
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                }
            });
            videoArea.addView(textureView, params);
        } else {
            surfaceView = new SurfaceView(this);
            videoArea.addView(surfaceView, params);
        }

        rotate_btn.setOnClickListener(view -> rotate());

        findViewById(R.id.button_sound_add).setOnClickListener(view -> changeVolume(true));
        findViewById(R.id.button_sound_cut).setOnClickListener(view -> changeVolume(false));

        menu_btn.setOnClickListener(view -> {
            if(menu_open) {
                right_second.setVisibility(View.GONE);
                menu_btn.setImageResource(R.mipmap.morehide);
            }
            else {
                right_second.setVisibility(View.VISIBLE);
                menu_btn.setImageResource(R.mipmap.moreshow);
            }
            menu_open = !menu_open;
        });

        card_bg.setOnClickListener(view -> {
            card_bg.setVisibility(View.GONE);
            card_subtitle.setVisibility(View.GONE);
            card_danmaku_send.setVisibility(View.GONE);
        });
        danmaku_send_btn.setOnClickListener(view -> {
            card_bg.setVisibility(View.VISIBLE);
            card_danmaku_send.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.danmaku_send).setOnClickListener(view1 -> {
            EditText editText = findViewById(R.id.danmaku_send_edit);
            if(editText.getText().toString().isEmpty()){
                MsgUtil.showMsg("不能发送空弹幕");
            } else {
                CenterThreadPool.run(() -> {
                    try {
                        int result = DanmakuApi.sendVideoDanmakuByAid(cid, editText.getText().toString(), aid, videonow, ToolsUtil.getRgb888(Color.WHITE), 1);

                        if(result == 0){
                            runOnUiThread(() -> {
                                adddanmaku(editText.getText().toString(), Color.WHITE);
                                card_bg.setVisibility(View.GONE);
                                card_danmaku_send.setVisibility(View.GONE);
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


    @SuppressLint("ClickableViewAccessibility")
    private void setVideoGestures() {
        if (SharedPreferencesUtil.getBoolean("player_scale", true)) {
            scaleGestureListener = new ViewScaleGestureListener(videoArea);
            scaleGestureDetector = new ScaleGestureDetector(this, scaleGestureListener);

            boolean doublemove_enabled = SharedPreferencesUtil.getBoolean("player_doublemove", true);  //是否启用双指移动

            control_layout.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                int pointerCount = event.getPointerCount();
                boolean singleTouch = pointerCount == 1;
                boolean doubleTouch = pointerCount == 2;

                Log.e("debug-gesture", event.getEventTime() + "");
                scaleGestureDetector.onTouchEvent(event);
                gesture_scaling = scaleGestureListener.scaling;

                if (!gesture_scaled && gesture_scaling) {
                    gesture_scaled = true;
                }

                //Log.e("debug-gesture", (scaling ? "scaled-yes" : "scaled-no"));

                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        if (singleTouch) {
                            if (gesture_scaling) {
                                videoMoveTo(videoArea.getX(), videoArea.getY());    //防止单指缩放出框
                            } else if (!(gesture_scaled && !doublemove_enabled)) {
                                float currentX = event.getX(0);  //单指移动
                                float currentY = event.getY(0);
                                float deltaX = currentX - previousX;
                                float deltaY = currentY - previousY;
                                if (deltaX != 0f || deltaY != 0f) {
                                    videoMoveTo(videoArea.getX() + deltaX, videoArea.getY() + deltaY);
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
                                videoMoveTo(videoArea.getX() + deltaX, videoArea.getY() + deltaY);
                                previousX = currentX;
                                previousY = currentY;
                            }
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:
                        if (singleTouch) {  //如果是单指按下，设置起始位置为当前手指位置
                            previousX = event.getX(0);
                            previousY = event.getY(0);
                            //Log.e("debug-gesture", "touch_start:" + previousX + "," + previousY);
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (doubleTouch) {  //如果是双指按下，设置起始位置为两指连线的中心点
                            previousX = (event.getX(0) + event.getX(1)) / 2;
                            previousY = (event.getY(0) + event.getY(1)) / 2;
                            //Log.e("debug-gesture","double_touch");
                            hidecon();
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (doubleTouch) {
                            int index = event.getActionIndex();  //actionIndex是抬起来的手指位置
                            previousX = event.getX((index == 0 ? 1 : 0));
                            previousY = event.getY((index == 0 ? 1 : 0));
                            //Log.e("debug-gesture","single_touch");
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        //Log.e("debug-gesture","touch_stop");
                        if (onLongClick) {
                            onLongClick = false;
                            ijkPlayer.setSpeed(speed_values[speed_seekbar.getProgress()]);
                            mDanmakuView.setSpeed(speed_values[speed_seekbar.getProgress()]);
                            text_speed.setText(speed_strs[speed_seekbar.getProgress()]);
                        }
                        if (gesture_moved) {
                            gesture_moved = false;
                        }
                        if (gesture_scaled) {
                            gesture_scaled = false;
                        }
                        break;
                }

                if (!click_disabled && (gesture_moved || gesture_scaled)) click_disabled = true;

                return false;
            });
        } else {
            control_layout.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && onLongClick) {
                    onLongClick = false;
                    ijkPlayer.setSpeed(speed_values[speed_seekbar.getProgress()]);
                    mDanmakuView.setSpeed(speed_values[speed_seekbar.getProgress()]);
                    text_speed.setText(speed_strs[speed_seekbar.getProgress()]);
                }
                return false;
            });
        }

        //这个管普通点击
        control_layout.setOnClickListener(view -> {
            if (click_disabled) click_disabled = false;
            else clickUI();
        });
        //这个管长按开始
        control_layout.setOnLongClickListener(view -> {
            if (SharedPreferencesUtil.getBoolean("player_longclick", true) && ijkPlayer != null && (isPlaying) && (!isLiveMode)) {
                if (!onLongClick && !gesture_moved && !gesture_scaled) {
                    hidecon();
                    ijkPlayer.setSpeed(3.0F);
                    mDanmakuView.setSpeed(3.0f);
                    text_speed.setText("x 3.0");
                    onLongClick = true;
                    Log.e("debug-gesture", "longclick_down");
                    return true;
                }
                return false;
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
                runOnUiThread(() -> hidecon());
                this.cancel();
            }
        };
        autoHideTimer.schedule(timerTask, 4000);
    }

    private void clickUI() {
        long now_timestamp = System.currentTimeMillis();
        if (now_timestamp - click_timestamp < 300) {
            if (SharedPreferencesUtil.getBoolean("player_scale", true) && scaleGestureListener.can_reset) {
                scaleGestureListener.can_reset = false;
                videoArea.setX(video_origX);
                videoArea.setY(video_origY);
                videoArea.setScaleX(1.0f);
                videoArea.setScaleY(1.0f);
            } else if (!isLiveMode) {
                if (isPlaying) playerPause();
                else playerResume();
                showcon();
            }
        } else {
            click_timestamp = now_timestamp;
            if ((top_control.getVisibility()) == View.GONE) showcon();
            else hidecon();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showcon() {
        right_control.setVisibility(View.VISIBLE);
        top_control.setVisibility(View.VISIBLE);
        bottom_buttons.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (isPrepared && !isLiveMode) text_speed.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView.setPower(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }
        if(screen_round) {
            text_progress.setGravity(Gravity.NO_GRAVITY);
            text_progress.setPadding(ToolsUtil.dp2px(24f),0,0,0);
            if(onlineTimer != null) text_online.setVisibility(View.VISIBLE);
        }

        autohide();
    }

    private void hidecon() {
        right_control.setVisibility(View.GONE);
        top_control.setVisibility(View.GONE);
        bottom_buttons.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        if (isPrepared) text_speed.setVisibility(View.GONE);
        if(screen_round) {
            text_progress.setGravity(Gravity.CENTER);
            text_progress.setPadding(0,0,0,ToolsUtil.dp2px(8f));
            if(onlineTimer != null) text_online.setVisibility(View.GONE);
        }
        if(menu_open) menu_btn.performClick();
    }


    private void setDisplay() {
        Log.e("debug", "创建播放器");
        Log.e("debug-url", video_url);


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
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 15 * 1000 * 1000);
        //这个坑死我！请允许我为解决此问题而大大地兴奋一下ohhhhhhhhhhhhhhhhhhhhhhhhhhhh
        //ijkplayer是自带一个useragent的，要把默认的改掉才能用！
        if (isOnlineVideo) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", USER_AGENT_WEB);
            Log.e("debug", "设置ua");
        }

        Log.e("debug", "准备设置显示");
        if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 19)) {            //Texture
            Log.e("debug", "使用texture模式");
            Timer textureTimer = new Timer();
            textureTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e("debug", "循环检测");
                    if (mSurfaceTexture != null) {
                        Surface surface = new Surface(mSurfaceTexture);
                        ijkPlayer.setSurface(surface);
                        MPPrepare(video_url);
                        Log.e("debug", "设置surfaceTexture成功！");
                        this.cancel();
                    }
                }
            }, 0, 200);
        } else {
            Log.e("debug", "使用surface模式");
            Log.e("debug", "获取surfaceHolder");
            SurfaceHolder surfaceHolder = surfaceView.getHolder();       //Surface
            Timer textureTimer = new Timer();
            textureTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e("debug", "循环检测");
                    if (!surfaceHolder.isCreating()) {
                        ijkPlayer.setDisplay(surfaceHolder);
                        firstSurfaceHolder = false;
                        Log.e("debug", "设置surfaceHolder成功！");
                    }
                    if (!firstSurfaceHolder) {
                        Log.e("debug", "添加callback");
                        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                                ijkPlayer.setDisplay(surfaceHolder);
                                if (isPrepared) {
                                    Log.e("debug", "重新设置Holder");
                                    ijkPlayer.seekTo(progressBar.getProgress());
                                }
                            }

                            @Override
                            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                            }

                            @Override
                            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                                Log.e("debug", "Holder没了");
                                ijkPlayer.setDisplay(null);
                            }
                        });
                        MPPrepare(video_url);
                        Log.e("debug", "定时器结束！");
                        this.cancel();
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
            if (loop) {
                ijkPlayer.seekTo(0);
                ijkPlayer.start();
            } else {
                isPlaying = false;
                if (hasDanmaku) mDanmakuView.pause();
                control_btn.setImageResource(R.drawable.btn_player_play);
            }
        });

        ijkPlayer.setOnErrorListener((iMediaPlayer, what, extra) -> {
            String EReport = "播放器可能遇到错误！\n错误码：" + what + "\n附加：" + extra;
            Log.e("ERROR", EReport);
            //Toast.makeText(PlayerActivity.this, EReport, Toast.LENGTH_LONG).show();
            return false;
        });

        ijkPlayer.setOnBufferingUpdateListener((mp, percent) -> progressBar.setSecondaryProgress(percent * videoall / 100));

        //if(mode==0)
        ijkPlayer.setOnInfoListener((mp, what, extra) -> {
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
        Log.e("debug", "开始准备");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPrepared(IMediaPlayer mediaPlayer) {
        isPrepared = true;
        videoall = (int) mediaPlayer.getDuration();

        changeVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());

        if(isLiveMode || hasDanmaku) mDanmakuView.start();
        if (SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn", true)) {
            isDanmakuVisible = !SharedPreferencesUtil.getBoolean("pref_switch_danmaku",true);
            //这里设置值是反的，因为下面直接调用监听器点击按钮
            danmaku_btn.setOnClickListener(view -> {
                if (isDanmakuVisible) mDanmakuView.hide();
                else mDanmakuView.show();
                danmaku_btn.setImageResource((isDanmakuVisible ? R.mipmap.danmakuoff : R.mipmap.danmakuon));
                isDanmakuVisible = !isDanmakuVisible;
                SharedPreferencesUtil.putBoolean("pref_switch_danmaku",isDanmakuVisible);
            });
            danmaku_btn.performClick();

            danmaku_btn.setVisibility(View.VISIBLE);
        } else danmaku_btn.setVisibility(View.GONE);
        //原作者居然把旋转按钮命名为danmaku_btn，也是没谁了...我改过来了  ----RobinNotBad
        //他大抵是觉得能用就行

        if (!isLiveMode) {
            if (loop) loop_btn.setImageResource(R.mipmap.loopon);
            else loop_btn.setImageResource(R.mipmap.loopoff);
            loop_btn.setOnClickListener(view -> {
                loop_btn.setImageResource((loop ? R.mipmap.loopoff : R.mipmap.loopon));
                loop = !loop;
            });
            loop_btn.setVisibility(View.VISIBLE);
        } else loop_btn.setVisibility(View.GONE);


        progressBar.setMax(videoall);
        progress_all_str = ToolsUtil.toTime(videoall / 1000);

        if (SharedPreferencesUtil.getBoolean("player_from_last", true) && !isLiveMode) {
            if (lastProgress > 6 && ((videoall / 1000) - lastProgress) > 6) { //阈值
                mediaPlayer.seekTo(lastProgress * 1000);
                runOnUiThread(() -> MsgUtil.showMsg("已从上次的位置播放"));
            }
        }

        loading_info.setVisibility(View.GONE);
        isPlaying = true;
        control_btn.setImageResource(R.drawable.btn_player_pause);

        text_speed.setVisibility(top_control.getVisibility());
        if (isLiveMode) text_speed.setVisibility(View.GONE);
        text_speed.setOnClickListener(view -> layout_speed.setVisibility(View.VISIBLE));
        layout_speed.setOnClickListener(view -> layout_speed.setVisibility(View.GONE));

        progressChange();
        onlineChange();

        mediaPlayer.start();

        control_btn.setOnClickListener(view -> controlVideo());
        subtitle_btn.setOnClickListener(view -> CenterThreadPool.run(() -> downsubtitle(true)));
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

    private void changeVideoSize(int width, int height) {
        Log.e("debug-改变视频区域大小", "开始");
        Log.e("debug-screen", screen_width + "x" + screen_height);
        Log.e("debug-video", width + "x" + height);

        if (SharedPreferencesUtil.getBoolean("player_ui_round", false)) {
            float video_mul = (float) height / (float) width;
            double sqrt = Math.sqrt(((double) screen_width * (double) screen_width) / ((((double) height * (double) height) / ((double) width * (double) width)) + 1));
            video_height = (int) (sqrt * video_mul + 0.5);
            video_width = (int) (sqrt + 0.5);
        } else {
            float multiplewidth = screen_width / width;
            float multipleheight = screen_height / height;
            int endhi1 = (int) (height * multipleheight);
            int endwi1 = (int) (width * multipleheight);
            int endhi2 = (int) (height * multiplewidth);
            int endwi2 = (int) (width * multiplewidth);//这句之前是multipleheight，其实算出来结果是不对的
            //但是这个bug神奇般的在原来的ui布局中无法显现出来，直到我换成ConstraintLayout。。。
            //越发对小电视作者心生敬意（
            Log.e("debug-case1", endwi1 + "x" + endhi1);
            Log.e("debug-case2", endwi2 + "x" + endhi2);
            if (endhi1 <= screen_height && endwi1 <= screen_width) {
                video_height = endhi1;
                video_width = endwi1;
                Log.e("debug-chosen", "case1");
            } else {
                video_height = endhi2;
                video_width = endwi2;
                Log.e("debug-chosen", "case2");
            }
        }

        runOnUiThread(() -> {
            videoArea.setLayoutParams(new RelativeLayout.LayoutParams(video_width, video_height));
            Log.e("debug-改变视频区域大小", video_width + "x" + video_height);
            video_origX = (screen_width - video_width) / 2;
            video_origY = (screen_height - video_height) / 2;

            videoArea.postDelayed(() -> {
                videoArea.setX(video_origX);
                videoArea.setY(video_origY);
                Log.e("debug-改变视频位置", ((screen_width - video_width) / 2) + "," + ((screen_height - video_height) / 2));
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
                    videonow = (int) ijkPlayer.getCurrentPosition();
                    if (videonow_last != videonow) {               //检测进度是否在变动
                        videonow_last = videonow;
                        float curr_sec = videonow / 1000f;
                        runOnUiThread(() -> {
                            if (isLiveMode) {
                                text_progress.setText(ToolsUtil.toTime((int) curr_sec));
                                text_online.setText(online_number);
                            }
                            else {
                                progressBar.setProgress(videonow);
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
        if(!SharedPreferencesUtil.getBoolean("show_online", true) || isLiveMode || aid==0 || cid==0) return;

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
        } catch (Exception e){
            MsgUtil.err(e);
        }
    }

    private void showSubtitle(float curr_sec) {
        Subtitle subtitle_curr = subtitles[subtitle_curr_index];

        boolean need_change = true;
        boolean need_show = true;

        while (need_change) {
            if (curr_sec < subtitle_curr.from) {
                if (subtitle_curr_index != 0 && curr_sec < subtitles[subtitle_curr_index - 1].to) subtitle_curr_index--;
                else {
                    need_change = false;
                    need_show = false;
                }
            } else if (curr_sec > subtitle_curr.to) {
                if (subtitle_curr_index+1 < subtitle_count && curr_sec > subtitles[subtitle_curr_index + 1].from) subtitle_curr_index++;
                else {
                    need_change = false;
                    need_show = false;
                }
            }
            else need_change = false;
        }

        if(need_show) runOnUiThread(()->{
            text_subtitle.setText(subtitles[subtitle_curr_index].content);
            text_subtitle.setVisibility(View.VISIBLE);
        });
        else runOnUiThread(()->text_subtitle.setVisibility(View.GONE));
    }

    private int select_subtitle_id = -1;
    private void downsubtitle(boolean msg){
        try {
            SubtitleLink[] subtitleLinks = PlayerApi.getSubtitleLink(aid, cid);

            if(subtitleLinks.length > 1 || (SharedPreferencesUtil.getBoolean("player_subtitle_ai_allowed", true) && subtitleLinks.length == 1 && subtitleLinks[0].isAI)) {
                ArrayList<Bangumi.Episode> episodeList = new ArrayList<>();
                for (int i = 0; i < subtitleLinks.length; i++) {
                    Bangumi.Episode episode = new Bangumi.Episode();
                    episode.id = i;
                    episode.title = subtitleLinks[i].lang;

                    episodeList.add(episode);

                    if(i == subtitleLinks.length - 1){
                        Bangumi.Episode episode2 = new Bangumi.Episode();
                        episode2.id = -1;
                        episode2.title = "不显示字幕";
                        episodeList.add(episode2);
                    }
                }
                if(select_subtitle_id == -1) select_subtitle_id = subtitleLinks.length;

                runOnUiThread(()->{
                    card_bg.setVisibility(View.VISIBLE);
                    card_subtitle.setVisibility(View.VISIBLE);
                    RecyclerView eposideRecyclerView = findViewById(R.id.subtitle_list);
                    MediaEpisodeAdapter adapter = new MediaEpisodeAdapter();
                    adapter.setData(episodeList);
                    adapter.setSelectedItemIndex(select_subtitle_id);
                    adapter.setOnItemClickListener(index -> {
                        card_bg.setVisibility(View.GONE);
                        card_subtitle.setVisibility(View.GONE);
                        select_subtitle_id = index;
                        
                        if(episodeList.get(index).id == -1) 
                            subtitles = null;
                        else 
                            CenterThreadPool.run(() -> getSubtitle(subtitleLinks[(int) episodeList.get(index).id].url));
                    });
                    eposideRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    eposideRecyclerView.setAdapter(adapter);
                });
            }
            else if(msg) MsgUtil.showMsg("无字幕可供选择");
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
            streamdanmaku(danmakuFile.toString());
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

    private void streamdanmaku(String danmakuPath) {
        Log.e("debug", "streamdanmaku");

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
                Log.e("debug","danmaku_prepared");
                adddanmaku("弹幕君准备完毕～(*≧ω≦)", Color.WHITE);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                if(isPlaying) timer.update(ijkPlayer.getCurrentPosition()); //实时同步弹幕和播放器时间
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

    public void adddanmaku(String text, int color) {
        adddanmaku(text, color, 25, 1, 0);
    }

    public void adddanmaku(String text, int color, int textSize, int type, int backgroundColor) {
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
            if (autoHideTimer != null) autoHideTimer.cancel();
        } else {
            isPlaying = true;
            // 因为弹幕实时同步，不需要自行设置弹幕时间了
            if (videonow >= videoall - 250) {     //别问为啥有个>=，问就是这TM都能有误差，视频停止时并不是播放到最后一帧,可以多或者少出来几十甚至上百个毫秒...  ----RobinNotBad
                ijkPlayer.seekTo(0);
                // mDanmakuView.seekTo(0L);
                mDanmakuView.resume();
                Log.e("debug", "播完重播");
            } // else mDanmakuView.start(ijkPlayer.getCurrentPosition());
            ijkPlayer.start();
            control_btn.setImageResource(R.drawable.btn_player_pause);
        }
        autohide();
    }

    @SuppressLint("SetTextI18n")
    public void changeVolume(Boolean add_or_cut) {
        if (autoHideTimer != null) autoHideTimer.cancel();
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

        hideVolume();
        autohide();
    }

    private void hideVolume() {
        if (volumeTimer != null) volumeTimer.cancel();
        volumeTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> text_volume.setVisibility(View.GONE));
            }
        };
        volumeTimer.schedule(timerTask, 3000);
    }

    public void rotate() {
        Log.e("debug", "点击旋转按钮");
        screen_landscape = !screen_landscape;
        setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("debug", "开始旋转屏幕");

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;//获取屏宽
        screen_height = displayMetrics.heightPixels;//获取屏高
        if (isPrepared) {
            changeVideoSize(ijkPlayer.getVideoWidth(), ijkPlayer.getVideoHeight());
        }

        Log.e("debug", "旋转屏幕结束");
    }

    private void videoMoveTo(float x, float y) {
        float width_delta = 0.5f * video_width * (videoArea.getScaleX() - 1f);
        float height_delta = 0.5f * video_height * (videoArea.getScaleY() - 1f);
        float video_x_min = video_origX - width_delta;
        float video_x_max = video_origX + width_delta;
        float video_y_min = video_origY - height_delta;
        float video_y_max = video_origY + height_delta;

        if (x < video_x_min) x = video_x_min;
        if (x > video_x_max) x = video_x_max;
        if (y < video_y_min) y = video_y_min;
        if (y > video_y_max) y = video_y_max;

        if (videoArea.getX() != x || videoArea.getY() != y) {
            //Log.e("debug-gesture","moveto:" + x + "," + y);
            videoArea.setX(x);
            videoArea.setY(y);
            if (!gesture_moved) {
                gesture_moved = true;
                hidecon();
            }
        }
    }

    private void playerPause() {
        isPlaying = false;
        if (ijkPlayer != null && isPrepared) ijkPlayer.pause();
        if (hasDanmaku) mDanmakuView.pause();
        if (control_btn != null) control_btn.setImageResource(R.drawable.btn_player_play);
    }

    private void playerResume() {
        isPlaying = true;
        if (ijkPlayer != null && isPrepared) {
            ijkPlayer.start();
            if (hasDanmaku) mDanmakuView.start(ijkPlayer.getCurrentPosition());
        }
        if (control_btn != null) control_btn.setImageResource(R.drawable.btn_player_pause);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("debug", "onNewIntent");
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("debug", "onPause");
        if (!SharedPreferencesUtil.getBoolean("player_background", false)) {
            playerPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("debug", "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("debug", "onStop");
    }


    WebSocket liveWebSocket = null;

    @Override
    protected void onDestroy() {
        Log.e("debug", "结束");
        if (eventBusInit) {
            EventBus.getDefault().unregister(this);
            eventBusInit = false;
        }
        destroyed = true;
        if (isPlaying) playerPause();
        if (ijkPlayer != null) ijkPlayer.release();
        if (mDanmakuView != null) mDanmakuView.release();

        if (autoHideTimer != null) autoHideTimer.cancel();
        if (volumeTimer != null) volumeTimer.cancel();
        if (progressTimer != null) progressTimer.cancel();
        if (onlineTimer != null) onlineTimer.cancel();
        if (loadingTimer != null) loadingTimer.cancel();

        if (danmakuFile != null && danmakuFile.exists()) danmakuFile.delete();

        CenterThreadPool.run(() -> {
            try {
                if (mid != 0 && aid != 0 && !isLiveMode) {
                    HistoryApi.reportHistory(aid, cid, mid, videonow / 1000);
                }
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.err(e));
            }
        });

        if (liveWebSocket != null) liveWebSocket.close(1000, "");

        setRequestedOrientation(SharedPreferencesUtil.getBoolean("ui_landscape", false) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onDestroy();
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
                Log.e("debug", "连接WebSocket：" + url);

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
            Log.d("debug-event","register");
            eventBusInit = true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SnackEvent event) {
        if (isFinishing()) return;
        Log.d("debug-event","onEvent");
        MsgUtil.toast(event.getMessage());  //由于Theme.Black不支持，只能这样用了
    }

    protected boolean eventBusEnabled() {
        return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true);
    }
}