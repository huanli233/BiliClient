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
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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
import com.RobinNotBad.BiliClient.activity.InteractionDebugActivity;
import com.RobinNotBad.BiliClient.adapter.QualitySelectorAdapter;
import com.RobinNotBad.BiliClient.adapter.ViewPointAdapter;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.DanmakuApi;
import com.RobinNotBad.BiliClient.api.InteractionVideoApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.event.SnackEvent;
import com.RobinNotBad.BiliClient.model.DmSegMobileReply;
import com.RobinNotBad.BiliClient.model.HighEnergyData;
import com.RobinNotBad.BiliClient.model.InteractionVideoData;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.model.Subtitle;
import com.RobinNotBad.BiliClient.model.SubtitleLink;
import com.RobinNotBad.BiliClient.model.ViewPoint;
import com.RobinNotBad.BiliClient.ui.widget.BatteryView;
import com.RobinNotBad.BiliClient.ui.widget.HighEnergyProgressBar;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.StringUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import master.flame.danmaku.danmaku.parser.android.BiliProtobufDanmakuParser;
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
    private float subtitle_delta;

    private RelativeLayout layout_control, layout_top, layout_video, layout_card_bg, layout_audio_only;
    private LinearLayout layout_speed, right_control, loading_info;
    private RelativeLayout bottom_buttons;
    private HorizontalScrollView right_second;
    private LinearLayout card_subtitle, card_danmaku_send, card_page_selector, card_quality_selector, card_viewpoint_selector;

    private ImageView img_loading;
    private AnimationDrawable anim_loading;
    private ImageButton btn_control, btn_danmaku, btn_loop, btn_rotate, btn_menu, btn_subtitle, btn_danmaku_send,
            btn_audio_only, btn_page_selector, btn_auto_next, btn_quality, btn_viewpoint, btn_debug;
    private HighEnergyProgressBar seekbar_progress;
    private SeekBar seekbar_speed;
    private TextView text_progress, text_online, text_volume, loading_text0, loading_text1, text_speed, text_newspeed;
    public TextView text_title, text_subtitle, text_audio_title, text_audio_subtitle;

    private Timer progressTimer, speedTimer, loadingTimer, onlineTimer, surfaceTimer;
    private Handler mainHandler;
    private Runnable danmakuSyncRunnable;
    private String video_url, danmaku_url;
    private MediaSession mediaSession;

    private boolean isPlaying, isPrepared, hasDanmaku,
            isOnlineVideo, isLiveMode, isSeeking, isDanmakuVisible;
    private boolean menu_opened = false;
    private boolean isAudioOnlyMode = false;
    private boolean isLocalAudioFile = false; // 标记是否为本地音频文件

    private int video_all, video_now, video_now_last;
    private long progress_history;
    private String progress_str;

    private int screen_width, screen_height;
    private int video_width, video_height;

    private AudioManager audioManager;

    private ScaleGestureDetector scaleGestureDetector;
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
    private boolean auto_next_enabled = false;

    private BatteryView batteryView;
    private BatteryManager batteryManager;

    private File danmakuFile;

    private boolean screen_landscape, screen_round;

    public String online_number = "0";

    private long aid, cid, mid;

    private ArrayList<String> pagenames;
    private ArrayList<Long> cids;
    private int currentPageIndex = 0;
    private String videoTitle;

    private String[] qnStrList;
    private int[] qnValueList;
    private int currentQuality = 0;

    private InteractionVideoData interactionData;
    private long interactionGraphVersion = 0;
    private long currentEdgeId = 0;
    private long initialEdgeId = 0;
    private InteractionVideoData.InteractionQuestion currentQuestion = null;
    private boolean questionShown = false;
    private LinearLayout interactionChoiceLayout;

    @Override
    public void onBackPressed() {
        if (!SharedPreferencesUtil.getBoolean("back_disable", false))
            super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }

    private boolean getExtras() {
        Intent intent = getIntent();
        if (intent == null)
            return false;

        video_url = intent.getStringExtra("url");
        danmaku_url = intent.getStringExtra("danmaku");
        String title = intent.getStringExtra("title");

        if (video_url == null)
            return false;
        if (danmaku_url != null)
            Logu.v("弹幕", danmaku_url);
        Logu.v("视频", video_url);
        Logu.v("标题", title);
        text_title.setText(title);
        videoTitle = title;

        aid = intent.getLongExtra("aid", 0);
        cid = intent.getLongExtra("cid", 0);
        mid = intent.getLongExtra("mid", 0);

        progress_history = intent.getIntExtra("progress", 0);
        Logu.d("history", String.valueOf(progress_history));

        isLiveMode = intent.getBooleanExtra("live_mode", false);
        isOnlineVideo = video_url.contains("http");
        hasDanmaku = !danmaku_url.equals("");

        if (intent.hasExtra("pagenames") && intent.hasExtra("cids")) {
            pagenames = intent.getStringArrayListExtra("pagenames");
            ArrayList<Long> cidList = new ArrayList<>();
            long[] cidArray = intent.getLongArrayExtra("cids");
            if (cidArray != null) {
                for (long c : cidArray) {
                    cidList.add(c);
                }
            }
            cids = cidList;
            currentPageIndex = intent.getIntExtra("currentPageIndex", 0);
        }

        initialEdgeId = intent.getLongExtra("edgeId", 0);
        if (initialEdgeId > 0) {
            currentEdgeId = initialEdgeId;
        }

        if (intent.hasExtra("qnStrList") && intent.hasExtra("qnValueList")) {
            qnStrList = intent.getStringArrayExtra("qnStrList");
            qnValueList = intent.getIntArrayExtra("qnValueList");
            currentQuality = intent.getIntExtra("currentQuality", SharedPreferencesUtil.getInt("play_qn", 16));
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logu.v("加载", "加载");
        super.onCreate(savedInstanceState);

        screen_landscape = SharedPreferencesUtil.getBoolean("player_autolandscape", false)
                || SharedPreferencesUtil.getBoolean("ui_landscape", false);
        if (SharedPreferencesUtil.getBoolean("dev_player_rotate_software", false) && screen_landscape) {
            MsgUtil.showMsg("不支持默认横屏！");
            screen_landscape = false;
        } else {
            setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_player);
        findview();
        if (!getExtras()) {
            finish();
            return;
        }

        initUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.PLAYER_MEDIA_SESSION_ENABLE, false)) {
            initMediaSession();
        }

        IjkMediaPlayer.loadLibrariesOnce(null);

        ijkPlayer = new IjkMediaPlayer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            batteryView.setPower(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        } else
            batteryView.setVisibility(View.GONE);

        loop_enabled = SharedPreferencesUtil.getBoolean("player_loop", false);
        // 从设置读取听视频模式的默认值
        isAudioOnlyMode = SharedPreferencesUtil.getBoolean("player_audio_only", false);
        // 从Intent读取是否为仅音频模式（用于播放本地音频文件）
        isLocalAudioFile = getIntent().getBooleanExtra("audio_only", false);
        if (isLocalAudioFile) {
            isAudioOnlyMode = true;
        }
        img_loading.setImageResource(R.drawable.loading_tv_shaking);
        anim_loading = (AnimationDrawable) img_loading.getDrawable();
        anim_loading.start();

        File cachepath = getCacheDir();
        if (!cachepath.exists())
            cachepath.mkdirs();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());

        setVideoGestures();
        autohideReset();

        initSeekbars();

        if (isLiveMode) {
            btn_control.setVisibility(View.GONE); // 直播模式隐藏暂停按钮，使用GONE而不是INVISIBLE以保持UI布局正确
            seekbar_progress.setVisibility(View.GONE);
            seekbar_progress.setEnabled(false);
            streamDanmaku(null); // 用来初始化一下弹幕层
            // danmuSocketConnect();
            // 先把弹幕连接注释掉
        }

        layout_control.postDelayed(() -> CenterThreadPool.run(() -> { // 等界面加载完成
            if (isLiveMode) {
                runOnUiThread(() -> {
                    btn_menu.setVisibility(View.GONE);
                    // 直播模式隐藏清晰度按钮
                    btn_quality.setVisibility(View.GONE);
                    // 直播模式隐藏循环按钮
                    btn_loop.setVisibility(View.GONE);
                    // 直播模式隐藏听视频模式按钮
                    btn_audio_only.setVisibility(View.GONE);
                    // 直播模式隐藏自动下一个按钮
                    btn_auto_next.setVisibility(View.GONE);
                    // 直播模式隐藏分P选择器按钮
                    btn_page_selector.setVisibility(View.GONE);
                });
                setDisplay();
                return;
            }

            runOnUiThread(() -> {
                loading_text0.setText("装填弹幕中");
                loading_text1.setText("(≧∇≦)");
            });
            if (isOnlineVideo) {
                danmakuFile = new File(cachepath, "danmaku.xml");
                downdanmu();
            } else {
                runOnUiThread(() -> btn_danmaku_send.setVisibility(View.GONE));
                danmakuFile = new File(danmaku_url);
                if (danmakuFile.exists())
                    streamDanmaku(danmakuFile.toString());
                else
                    hasDanmaku = false;
            }

            if (!destroyed && SharedPreferencesUtil.getBoolean("player_subtitle_autoshow", true))
                downSubtitle(false);

            // 加载高能进度条数据
            if (!destroyed && isOnlineVideo && aid > 0 && cid > 0) {
                loadHighEnergyData();
            }

            if (!destroyed && isOnlineVideo && aid > 0 && cid > 0 && SharedPreferencesUtil.getBoolean("player_show_viewpoints", false)) {
                loadViewPoints();
            }

            if (!destroyed && isOnlineVideo && aid > 0 && cid > 0) {
                loadInteractionVideo();
            }

            if (!destroyed)
                setDisplay();
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
        card_page_selector = findViewById(R.id.page_selector_card);
        card_quality_selector = findViewById(R.id.quality_selector_card);
        card_viewpoint_selector = findViewById(R.id.viewpoint_selector_card);
        layout_audio_only = findViewById(R.id.audio_only_layout);

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
        btn_audio_only = findViewById(R.id.audio_only_btn);
        btn_control = findViewById(R.id.button_video);
        btn_page_selector = findViewById(R.id.button_page_selector);
        btn_auto_next = findViewById(R.id.auto_next_btn);
        btn_quality = findViewById(R.id.button_quality);
        btn_viewpoint = findViewById(R.id.viewpoint_btn);
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
        btn_debug = findViewById(R.id.btn_debug);

        text_subtitle = findViewById(R.id.text_subtitle);
        text_audio_title = findViewById(R.id.audio_title);
        text_audio_subtitle = findViewById(R.id.audio_subtitle);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setVideoGestures() {
        if (SharedPreferencesUtil.getBoolean("player_scale", true)) {
            scaleGestureListener = new ViewScaleGestureListener(layout_video);
            scaleGestureDetector = new ScaleGestureDetector(this, scaleGestureListener);

            boolean doublemove_enabled = SharedPreferencesUtil.getBoolean("player_doublemove", true); // 是否启用双指移动

            layout_control.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                int pointerCount = event.getPointerCount();
                boolean singleTouch = pointerCount == 1;
                boolean doubleTouch = pointerCount == 2;

                // Logu.v("gesture", event.getEventTime() + "");
                scaleGestureDetector.onTouchEvent(event);
                boolean gesture_scaling = scaleGestureListener.scaling;

                if (!gesture_scaled && gesture_scaling)
                    gesture_scaled = true;

                // Logu.v("gesture", (scaling ? "scaled-yes" : "scaled-no"));

                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        if (singleTouch) {
                            if (gesture_scaling) {
                                videoMoveBy(0, 0); // 防止单指缩放出框
                            } else if (!(gesture_scaled && !doublemove_enabled)) {
                                float currentX = event.getX(0); // 单指移动
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
                        if (singleTouch) { // 如果是单指按下，设置起始位置为当前手指位置
                            previousX = event.getX(0);
                            previousY = event.getY(0);
                            // Logu.v("gesture", "touch_start:" + previousX + "," + previousY);
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (doubleTouch) { // 如果是双指按下，设置起始位置为两指连线的中心点
                            previousX = (event.getX(0) + event.getX(1)) / 2;
                            previousY = (event.getY(0) + event.getY(1)) / 2;
                            // Logu.v("gesture","double_touch");
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        if (doubleTouch) {
                            int index = event.getActionIndex(); // actionIndex是抬起来的手指位置
                            previousX = event.getX((index == 0 ? 1 : 0));
                            previousY = event.getY((index == 0 ? 1 : 0));
                            // Logu.v("gesture","single_touch");
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (onLongClick) {
                            onLongClick = false;
                            float normalSpeed = speed_values[seekbar_speed.getProgress()];
                            if (ijkPlayer != null)
                                ijkPlayer.setSpeed(normalSpeed);
                            if (mDanmakuView != null)
                                mDanmakuView.setSpeed(normalSpeed);
                            text_speed.setText(speed_strs[seekbar_speed.getProgress()]);
                        }
                        if (gesture_moved)
                            gesture_moved = false;
                        if (gesture_scaled)
                            gesture_scaled = false;
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
                    float normalSpeed = speed_values[seekbar_speed.getProgress()];
                    if (ijkPlayer != null)
                        ijkPlayer.setSpeed(normalSpeed);
                    if (mDanmakuView != null)
                        mDanmakuView.setSpeed(normalSpeed);
                    text_speed.setText(speed_strs[seekbar_speed.getProgress()]);
                }
                return false;
            });
        }

        // 这个管普通点击
        layout_control.setOnClickListener(view -> {
            if (gesture_click_disabled)
                gesture_click_disabled = false;
            else
                clickUI();
        });
        // 这个管长按开始
        layout_control.setOnLongClickListener(view -> {
            if (SharedPreferencesUtil.getBoolean("player_longclick", true) && ijkPlayer != null && isPlaying
                    && !isLiveMode) {
                if (!onLongClick && !gesture_click_disabled) {
                    hidecon.run();
                    if (ijkPlayer != null)
                        ijkPlayer.setSpeed(3.0F);
                    if (mDanmakuView != null)
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
        layout_control.postDelayed(hidecon, 4000);
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
                if (isPlaying)
                    playerPause();
                else
                    playerResume();
                showcon();
            }
        } else {
            timestamp_click = now_timestamp;
            if ((layout_top.getVisibility()) == View.GONE)
                showcon();
            else
                hidecon.run();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showcon() {
        right_control.setVisibility(View.VISIBLE);
        layout_top.setVisibility(View.VISIBLE);
        bottom_buttons.setVisibility(View.VISIBLE);
        seekbar_progress.setVisibility(View.VISIBLE);
        seekbar_progress.setEnabled(false);
        seekbar_progress.postDelayed(progressbarEnable, 200);
        if (isPrepared && (!isLiveMode) && (!isAudioOnlyMode)) {
            text_speed.setVisibility(View.VISIBLE);
            updateDebugButtonVisibility();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryView.setPower(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }
        if (screen_round) {
            text_progress.setGravity(Gravity.NO_GRAVITY);
            text_progress.setPadding(ToolsUtil.dp2px(24f), 0, 0, 0);
            if (onlineTimer != null)
                text_online.setVisibility(View.VISIBLE);
        }

        autohideReset();
    }

    private final Runnable progressbarEnable = () -> seekbar_progress.setEnabled(true);

    private final Runnable hidecon = () -> {
        right_control.setVisibility(View.GONE);
        layout_top.setVisibility(View.GONE);
        bottom_buttons.setVisibility(View.GONE);
        seekbar_progress.setVisibility(View.GONE);
        if (isPrepared && (!isAudioOnlyMode)) {
            text_speed.setVisibility(View.GONE);
            btn_debug.setVisibility(View.GONE);
        }
        if (screen_round) {
            text_progress.setGravity(Gravity.CENTER);
            text_progress.setPadding(0, 0, 0, ToolsUtil.dp2px(8f));
            if (onlineTimer != null)
                text_online.setVisibility(View.GONE);
        }
        if (menu_opened)
            btn_menu.performClick();
    };

    private void setDisplay() {
        Logu.v("创建播放器");
        Logu.v("url", video_url);

        runOnUiThread(() -> loading_text0.setText("初始化播放"));

        if (isAudioOnlyMode) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vn", 1); // 禁用视频
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 48); // 跳过所有视频帧
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        } else {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",
                    (SharedPreferencesUtil.getBoolean("player_codec", true) ? 1 : 0));
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        }

        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles",
                (SharedPreferencesUtil.getBoolean("player_audio", false) ? 1 : 0));
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 4);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);

        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "flush_packets");
        ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);

        // 这个坑死我！请允许我为解决此问题而大大地兴奋一下ohhhhhhhhhhhhhhhhhhhhhhhhhhhh
        // ijkplayer是自带一个useragent的，要把默认的改掉才能用！
        if (isOnlineVideo) {
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 15 * 1024 * 1024);
            ijkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", USER_AGENT_WEB);
            Logu.v("设置ua");
        }

        Logu.v("准备设置显示");
        if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 26)) { // Texture
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
        } else {
            Logu.v("使用surface模式");
            SurfaceHolder surfaceHolder = surfaceView.getHolder(); // Surface
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
                                if (!destroyed) {
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
                                if (isPrepared && !destroyed)
                                    ijkPlayer.setDisplay(null);
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

        if (isLiveMode) {
            runOnUiThread(() -> loading_text0.setText("载入直播中"));
            danmuSocketConnect();
        } else
            runOnUiThread(() -> loading_text0.setText("载入视频中"));
        try {
            if (isOnlineVideo) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Referer", "https://www.bilibili.com/");
                headers.put("Cookie", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
                ijkPlayer.setDataSource(nowurl, headers);
            } else
                ijkPlayer.setDataSource(nowurl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ijkPlayer.setOnCompletionListener(iMediaPlayer -> {
            finishWatching = true;
            
            if (interactionData != null && interactionData.edges != null && 
                interactionData.edges.questions != null && !questionShown) {
                checkEndInteractionQuestions();
                if (questionShown) {
                    isPlaying = false;
                    if (hasDanmaku && mDanmakuView != null) {
                        mDanmakuView.pause();
                    }
                    btn_control.setImageResource(R.drawable.btn_player_play);
                    return;
                }
            }
            
            if (loop_enabled) {
                ijkPlayer.seekTo(0);
                if (hasDanmaku && mDanmakuView != null) {
                    mDanmakuView.seekTo(0L);
                }
                ijkPlayer.start();
            } else if (auto_next_enabled && hasMultiplePages() && currentPageIndex < pagenames.size() - 1) {
                switchToPage(currentPageIndex + 1);
            } else {
                isPlaying = false;
                if (hasDanmaku && mDanmakuView != null) {
                    mDanmakuView.pause();
                }
                btn_control.setImageResource(R.drawable.btn_player_play);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
                    updateMediaSessionPlaybackState();
                }
            }
        });

        ijkPlayer.setOnErrorListener((iMediaPlayer, what, extra) -> {
            String EReport = "播放器可能遇到错误！\n错误码：" + what + "\n附加：" + extra;
            Logu.e("ijk-err", EReport);
            // Toast.makeText(PlayerActivity.this, EReport, Toast.LENGTH_LONG).show();
            return false;
        });

        ijkPlayer.setOnBufferingUpdateListener(
                (mp, percent) -> seekbar_progress.setSecondaryProgress(percent * video_all / 100));

        if (isOnlineVideo || isLiveMode)
            ijkPlayer.setOnInfoListener((mp, what, extra) -> {
                if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    runOnUiThread(() -> {
                        loading_info.setVisibility(View.VISIBLE);
                        anim_loading.start();
                        loading_text0.setText("正在缓冲");
                        showLoadingSpeed();
                        if (hasDanmaku && mDanmakuView != null && isPlaying) {
                            mDanmakuView.pause();
                        }
                    });
                } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    runOnUiThread(() -> {
                        if (loadingTimer != null)
                            loadingTimer.cancel();
                        loading_info.setVisibility(View.GONE);
                        anim_loading.stop();
                        if (hasDanmaku && mDanmakuView != null && isPlaying) {
                            mDanmakuView.resume();
                        }
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
        if (destroyed) {
            ijkPlayer.release();
            return;
        }

        isPrepared = true;
        video_all = (int) ijkPlayer.getDuration();

        changeVideoSize();

        if ((isLiveMode || hasDanmaku) && mDanmakuView != null) {
            mDanmakuView.start();
        }
        if (SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn", true)) {
            isDanmakuVisible = !SharedPreferencesUtil.getBoolean("pref_switch_danmaku", true);
            btn_danmaku.setOnClickListener(view -> {
                if (mDanmakuView == null)
                    return;
                if (isDanmakuVisible) {
                    mDanmakuView.hide();
                } else {
                    mDanmakuView.show();
                }
                btn_danmaku.setImageResource((isDanmakuVisible ? R.mipmap.danmakuoff : R.mipmap.danmakuon));
                isDanmakuVisible = !isDanmakuVisible;
                SharedPreferencesUtil.putBoolean("pref_switch_danmaku", isDanmakuVisible);
            });
            btn_danmaku.performClick();

            btn_danmaku.setVisibility(View.VISIBLE);
        } else
            btn_danmaku.setVisibility(View.GONE);
        // 原作者居然把旋转按钮命名为danmaku_btn，也是没谁了...我改过来了 ----RobinNotBad
        // 他大抵是觉得能用就行

        if (!isLiveMode) {
            if (loop_enabled)
                btn_loop.setImageResource(R.mipmap.loopon);
            else
                btn_loop.setImageResource(R.mipmap.loopoff);
            btn_loop.setOnClickListener(view -> {
                btn_loop.setImageResource((loop_enabled ? R.mipmap.loopoff : R.mipmap.loopon));
                loop_enabled = !loop_enabled;
            });
            btn_loop.setVisibility(View.VISIBLE);

            // 听视频模式按钮
            // 如果是本地音频文件，隐藏听视频开关（因为已经是纯音频了）
            if (isLocalAudioFile) {
                btn_audio_only.setVisibility(View.GONE);
            } else {
                updateAudioOnlyButton();
                btn_audio_only.setOnClickListener(view -> toggleAudioOnlyMode());
                btn_audio_only.setVisibility(View.VISIBLE);
            }

            if (hasMultiplePages()) {
                btn_page_selector.setVisibility(View.VISIBLE);
                btn_page_selector.setOnClickListener(view -> showPageSelectorCard());
                btn_auto_next.setVisibility(View.VISIBLE);
                updateAutoNextButton();
                btn_auto_next.setOnClickListener(view -> toggleAutoNext());
            } else {
                btn_page_selector.setVisibility(View.GONE);
                btn_auto_next.setVisibility(View.GONE);
            }

            if (SharedPreferencesUtil.getBoolean("player_ui_showQualityBtn", true) && isOnlineVideo) {
                btn_quality.setVisibility(View.VISIBLE);
                btn_quality.setOnClickListener(view -> showQualitySelectorCard());
            } else {
                btn_quality.setVisibility(View.GONE);
            }

            if (!SharedPreferencesUtil.getBoolean("player_ui_showPageBtn", true))
                btn_page_selector.setVisibility(View.GONE);
        } else {
            // 直播模式下隐藏这些按钮
            btn_loop.setVisibility(View.GONE);
            btn_audio_only.setVisibility(View.GONE);
            btn_page_selector.setVisibility(View.GONE);
            btn_auto_next.setVisibility(View.GONE);
            btn_quality.setVisibility(View.GONE);
        }

        seekbar_progress.setMax(video_all);
        progress_str = StringUtil.toTime(video_all / 1000);

        if (isAudioOnlyMode) {
            updateAudioOnlyUI();
        }

        if (SharedPreferencesUtil.getBoolean("player_from_last", true) && !isLiveMode) {
            if (progress_history > 5) {
                ijkPlayer.seekTo(progress_history);
                if (hasDanmaku && mDanmakuView != null) {
                    mDanmakuView.seekTo(progress_history);
                }
                Logu.d("进度跳转", String.valueOf(progress_history));
                runOnUiThread(() -> MsgUtil.showMsg("已从上次的位置播放"));
            }
        }

        loading_info.setVisibility(View.GONE);
        anim_loading.stop();
        isPlaying = true;
        btn_control.setImageResource(R.drawable.btn_player_pause);

        text_speed.setVisibility(layout_top.getVisibility());
        if (isLiveMode)
            text_speed.setVisibility(View.GONE);
        text_speed.setOnClickListener(view -> layout_speed.setVisibility(View.VISIBLE));
        layout_speed.setOnClickListener(view -> layout_speed.setVisibility(View.GONE));

        btn_debug.setOnClickListener(view -> showInteractionDebugDialog());
        updateDebugButtonVisibility();

        progressChange();
        onlineChange();

        ijkPlayer.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
            updateMediaSessionMetadata();
            updateMediaSessionPlaybackState();
        }

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
        if (!isPrepared || ijkPlayer == null)
            return;
        int width = ijkPlayer.getVideoWidth();
        int height = ijkPlayer.getVideoHeight();
        Logu.v("screen", screen_width + "x" + screen_height);
        Logu.v("video", width + "x" + height);

        // 在听视频模式下，视频宽高可能为0，跳过尺寸调整
        if (width == 0 || height == 0) {
            Logu.v("视频尺寸", "视频宽高为0，跳过尺寸调整（可能处于听视频模式）");
            return;
        }

        if (SharedPreferencesUtil.getBoolean("player_ui_round", false)) {
            float video_mul = (float) height / (float) width;
            double sqrt = Math.sqrt(screen_width * screen_width / ((double) (height * height) / (width * width) + 1));
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
            }, 60); // 别问为什么，问就是必须这么写，要等上面的绘制完成
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
                    if (video_now_last != video_now) { // 检测进度是否在变动
                        video_now_last = video_now;
                        float curr_sec = video_now / 1000f;
                        runOnUiThread(() -> {
                            if (isLiveMode) {
                                text_progress.setText(StringUtil.toTime((int) curr_sec));
                                text_online.setText(online_number);
                            } else {
                                seekbar_progress.setProgress(video_now);
                                // progressBar上有一个onProgressChange的监听器，文字更改在那里
                            }
                        });
                        if (subtitles != null)
                            showSubtitle(curr_sec + subtitle_delta);
                        else
                            runOnUiThread(() -> text_subtitle.setVisibility(View.GONE));
                        
                        if (viewPointAdapter != null && viewPoints != null && !viewPoints.isEmpty()) {
                            viewPointAdapter.updateCurrentPosition((int) curr_sec);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
                            updateMediaSessionPlaybackState();
                        }
                    }
                }
            }
        };
        progressTimer.schedule(task, 0, 250);
    }

    private void onlineChange() {
        if (!SharedPreferencesUtil.getBoolean("player_show_online", false) || isLiveMode || aid == 0 || cid == 0)
            return;

        onlineTimer = new Timer();
        TimerTask task = new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (ijkPlayer != null) {
                    try {
                        online_number = VideoInfoApi.getWatching(aid, cid);
                        runOnUiThread(() -> {
                            if (!online_number.isEmpty())
                                text_online.setText(online_number + "人在看");
                            else
                                text_online.setText("");
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

    private void getSubtitle(String subtitle_url) {
        if (subtitle_url == null || subtitle_url.isEmpty())
            return;
        try {
            if (isOnlineVideo)
                subtitles = PlayerApi.getSubtitle(subtitle_url);
            else
                subtitles = PlayerApi.getSubtitle(new File(subtitle_url));

            if (subtitles == null)
                return;

            subtitle_count = subtitles.length;
            subtitle_curr_index = 0;
            runOnUiThread(() -> btn_subtitle.setImageResource(R.mipmap.subtitle_on));
        } catch (Exception e) {
            MsgUtil.err(e);
        }
    }

    private void showSubtitle(float curr_sec) {
        if (subtitles == null || subtitle_count == 0) {
            runOnUiThread(() -> text_subtitle.setVisibility(View.GONE));
            return;
        }
        
        Subtitle subtitle_curr = subtitles[subtitle_curr_index];

        boolean need_adjust = true;
        boolean need_show = true;

        while (need_adjust) {
            if (curr_sec < subtitle_curr.from) { // 进度在当前字幕的起始位置之前
                // 如果不是第一条字幕，且进度在上一条字幕的结束位置之前，那么字幕前移一位
                // 否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitle_curr_index != 0 && curr_sec < subtitles[subtitle_curr_index - 1].to) {
                    subtitle_curr_index--;
                } else {
                    need_adjust = false;
                    need_show = false;
                }
            } else if (curr_sec > subtitle_curr.to) { // 在当前字幕的结束位置之后
                // 如果不是最后一条字幕，且进度在下一条字幕的开始位置之后，那么字幕后移一位
                // 否则字幕不显示且退出校准（当前进度在两条字幕之间）
                if (subtitle_curr_index + 1 < subtitle_count && curr_sec > subtitles[subtitle_curr_index + 1].from) {
                    subtitle_curr_index++;
                } else {
                    need_adjust = false;
                    need_show = false;
                }
            } else
                need_adjust = false; // 在当前字幕的时间段内，则退出校准
        }

        if (need_show)
            runOnUiThread(() -> {
                text_subtitle.setText(subtitles[subtitle_curr_index].content);
                text_subtitle.setVisibility(View.VISIBLE);
            });
        else
            runOnUiThread(() -> text_subtitle.setVisibility(View.GONE));
    }

    private int subtitle_selected = -1;

    private void downSubtitle(boolean from_btn) {
        try {
            if (subtitleLinks == null) { // 首次运行，获取字幕
                if (isOnlineVideo)
                    subtitleLinks = PlayerApi.getSubtitleLinks(aid, cid);
                else
                    subtitleLinks = PlayerApi.getSubtitleLinks(new File(danmakuFile.getParentFile(), "subtitles"));
            }

            if (subtitleLinks.length == 1) {
                if (from_btn)
                    MsgUtil.showMsg("本视频无字幕");
                return;
            }

            subtitle_delta = SharedPreferencesUtil.getFloat("player_subtitle_delta", 0.3f);

            boolean ai_not_only = (subtitleLinks.length > 2 || (subtitleLinks.length == 2 && !subtitleLinks[0].isAI));
            boolean ai_allowed = (from_btn || SharedPreferencesUtil.getBoolean("player_subtitle_ai_allowed", false));

            if (ai_not_only || ai_allowed) {
                if (subtitle_selected == -1)
                    subtitle_selected = subtitleLinks.length;

                runOnUiThread(() -> {
                    RecyclerView subtitleRecycler = findViewById(R.id.subtitle_list);
                    SubtitleAdapter adapter = new SubtitleAdapter();
                    adapter.setData(subtitleLinks);
                    adapter.setSelectedItemIndex(subtitle_selected);
                    adapter.setOnItemClickListener(index -> {
                        layout_card_bg.setVisibility(View.GONE);
                        card_subtitle.setVisibility(View.GONE);
                        subtitle_selected = index;

                        if (subtitleLinks[index].id == -1) {
                            subtitles = null;
                            btn_subtitle.setImageResource(R.mipmap.subtitle_off);
                        } else
                            CenterThreadPool.run(() -> getSubtitle(subtitleLinks[index].url));
                    });
                    subtitleRecycler
                            .setLayoutManager(new CustomLinearManager(this, LinearLayoutManager.HORIZONTAL, false));
                    subtitleRecycler.setHasFixedSize(true);
                    subtitleRecycler.setAdapter(adapter);
                    layout_card_bg.setVisibility(View.VISIBLE);
                    card_subtitle.setVisibility(View.VISIBLE);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            MsgUtil.err(e);
        }
    }

    private void downdanmu() {
        if (danmaku_url.isEmpty())
            return;

        boolean useNewApi = SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.NEW_DANMAKU_API, true);

        if (useNewApi) {
            downdanmuNew();
        } else {
            downdanmuOld();
        }
    }

    private void downdanmuOld() {
        try {
            Response response = NetWorkUtil.get(danmaku_url, NetWorkUtil.webHeaders);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists())
                    danmakuFile.createNewFile();
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());// 调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);// 将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
            streamDanmaku(danmakuFile.toString(), null);
        } catch (Exception e) {
            runOnUiThread(() -> MsgUtil.err(e));
        }
    }

    private void downdanmuNew() {
        try {
            int estimatedDuration = 3600;

            if (ijkPlayer != null) {
                long duration = ijkPlayer.getDuration();
                if (duration > 0) {
                    estimatedDuration = (int) (duration / 1000);
                }
            }

            Logu.d("新版弹幕", "开始获取新版弹幕，aid=" + aid + ", cid=" + cid);

            java.util.List<DmSegMobileReply> segments = DanmakuApi.getAllVideoDanmaku(aid, cid, estimatedDuration);

            if (segments.isEmpty()) {
                Logu.w("新版弹幕", "未获取到弹幕，尝试使用旧版接口");
                CenterThreadPool.run(() -> downdanmuOld());
                return;
            }

            Logu.d("新版弹幕", "成功获取 " + segments.size() + " 个弹幕分段");

            streamDanmaku(null, segments);
        } catch (Exception e) {
            e.printStackTrace();
            Logu.e("新版弹幕", "获取失败: " + e.getMessage() + "，回退到旧版接口");
            runOnUiThread(() -> MsgUtil.toast("新版弹幕获取失败，使用旧版接口"));
            CenterThreadPool.run(() -> downdanmuOld());
        }
    }

    private BaseDanmakuParser createParser(String stream) {
        return createParser(stream, null);
    }

    private BaseDanmakuParser createParser(String stream, java.util.List<DmSegMobileReply> protobufSegments) {
        if (protobufSegments != null && !protobufSegments.isEmpty()) {
            BiliProtobufDanmakuParser parser = new BiliProtobufDanmakuParser();
            parser.sharedPreferences = SharedPreferencesUtil.getSharedPreferences();
            parser.setDanmakuSegments(protobufSegments);
            return parser;
        }

        // 兼容性回退
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

    private void streamDanmaku(String danmakuFile) {
        streamDanmaku(danmakuFile, null);
    }

    private void streamDanmaku(String danmakuFile, java.util.List<DmSegMobileReply> protobufSegments) {
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
                .setScaleTextSize(SharedPreferencesUtil.getFloat("player_danmaku_size", 0.7f))// 缩放值
                .setMaximumLines(maxLinesPair)
                .setDanmakuTransparency(SharedPreferencesUtil.getFloat("player_danmaku_transparency", 0.5f))
                .preventOverlapping(overlap);

        BaseDanmakuParser mParser = createParser(danmakuFile, protobufSegments);

        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                Logu.v("danmaku", "prepared");
                String msg = protobufSegments != null
                        ? "弹幕君准备完毕～(是新来的哦～)"
                        : "弹幕君准备完毕～(*≧ω≦)";
                addDanmaku(msg, Color.WHITE);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                if (ijkPlayer != null && isPrepared) {
                    long currentPos = ijkPlayer.getCurrentPosition();
                    timer.update(currentPos);
                }
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
        if (text == null || danmaku == null || ijkPlayer == null)
            return;
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
        Inflater decompresser = new Inflater(true);// 这个true是关键
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
            if (video_now >= video_all - 250) {
                if (interactionData != null && interactionData.edges != null && 
                    interactionData.edges.questions != null && !questionShown) {
                    if (!questionShown) {
                        ijkPlayer.seekTo(0);
                        if (hasDanmaku && mDanmakuView != null) {
                            mDanmakuView.seekTo(0L);
                        }
                        Logu.v("播完重播");
                    }
                } else {
                    ijkPlayer.seekTo(0);
                    if (hasDanmaku && mDanmakuView != null) {
                        mDanmakuView.seekTo(0L);
                    }
                    Logu.v("播完重播");
                }
            }
            playerResume();
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
    private void softwareRotate() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = screen_landscape ? displayMetrics.heightPixels : displayMetrics.widthPixels;
        screen_height = screen_landscape ? displayMetrics.widthPixels : displayMetrics.heightPixels;

        ViewGroup root_layout = findViewById(R.id.root_layout);
        ViewGroup.LayoutParams params = root_layout.getLayoutParams();
        params.width = screen_width;
        params.height = screen_height;

        if (isPrepared && !destroyed)
            runOnUiThread(() -> {
                root_layout.setLayoutParams(params);
                root_layout.setPivotX(0);
                root_layout.setPivotY(0);
                root_layout.setX(screen_landscape ? screen_height : 0);
                root_layout.setRotation(screen_landscape ? 90 : 0);
                if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 26)) {
                    if (textureView != null) {
                        Matrix matrix = new Matrix();
                        textureView.getTransform(matrix);
                        matrix.postRotate(0);
                        textureView.setTransform(matrix);
                    }
                } else {
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

        if (x < video_x_min)
            x = video_x_min;
        if (x > video_x_max)
            x = video_x_max;
        if (y < video_y_min)
            y = video_y_min;
        if (y > video_y_max)
            y = video_y_max;

        if (layout_video.getX() != x || layout_video.getY() != y) {
            // Logu.v("gesture","moveto:" + x + "," + y);
            layout_video.setX(x);
            layout_video.setY(y);
            if (!gesture_moved && (Math.abs(video_origX - x) > 5f || Math.abs(video_origY - y) > 5f)) {
                gesture_moved = true;
            }
        }
    }

    private void playerPause() {
        isPlaying = false;
        if (ijkPlayer != null && isPrepared) {
            ijkPlayer.pause();
            if (hasDanmaku && mDanmakuView != null) {
                mDanmakuView.pause();
            }
        }
        if (btn_control != null)
            btn_control.setImageResource(R.drawable.btn_player_play);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
            updateMediaSessionPlaybackState();
        }
    }

    private void playerResume() {
        isPlaying = true;
        if (ijkPlayer != null && isPrepared) {
            ijkPlayer.start();
            if (hasDanmaku && mDanmakuView != null) {
                mDanmakuView.resume();
            }
        }
        if (btn_control != null)
            btn_control.setImageResource(R.drawable.btn_player_pause);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
            updateMediaSessionPlaybackState();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logu.v("开始旋转屏幕");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;// 获取屏宽
        screen_height = displayMetrics.heightPixels;// 获取屏高
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
        if (!isFinishing()) {
            super.onDestroy();
            return; // 貌似有些设备启动activity会先调用一下onDestroy，头大…… 不super还会报错
        }

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

        if (isOnlineVideo && danmakuFile != null && danmakuFile.exists())
            danmakuFile.delete();

        if (liveWebSocket != null) {
            liveWebSocket.close(1000, "");
            liveWebSocket = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        setRequestedOrientation(SharedPreferencesUtil.getBoolean("ui_landscape", false)
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onDestroy();
    }

    private void cancelAllTimers() {
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
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
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
                ArrayList<String> mHeaders = new ArrayList<>() {
                    {
                        add("Cookie");
                        add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
                        add("Referer");
                        add("https://live.bilibili.com/" + aid);
                        add("Origin");
                        add("https://live.bilibili.com");
                        add("User-Agent");
                        add(USER_AGENT_WEB);
                    }
                };
                Response response = NetWorkUtil.get(ConfInfoApi.signWBI(url), mHeaders);
                JSONObject data = new JSONObject(Objects.requireNonNull(response.body()).string())
                        .getJSONObject("data");
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
                // okHttpClient.dispatcher().executorService().shutdown();
            } catch (Exception e) {
                MsgUtil.showMsg("直播弹幕连接失败");
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void initMediaSession() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        mediaSession = new MediaSession(this, "BiliClientPlayer");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                runOnUiThread(() -> {
                    if (!isPlaying) {
                        playerResume();
                        updateMediaSessionPlaybackState();
                    }
                });
            }

            @Override
            public void onPause() {
                super.onPause();
                runOnUiThread(() -> {
                    if (isPlaying) {
                        playerPause();
                        updateMediaSessionPlaybackState();
                    }
                });
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                runOnUiThread(() -> {
                    if (hasMultiplePages() && currentPageIndex < pagenames.size() - 1) {
                        switchToPage(currentPageIndex + 1);
                    }
                });
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                runOnUiThread(() -> {
                    if (hasMultiplePages() && currentPageIndex > 0) {
                        switchToPage(currentPageIndex - 1);
                    }
                });
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                runOnUiThread(() -> {
                    seekToPosition(pos);
                    updateMediaSessionPlaybackState();
                });
            }
        });
        mediaSession.setActive(true);
    }

    private void updateMediaSessionMetadata() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || mediaSession == null) {
            return;
        }
        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        if (videoTitle != null) {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, videoTitle);
        }
        if (video_all > 0) {
            metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, video_all);
        }
        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updateMediaSessionPlaybackState() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || mediaSession == null) {
            return;
        }
        int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        long position = isPrepared && ijkPlayer != null ? ijkPlayer.getCurrentPosition() : 0;
        long actions = PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_SEEK_TO
                | PlaybackState.ACTION_SKIP_TO_NEXT
                | PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        if (!hasMultiplePages() || currentPageIndex >= pagenames.size() - 1) {
            actions &= ~PlaybackState.ACTION_SKIP_TO_NEXT;
        }
        if (!hasMultiplePages() || currentPageIndex <= 0) {
            actions &= ~PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        }
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setState(state, position, 1.0f)
                .setActions(actions);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void initUI() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = displayMetrics.widthPixels;// 获取屏宽
        screen_height = displayMetrics.heightPixels;// 获取屏高

        if (SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn", true))
            btn_rotate.setVisibility(View.VISIBLE);
        else
            btn_rotate.setVisibility(View.GONE);

        screen_round = SharedPreferencesUtil.getBoolean("player_ui_round", false);
        if (screen_round) {
            int padding = (int) (screen_width * 0.03);

            LinearLayout.LayoutParams progressParams = (LinearLayout.LayoutParams) seekbar_progress.getLayoutParams();
            progressParams.leftMargin = padding * 4;
            progressParams.rightMargin = padding * 4;
            seekbar_progress.setLayoutParams(progressParams);

            text_online.setPadding(0, 0, padding * 3, 0);
            text_progress.setPadding(padding * 3, 0, 0, 0);

            bottom_buttons.setPadding(padding, 0, padding, padding);

            right_control.setPadding(0, 0, padding, 0);

            RelativeLayout.LayoutParams danmakuParams = (RelativeLayout.LayoutParams) mDanmakuView.getLayoutParams();
            danmakuParams.setMargins(0, padding * 3, 0, padding * 3);
            mDanmakuView.setLayoutParams(danmakuParams);

            text_subtitle.setMaxWidth((int) (screen_width * 0.65));

            layout_top.setPadding(padding * 7, padding, padding * 7, 0);

            LinearLayout clockLayout = findViewById(R.id.clock_layout);
            clockLayout.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams clockLayoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clockLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            clockLayout.setLayoutParams(clockLayoutParams);

            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

        if ((!SharedPreferencesUtil.getBoolean("player_show_online", false)) || aid == 0 || cid == 0)
            text_online.setVisibility(View.GONE);

        layout_top.setOnClickListener(view -> finish());

        // 提前根据设置隐藏按钮，避免在视频加载完成前显示
        if (!SharedPreferencesUtil.getBoolean("player_ui_showPageBtn", true)) {
            btn_page_selector.setVisibility(View.GONE);
        }
        if (!SharedPreferencesUtil.getBoolean("player_ui_showQualityBtn", true) || !isOnlineVideo) {
            btn_quality.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        if (SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT < 26)) {
            textureView = new TextureView(this);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    Logu.v("surfacetexture", "available");
                    mSurfaceTexture = surfaceTexture;
                    if (isPrepared && ijkPlayer != null)
                        ijkPlayer.setSurface(new Surface(surfaceTexture));
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                    Logu.v("surfacetexture", "sizechanged");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                    Logu.v("surfacetexture", "destroyed");
                    mSurfaceTexture = null;
                    if (ijkPlayer != null)
                        ijkPlayer.setSurface(null);
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                }
            });
            layout_video.addView(textureView, params);
        } else {
            surfaceView = new SurfaceView(this);
            layout_video.addView(surfaceView, params);
        }

        btn_rotate.setOnClickListener(view -> {
            Logu.v("点击旋转按钮");
            screen_landscape = !screen_landscape;
            if (SharedPreferencesUtil.getBoolean("dev_player_rotate_software", false))
                softwareRotate();
            else
                setRequestedOrientation(screen_landscape ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        findViewById(R.id.button_sound_add).setOnClickListener(view -> changeVolume(true));
        findViewById(R.id.button_sound_cut).setOnClickListener(view -> changeVolume(false));

        btn_menu.setOnClickListener(view -> {
            if (menu_opened) {
                right_second.setVisibility(View.GONE);
                btn_menu.setImageResource(R.mipmap.morehide);
            } else {
                right_second.setVisibility(View.VISIBLE);
                btn_menu.setImageResource(R.mipmap.moreshow);
            }
            menu_opened = !menu_opened;
        });

        layout_card_bg.setOnClickListener(view -> {
            layout_card_bg.setVisibility(View.GONE);
            card_subtitle.setVisibility(View.GONE);
            card_danmaku_send.setVisibility(View.GONE);
            card_page_selector.setVisibility(View.GONE);
            card_quality_selector.setVisibility(View.GONE);
            card_viewpoint_selector.setVisibility(View.GONE);
        });
        btn_danmaku_send.setOnClickListener(view -> {
            layout_card_bg.setVisibility(View.VISIBLE);
            card_danmaku_send.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.danmaku_send).setOnClickListener(view1 -> {
            EditText editText = findViewById(R.id.danmaku_send_edit);
            if (editText.getText().toString().isEmpty()) {
                MsgUtil.showMsg("不能发送空弹幕喵");
            } else {
                layout_card_bg.setVisibility(View.GONE);
                card_danmaku_send.setVisibility(View.GONE);

                CenterThreadPool.run(() -> {
                    try {
                        MsgUtil.showMsg("正在发送~");

                        int result = DanmakuApi.sendVideoDanmakuByAid(cid, editText.getText().toString(), aid,
                                video_now, ToolsUtil.getRgb888(Color.WHITE), 1);

                        if (result == 0) {
                            MsgUtil.showMsg("发送成功喵~");
                            runOnUiThread(() -> {
                                addDanmaku(editText.getText().toString(), Color.WHITE);
                                editText.setText("");
                            });
                        } else
                            MsgUtil.showMsg("发送失败：" + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MsgUtil.err(e);
                    }
                });
            }
        });
    }

    private void initSeekbars() {
        seekbar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                runOnUiThread(() -> {
                    if (!isLiveMode)
                        text_progress.setText(StringUtil.toTime(position / 1000) + "/" + progress_str);
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
                    int seekPos = seekbar_progress.getProgress();
                    ijkPlayer.seekTo(seekPos);
                    if (hasDanmaku && mDanmakuView != null) {
                        mDanmakuView.seekTo((long) seekPos);
                    }
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
                    if (ijkPlayer != null)
                        ijkPlayer.setSpeed(speed_values[position]);
                    if (mDanmakuView != null)
                        mDanmakuView.setSpeed(speed_values[position]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (speedTimer != null)
                    speedTimer.cancel();
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
        if (isPrepared)
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    controlVideo();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    seekToPosition(ijkPlayer.getCurrentPosition() - 10000L);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    seekToPosition(ijkPlayer.getCurrentPosition() + 10000L);
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

    private void seekToPosition(long position) {
        if (ijkPlayer != null && isPrepared) {
            ijkPlayer.seekTo(position);
            if (hasDanmaku && mDanmakuView != null) {
                mDanmakuView.seekTo(position);
            }
        }
    }

    private void toggleAudioOnlyMode() {
        boolean oldMode = isAudioOnlyMode;
        isAudioOnlyMode = !isAudioOnlyMode;
        // 不保存状态，仅在当前播放会话中切换

        if (isPrepared && ijkPlayer != null) {
            final long currentPosition = ijkPlayer.getCurrentPosition();
            final boolean wasPlaying = isPlaying;

            MsgUtil.showMsg(isAudioOnlyMode ? "正在切换到听视频模式..." : "正在切换到普通模式...");

            CenterThreadPool.run(() -> {
                try {
                    runOnUiThread(() -> {
                        if (hasDanmaku && mDanmakuView != null) {
                            mDanmakuView.pause();
                        }
                        if (ijkPlayer != null) {
                            ijkPlayer.stop();
                            ijkPlayer.release();
                        }

                        loading_info.setVisibility(View.VISIBLE);
                        anim_loading.start();
                        loading_text0.setText(isAudioOnlyMode ? "切换到听视频模式" : "切换到普通模式");
                        isPrepared = false;
                        isPlaying = false;

                        updateAudioOnlyButton();
                        updateAudioOnlyUI();
                    });

                    Thread.sleep(100);

                    runOnUiThread(() -> {
                        ijkPlayer = new IjkMediaPlayer();
                        progress_history = currentPosition;

                        setDisplay();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        MsgUtil.showMsg("切换失败，请重试");
                        isAudioOnlyMode = oldMode;
                        // 不保存状态
                        updateAudioOnlyButton();
                        updateAudioOnlyUI();
                        loading_info.setVisibility(View.GONE);
                        anim_loading.stop();
                    });
                }
            });
        } else {
            updateAudioOnlyButton();
            updateAudioOnlyUI();
            MsgUtil.showMsg(isAudioOnlyMode ? "已切换到听视频模式" : "已切换到普通模式");
        }
    }

    private void updateAudioOnlyButton() {
        if (btn_audio_only != null) {
            btn_audio_only
                    .setImageResource(isAudioOnlyMode ? R.drawable.icon_audio_only_on : R.drawable.icon_audio_only_off);
        }
    }

    private void updateAudioOnlyUI() {
        runOnUiThread(() -> {
            if (isAudioOnlyMode) {
                // 进入听视频模式
                text_speed.setVisibility(View.GONE);
                btn_debug.setVisibility(View.GONE);
                btn_danmaku.setVisibility(View.GONE);
                layout_video.setVisibility(View.GONE);
                layout_audio_only.setVisibility(View.VISIBLE);
                if (mDanmakuView != null) {
                    mDanmakuView.setVisibility(View.GONE);
                }
                if (text_audio_title != null) {
                    String title = text_title.getText().toString();
                    text_audio_title.setText(title.isEmpty() ? "听视频模式" : title);
                }
            } else {
                // 退出听视频模式
                text_speed.setVisibility(View.VISIBLE);
                updateDebugButtonVisibility();
                btn_danmaku.setVisibility(View.VISIBLE);
                layout_video.setVisibility(View.VISIBLE);
                layout_audio_only.setVisibility(View.GONE);
                if (mDanmakuView != null && isDanmakuVisible) {
                    mDanmakuView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean eventBusInit = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this);
            Logu.v("event", "register");
            eventBusInit = true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SnackEvent snackEvent) {
        if (isFinishing())
            return;
        Logu.v("event", "onEvent");

        long currentTime = System.currentTimeMillis();

        int duration;
        if (snackEvent.getDuration() > 0)
            duration = snackEvent.getDuration();
        else if (snackEvent.getDuration() == Snackbar.LENGTH_SHORT)
            duration = 1950;
        else if (snackEvent.getDuration() == Snackbar.LENGTH_INDEFINITE)
            duration = Integer.MAX_VALUE;
        else
            duration = 2750;

        long endTime = snackEvent.getStartTime() + duration;
        if (currentTime >= endTime) {
            EventBus.getDefault().removeStickyEvent(snackEvent);
        } else {
            MsgUtil.toast(snackEvent.getMessage()); // 由于Theme.Black不支持，只能这样用了
        }
    }

    protected boolean eventBusEnabled() {
        return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true);
    }

    /**
     * 加载高能进度条数据
     */
    private void loadHighEnergyData() {
        if (!SharedPreferencesUtil.getBoolean("player_high_energy", false)) {
            Logu.d("高能进度条", "功能已禁用");
            return;
        }

        CenterThreadPool.run(() -> {
            try {
                Logu.d("高能进度条", "开始加载数据 aid=" + aid + " cid=" + cid);
                HighEnergyData data = PlayerApi.getHighEnergyData(cid, aid);

                if (data != null && data.hasValidData()) {
                    runOnUiThread(() -> {
                        if (!destroyed && seekbar_progress != null) {
                            seekbar_progress.setHighEnergyData(data.events, data.stepSec);
                            Logu.d("高能进度条", "数据加载成功并设置到进度条");
                        }
                    });
                } else {
                    Logu.w("高能进度条", "未获取到有效数据");
                }
            } catch (Exception e) {
                Logu.e("高能进度条", "加载失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private boolean hasMultiplePages() {
        return pagenames != null && pagenames.size() > 1;
    }

    private void showPageSelectorCard() {
        if (!hasMultiplePages())
            return;

        runOnUiThread(() -> {
            RecyclerView pageSelectorRecycler = findViewById(R.id.page_selector_list);
            PageSelectorAdapter adapter = new PageSelectorAdapter();
            adapter.setData(pagenames, currentPageIndex);
            adapter.setOnItemClickListener(index -> {
                layout_card_bg.setVisibility(View.GONE);
                card_page_selector.setVisibility(View.GONE);
                if (index != currentPageIndex) {
                    switchToPage(index);
                }
            });
            pageSelectorRecycler.setLayoutManager(new CustomLinearManager(this));
            pageSelectorRecycler.setAdapter(adapter);
            layout_card_bg.setVisibility(View.VISIBLE);
            card_page_selector.setVisibility(View.VISIBLE);
        });
    }

    private void switchToPage(int pageIndex) {
        if (!hasMultiplePages() || pageIndex < 0 || pageIndex >= pagenames.size())
            return;
        if (pageIndex == currentPageIndex)
            return;

        currentPageIndex = pageIndex;
        long newCid = cids.get(pageIndex);
        String newTitle = pagenames.get(pageIndex);

        MsgUtil.showMsg("切换到 P" + (pageIndex + 1));

        CenterThreadPool.run(() -> {
            try {
                PlayerData playerData = new PlayerData();
                playerData.aid = aid;
                playerData.cid = newCid;
                playerData.title = newTitle;
                playerData.mid = mid;
                playerData.qn = SharedPreferencesUtil.getInt("play_qn", 16);
                playerData.pagenames = pagenames;
                playerData.cids = cids;
                playerData.currentPageIndex = currentPageIndex;

                if (isOnlineVideo) {
                    PlayerApi.getVideo(playerData, false);
                } else {
                    runOnUiThread(() -> MsgUtil.showMsg("本地视频暂不支持切换分P"));
                    return;
                }

                runOnUiThread(() -> {
                    if (destroyed)
                        return;

                    if (ijkPlayer != null) {
                        ijkPlayer.stop();
                        ijkPlayer.release();
                    }
                    if (mDanmakuView != null) {
                        mDanmakuView.release();
                        mDanmakuView = null;
                    }

                    cid = newCid;
                    video_url = playerData.videoUrl;
                    danmaku_url = playerData.danmakuUrl;
                    text_title.setText(newTitle);
                    videoTitle = newTitle;

                    if (playerData.qnStrList != null && playerData.qnValueList != null) {
                        qnStrList = playerData.qnStrList;
                        qnValueList = playerData.qnValueList;
                        currentQuality = playerData.qn;
                    }

                    loading_info.setVisibility(View.VISIBLE);
                    anim_loading.start();
                    loading_text0.setText("加载P" + (pageIndex + 1));
                    isPrepared = false;
                    isPlaying = false;
                    finishWatching = false;
                    progress_history = 0;
                    subtitles = null;
                    subtitleLinks = null;
                    subtitle_selected = -1;
                    viewPoints = null;
                    viewPointAdapter = null;
                    if (btn_viewpoint != null) {
                        btn_viewpoint.setVisibility(View.GONE);
                    }

                    interactionData = null;
                    currentEdgeId = 0;
                    currentQuestion = null;
                    questionShown = false;
                    if (interactionChoiceLayout != null) {
                        interactionChoiceLayout.setVisibility(View.GONE);
                        interactionChoiceLayout.removeAllViews();
                    }

                    ijkPlayer = new IjkMediaPlayer();
                    mDanmakuView = findViewById(R.id.sv_danmaku);

                    setDisplay();

                    layout_control.postDelayed(() -> CenterThreadPool.run(() -> {
                        if (destroyed)
                            return;

                        runOnUiThread(() -> {
                            loading_text0.setText("装填弹幕中");
                            loading_text1.setText("(≧∇≦)");
                        });

                        if (isOnlineVideo) {
                            danmakuFile = new File(getCacheDir(), "danmaku.xml");
                            if (danmakuFile.exists()) {
                                danmakuFile.delete();
                            }
                            downdanmu();
                        }

                        if (!destroyed && SharedPreferencesUtil.getBoolean("player_subtitle_autoshow", true)) {
                            downSubtitle(false);
                        }

                        if (!destroyed && isOnlineVideo && aid > 0 && cid > 0) {
                            loadHighEnergyData();
                        }

                        if (!destroyed && isOnlineVideo && aid > 0 && cid > 0 && SharedPreferencesUtil.getBoolean("player_show_viewpoints", false)) {
                            loadViewPoints();
                        }

                        if (!destroyed && isOnlineVideo && aid > 0 && cid > 0) {
                            loadInteractionVideo();
                        }
                    }), 60);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    MsgUtil.err(e);
                    MsgUtil.showMsg("切换失败");
                });
            }
        });
    }

    private void toggleAutoNext() {
        auto_next_enabled = !auto_next_enabled;
        updateAutoNextButton();
        MsgUtil.showMsg(auto_next_enabled ? "已开启自动连播" : "已关闭自动连播");
    }

    private void updateAutoNextButton() {
        if (btn_auto_next != null) {
            btn_auto_next
                    .setImageResource(auto_next_enabled ? R.drawable.icon_auto_next_on : R.drawable.icon_auto_next_off);
        }
    }

    private void showQualitySelectorCard() {
        if (qnStrList == null || qnValueList == null || qnStrList.length == 0) {
            MsgUtil.showMsg("清晰度列表未加载");
            return;
        }

        runOnUiThread(() -> {
            RecyclerView qualitySelectorRecycler = findViewById(R.id.quality_selector_list);
            QualitySelectorAdapter adapter = new QualitySelectorAdapter();
            adapter.setData(qnStrList, qnValueList, currentQuality);
            adapter.setOnItemClickListener(index -> {
                layout_card_bg.setVisibility(View.GONE);
                card_quality_selector.setVisibility(View.GONE);
                if (index >= 0 && index < qnValueList.length && qnValueList[index] != currentQuality) {
                    switchQuality(qnValueList[index]);
                }
            });
            qualitySelectorRecycler
                    .setLayoutManager(new CustomLinearManager(this, LinearLayoutManager.HORIZONTAL, false));
            qualitySelectorRecycler.setAdapter(adapter);
            layout_card_bg.setVisibility(View.VISIBLE);
            card_quality_selector.setVisibility(View.VISIBLE);
        });
    }

    private void switchQuality(int newQuality) {
        if (!isOnlineVideo || newQuality == currentQuality) {
            return;
        }

        MsgUtil.showMsg("正在切换清晰度...");

        CenterThreadPool.run(() -> {
            try {
                PlayerData playerData = new PlayerData();
                playerData.aid = aid;
                playerData.cid = cid;
                playerData.title = text_title.getText().toString();
                playerData.mid = mid;
                playerData.qn = newQuality;
                playerData.pagenames = pagenames;
                playerData.cids = cids;
                playerData.currentPageIndex = currentPageIndex;

                PlayerApi.getVideo(playerData, false);

                runOnUiThread(() -> {
                    if (destroyed)
                        return;

                    final long currentPosition = ijkPlayer != null ? ijkPlayer.getCurrentPosition() : 0;
                    final boolean wasPlaying = isPlaying;

                    if (ijkPlayer != null) {
                        ijkPlayer.stop();
                        ijkPlayer.release();
                    }

                    video_url = playerData.videoUrl;
                    currentQuality = newQuality;

                    if (playerData.qnStrList != null && playerData.qnValueList != null) {
                        qnStrList = playerData.qnStrList;
                        qnValueList = playerData.qnValueList;
                    }

                    loading_info.setVisibility(View.VISIBLE);
                    anim_loading.start();
                    loading_text0.setText("切换清晰度中");
                    isPrepared = false;
                    isPlaying = false;

                    ijkPlayer = new IjkMediaPlayer();
                    progress_history = currentPosition;

                    setDisplay();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    MsgUtil.err(e);
                    MsgUtil.showMsg("清晰度切换失败");
                });
            }
        });
    }

    private List<ViewPoint> viewPoints;
    private ViewPointAdapter viewPointAdapter;

    private void loadViewPoints() {
        CenterThreadPool.run(() -> {
            try {
                Logu.d("视频分段", "开始加载分段数据 aid=" + aid + " cid=" + cid);
                viewPoints = PlayerApi.getViewPoints(aid, cid);

                if (viewPoints != null && !viewPoints.isEmpty()) {
                    runOnUiThread(() -> {
                        if (!destroyed && btn_viewpoint != null) {
                            btn_viewpoint.setVisibility(View.VISIBLE);
                            btn_viewpoint.setOnClickListener(view -> showViewPointSelectorCard());
                            Logu.d("视频分段", "成功加载 " + viewPoints.size() + " 个分段");
                        }
                    });
                } else {
                    Logu.d("视频分段", "未获取到分段数据");
                }
            } catch (Exception e) {
                Logu.e("视频分段", "加载失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void showViewPointSelectorCard() {
        if (viewPoints == null || viewPoints.isEmpty())
            return;

        runOnUiThread(() -> {
            RecyclerView viewPointRecycler = findViewById(R.id.viewpoint_selector_list);
            if (viewPointAdapter == null) {
                viewPointAdapter = new ViewPointAdapter();
                viewPointAdapter.setData(viewPoints);
                viewPointAdapter.setOnItemClickListener(index -> {
                    layout_card_bg.setVisibility(View.GONE);
                    card_viewpoint_selector.setVisibility(View.GONE);
                    if (index >= 0 && index < viewPoints.size()) {
                        ViewPoint vp = viewPoints.get(index);
                        seekToPosition(vp.from * 1000L);
                        MsgUtil.showMsg("跳转到: " + vp.content);
                    }
                });
                viewPointRecycler.setLayoutManager(new CustomLinearManager(this, LinearLayoutManager.HORIZONTAL, false));
                viewPointRecycler.setAdapter(viewPointAdapter);
            }
            if (ijkPlayer != null && isPrepared) {
                int currentPos = (int) (ijkPlayer.getCurrentPosition() / 1000);
                viewPointAdapter.updateCurrentPosition(currentPos);
            }
            layout_card_bg.setVisibility(View.VISIBLE);
            card_viewpoint_selector.setVisibility(View.VISIBLE);
        });
    }





    // 再往下就是互动视频的天下了

    private void loadInteractionVideo() {
        CenterThreadPool.run(() -> {
            try {
                long graphVersion = PlayerApi.getInteractionGraphVersion(aid, cid);
                if (graphVersion > 0) {
                    interactionGraphVersion = graphVersion;
                    Logu.d("互动视频", "检测到互动视频，graph_version: " + interactionGraphVersion + ", cid: " + cid);
                    
                    runOnUiThread(() -> {
                        questionShown = false;
                        currentQuestion = null;
                        if (interactionChoiceLayout != null) {
                            interactionChoiceLayout.setVisibility(View.GONE);
                            interactionChoiceLayout.removeAllViews();
                        }
                    });
                    
                    long edgeId = 0;
                    if (initialEdgeId > 0) {
                        edgeId = initialEdgeId;
                        initialEdgeId = 0;
                    } else if (interactionData != null && currentEdgeId > 0) {
                        edgeId = currentEdgeId;
                    }
                    
                    interactionData = InteractionVideoApi.getEdgeInfo(aid, null, interactionGraphVersion, edgeId);
                    if (interactionData != null) {
                        currentEdgeId = interactionData.edgeId;
                        Logu.d("互动视频", "成功加载互动视频数据，edge_id: " + currentEdgeId);
                        runOnUiThread(() -> updateDebugButtonVisibility());
                    }
                } else {
                    interactionData = null;
                    currentEdgeId = 0;
                    runOnUiThread(() -> {
                        questionShown = false;
                        currentQuestion = null;
                        updateDebugButtonVisibility();
                    });
                }
            } catch (Exception e) {
                Logu.e("互动视频", "加载失败: " + e.getMessage());
                e.printStackTrace();
                interactionData = null;
                currentEdgeId = 0;
                runOnUiThread(() -> {
                    questionShown = false;
                    currentQuestion = null;
                    updateDebugButtonVisibility();
                });
            }
        });
    }

    private void updateDebugButtonVisibility() {
        if (btn_debug == null) return;
        boolean debugEnabled = SharedPreferencesUtil.getBoolean("player_interaction_debug", false);
        if (debugEnabled && interactionData != null && interactionData.hiddenVars != null && !interactionData.hiddenVars.isEmpty() && !isLiveMode && !isAudioOnlyMode) {
            btn_debug.setVisibility(layout_top.getVisibility());
        } else {
            btn_debug.setVisibility(View.GONE);
        }
    }

    private void checkEndInteractionQuestions() {
        if (interactionData == null || interactionData.edges == null || 
            interactionData.edges.questions == null || questionShown) {
            return;
        }

        for (InteractionVideoData.InteractionQuestion question : interactionData.edges.questions) {
            if (question.type == 0) {
                if (question.choices != null && !question.choices.isEmpty()) {
                    for (InteractionVideoData.InteractionChoice choice : question.choices) {
                        if (choice.isHidden == 1) continue;
                        
                        if (choice.condition != null && !choice.condition.isEmpty()) {
                            if (!evaluateCondition(choice.condition)) {
                                continue;
                            }
                        }
                        
                        handleChoiceSelection(choice);
                        break;
                    }
                }
                continue;
            }
            showInteractionQuestion(question);
        }
    }

    private void showInteractionQuestion(InteractionVideoData.InteractionQuestion question) {
        if (questionShown || question.choices == null || question.choices.isEmpty()) {
            return;
        }

        runOnUiThread(() -> {
            questionShown = true;
            currentQuestion = question;

            if (question.pauseVideo == 1 && isPlaying) {
                ijkPlayer.pause();
                isPlaying = false;
                btn_control.setImageResource(R.drawable.btn_player_play);
            }

            if (interactionChoiceLayout == null) {
                createInteractionChoiceLayout();
            }

            interactionChoiceLayout.removeAllViews();
            
            for (InteractionVideoData.InteractionChoice choice : question.choices) {
                if (choice.isHidden == 1) continue;
                
                if (choice.condition != null && !choice.condition.isEmpty()) {
                    if (!evaluateCondition(choice.condition)) {
                        continue;
                    }
                }

                TextView choiceView = createChoiceView(choice);
                interactionChoiceLayout.addView(choiceView);
            }

            if (interactionChoiceLayout.getChildCount() > 0) {
                interactionChoiceLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private TextView createChoiceView(InteractionVideoData.InteractionChoice choice) {
        TextView choiceView = (TextView) LayoutInflater.from(this).inflate(R.layout.cell_interaction_choice, null);
        choiceView.setText(choice.option);
        choiceView.setOnClickListener(v -> handleChoiceSelection(choice));
        return choiceView;
    }

    private void createInteractionChoiceLayout() {
        RelativeLayout rootLayout = findViewById(R.id.root_layout);
        interactionChoiceLayout = new LinearLayout(this);
        interactionChoiceLayout.setOrientation(LinearLayout.VERTICAL);
        interactionChoiceLayout.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.setMargins(0, 0, 0, 100);
        
        interactionChoiceLayout.setLayoutParams(params);
        interactionChoiceLayout.setVisibility(View.GONE);
        rootLayout.addView(interactionChoiceLayout);
    }

    private boolean evaluateCondition(String condition) {
        if (interactionData == null || interactionData.hiddenVars == null) {
            return true;
        }
        
        try {
            for (InteractionVideoData.InteractionHiddenVar var : interactionData.hiddenVars) {
                condition = condition.replace(var.idV2, String.valueOf(var.value));
            }
            
            return evaluateExpression(condition);
        } catch (Exception e) {
            Logu.e("互动视频", "条件判断失败: " + e.getMessage());
            return true;
        }
    }

    private boolean evaluateExpression(String expr) {
        try {
            expr = expr.trim();
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left >= right;
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left <= right;
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left > right;
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left < right;
            } else if (expr.contains("==")) {
                String[] parts = expr.split("==");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left == right;
            } else if (expr.contains("!=")) {
                String[] parts = expr.split("!=");
                long left = Long.parseLong(parts[0].trim());
                long right = Long.parseLong(parts[1].trim());
                return left != right;
            }
        } catch (Exception e) {
            Logu.e("互动视频", "表达式计算失败: " + expr);
        }
        return true;
    }

    private void handleChoiceSelection(InteractionVideoData.InteractionChoice choice) {
        hideInteractionChoices();

        if (choice.nativeAction != null && !choice.nativeAction.isEmpty()) {
            executeNativeAction(choice.nativeAction);
        }

        CenterThreadPool.run(() -> {
            try {
                long targetEdgeId = choice.id;
                InteractionVideoData newData = InteractionVideoApi.getEdgeInfo(aid, null, interactionGraphVersion, targetEdgeId);
                
                if (newData == null) {
                    runOnUiThread(() -> MsgUtil.showMsg("获取互动视频数据失败"));
                    return;
                }

                interactionData = newData;
                currentEdgeId = newData.edgeId;
                
                long targetCid = choice.cid;
                if (targetCid > 0 && targetCid != cid) {
                    jumpToInteractionPage(targetCid, newData);
                } else {
                    resumePlaybackIfPaused();
                }
            } catch (Exception e) {
                Logu.e("互动视频", "处理选择失败: " + e.getMessage());
                runOnUiThread(() -> MsgUtil.showMsg("处理选择失败: " + e.getMessage()));
            }
        });
    }

    private void hideInteractionChoices() {
        runOnUiThread(() -> {
            if (interactionChoiceLayout != null) {
                interactionChoiceLayout.setVisibility(View.GONE);
            }
            questionShown = false;
            currentQuestion = null;
        });
    }

    private void jumpToInteractionPage(long targetCid, InteractionVideoData newData) {
        CenterThreadPool.run(() -> {
            try {
                PlayerData playerData = new PlayerData();
                playerData.aid = aid;
                playerData.cid = targetCid;
                playerData.title = newData.title;
                playerData.mid = mid;
                playerData.qn = getTargetQuality();
                
                if (pagenames != null && cids != null) {
                    playerData.pagenames = pagenames;
                    playerData.cids = cids;
                    int newPageIndex = cids.indexOf(targetCid);
                    if (newPageIndex >= 0) {
                        playerData.currentPageIndex = newPageIndex;
                        currentPageIndex = newPageIndex;
                    }
                }
                
                PlayerApi.getVideo(playerData, false);
                
                runOnUiThread(() -> {
                    if (destroyed)
                        return;
                    
                    if (ijkPlayer != null) {
                        ijkPlayer.stop();
                        ijkPlayer.release();
                    }
                    if (mDanmakuView != null) {
                        mDanmakuView.release();
                        mDanmakuView = null;
                    }
                    
                    cid = targetCid;
                    video_url = playerData.videoUrl;
                    danmaku_url = playerData.danmakuUrl;
                    text_title.setText(newData.title);
                    videoTitle = newData.title;
                    currentEdgeId = newData.edgeId;
                    
                    if (playerData.qnStrList != null && playerData.qnValueList != null) {
                        qnStrList = playerData.qnStrList;
                        qnValueList = playerData.qnValueList;
                        currentQuality = playerData.qn;
                    }
                    
                    loading_info.setVisibility(View.VISIBLE);
                    anim_loading.start();
                    loading_text0.setText("加载互动分P");
                    isPrepared = false;
                    isPlaying = false;
                    finishWatching = false;
                    progress_history = 0;
                    subtitles = null;
                    subtitleLinks = null;
                    subtitle_selected = -1;
                    viewPoints = null;
                    viewPointAdapter = null;
                    if (btn_viewpoint != null) {
                        btn_viewpoint.setVisibility(View.GONE);
                    }
                    
                    interactionData = newData;
                    currentQuestion = null;
                    questionShown = false;
                    if (interactionChoiceLayout != null) {
                        interactionChoiceLayout.setVisibility(View.GONE);
                        interactionChoiceLayout.removeAllViews();
                    }
                    
                    ijkPlayer = new IjkMediaPlayer();
                    mDanmakuView = findViewById(R.id.sv_danmaku);
                    
                    setDisplay();
                    
                    layout_control.postDelayed(() -> CenterThreadPool.run(() -> {
                        if (destroyed)
                            return;
                        
                        runOnUiThread(() -> {
                            loading_text0.setText("装填弹幕中");
                            loading_text1.setText("(≧∇≦)");
                        });
                        
                        if (isOnlineVideo) {
                            danmakuFile = new File(getCacheDir(), "danmaku.xml");
                            if (danmakuFile.exists()) {
                                danmakuFile.delete();
                            }
                            downdanmu();
                        }
                        
                        if (!destroyed && SharedPreferencesUtil.getBoolean("player_subtitle_autoshow", true)) {
                            downSubtitle(false);
                        }
                        
                        if (!destroyed && isOnlineVideo && aid > 0 && cid > 0) {
                            loadHighEnergyData();
                        }
                        
                        if (!destroyed && isOnlineVideo && aid > 0 && cid > 0 && SharedPreferencesUtil.getBoolean("player_show_viewpoints", false)) {
                            loadViewPoints();
                        }
                    }), 60);
                });
            } catch (Exception e) {
                Logu.e("互动视频", "跳转失败: " + e.getMessage());
                runOnUiThread(() -> MsgUtil.showMsg("跳转失败: " + e.getMessage()));
            }
        });
    }

    private void resumePlaybackIfPaused() {
        runOnUiThread(() -> {
            if (currentQuestion != null && currentQuestion.pauseVideo == 1 && !isPlaying) {
                ijkPlayer.start();
                isPlaying = true;
                btn_control.setImageResource(R.drawable.btn_player_pause);
            }
        });
    }

    private int getTargetQuality() {
        int defaultQn = SharedPreferencesUtil.getInt("play_qn", 16);
        if (qnValueList == null || qnValueList.length == 0) {
            return currentQuality > 0 ? currentQuality : defaultQn;
        }
        
        for (int qn : qnValueList) {
            if (qn == currentQuality) {
                return currentQuality;
            }
        }
        
        return currentQuality > 0 ? currentQuality : defaultQn;
    }

    private void executeNativeAction(String nativeAction) {
        if (interactionData == null || interactionData.hiddenVars == null || nativeAction == null || nativeAction.isEmpty()) {
            return;
        }

        String[] actions = nativeAction.split(";");
        for (String action : actions) {
            action = action.trim();
            if (action.isEmpty()) continue;

            try {
                if (action.contains("=")) {
                    String[] parts = action.split("=");
                    if (parts.length == 2) {
                        String varId = parts[0].trim();
                        String valueExpr = parts[1].trim();
                        
                        long value = evaluateValueExpression(valueExpr);
                        
                        for (InteractionVideoData.InteractionHiddenVar var : interactionData.hiddenVars) {
                            if (var.idV2.equals(varId)) {
                                var.value = value;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logu.e("互动视频", "执行动作失败: " + action);
            }
        }
    }

    private long evaluateValueExpression(String expr) {
        try {
            expr = expr.trim();
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                long sum = 0;
                for (String part : parts) {
                    sum += evaluateValueExpression(part.trim());
                }
                return sum;
            } else if (expr.contains("-")) {
                String[] parts = expr.split("-");
                long result = evaluateValueExpression(parts[0].trim());
                for (int i = 1; i < parts.length; i++) {
                    result -= evaluateValueExpression(parts[i].trim());
                }
                return result;
            } else {
                if (interactionData != null && interactionData.hiddenVars != null) {
                    for (InteractionVideoData.InteractionHiddenVar var : interactionData.hiddenVars) {
                        if (expr.equals(var.idV2)) {
                            return var.value;
                        }
                    }
                }
                if (expr.contains(".")) {
                    return (long) Double.parseDouble(expr);
                } else {
                    return Long.parseLong(expr);
                }
            }
        } catch (Exception e) {
            Logu.e("互动视频", "值表达式计算失败: " + expr + ", 错误: " + e.getMessage());
            return 0;
        }
    }

    private void showInteractionDebugDialog() {
        if (interactionData == null || interactionData.hiddenVars == null || interactionData.hiddenVars.isEmpty()) {
            MsgUtil.showMsg("当前没有互动视频变量");
            return;
        }

        InteractionDebugActivity.setInteractionData(interactionData);
        Intent intent = new Intent(this, InteractionDebugActivity.class);
        startActivity(intent);
    }

    @Override
    public void finish() {
        if (isPlaying)
            playerPause();
        if (ijkPlayer != null) {
            Intent result = new Intent();
            result.putExtra("progress", (int) ijkPlayer.getCurrentPosition());
            Logu.d("进度回传", String.valueOf(ijkPlayer.getCurrentPosition()));
            setResult(RESULT_OK, result);
        } else
            setResult(RESULT_CANCELED);
        super.finish();
    }
}