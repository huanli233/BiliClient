package com.RobinNotBad.BiliClient.activity.settings.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.api.CookiesApi;
import com.RobinNotBad.BiliClient.api.LoginApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

public class QRLoginFragment extends Fragment {
    private ImageView qrImageView;
    private TextView scanStat;
    Bitmap QRImage;
    Timer timer;
    boolean need_refresh = false;
    boolean from_setup = false;
    int qrScale = 0;

    public QRLoginFragment() {
    }

    public static QRLoginFragment newInstance(boolean from_setup) {
        Bundle args = new Bundle();
        args.putBoolean("from_setup", from_setup);
        QRLoginFragment fragment = new QRLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);

        Bundle bundle = getArguments();
        if(bundle!=null) from_setup = bundle.getBoolean("from_setup",false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_login, container, false);

        qrImageView = view.findViewById(R.id.qrImage);
        scanStat = view.findViewById(R.id.scanStat);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialCardView jump = view.findViewById(R.id.jump);
        jump.setOnClickListener(v -> {
            if (from_setup) startActivity(new Intent(requireContext(), SplashActivity.class));
            if (timer != null) timer.cancel();
            if (isAdded()) requireActivity().finish();
        });

        MaterialCardView special = view.findViewById(R.id.special);
        special.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SpecialLoginActivity.class);
            intent.putExtra("from_setup", from_setup);
            startActivity(intent);
            if (timer != null) timer.cancel();
            if (isAdded()) requireActivity().finish();
        });

        qrImageView.setOnClickListener(v -> {
            if (need_refresh) {
                qrImageView.setImageResource(R.mipmap.loading);
                qrImageView.setEnabled(false);
                refreshQrCode();
            } else {
                Guideline guideline_left = view.findViewById(R.id.guideline33);
                Guideline guideline_right = view.findViewById(R.id.guideline34);
                switch (qrScale) {
                    case 0:
                        guideline_left.setGuidelinePercent(0.00f);
                        guideline_right.setGuidelinePercent(1.00f);
                        MsgUtil.showMsg("切换为大二维码");
                        qrScale = 1;
                        break;
                    case 1:
                        guideline_left.setGuidelinePercent(0.30f);
                        guideline_right.setGuidelinePercent(0.70f);
                        MsgUtil.showMsg("切换为小二维码");
                        qrScale = 2;
                        break;
                    case 2:
                        guideline_left.setGuidelinePercent(0.15f);
                        guideline_right.setGuidelinePercent(0.85f);
                        MsgUtil.showMsg("切换为默认大小");
                        qrScale = 0;
                        break;
                }
            }
        });

        if (isAdded()) refreshQrCode();
    }

    @SuppressLint("SetTextI18n")
    public void refreshQrCode() {
        CenterThreadPool.run(() -> {
            try {
                CenterThreadPool.runOnUiThread(() -> scanStat.setText("正在获取二维码"));

                CookiesApi.checkCookies();
                QRImage = LoginApi.getLoginQR();
                CookiesApi.activeCookieInfo();

                CenterThreadPool.runOnUiThread(() -> {
                    Log.e("debug-image", QRImage.getWidth() + "," + QRImage.getHeight());
                    qrImageView.setImageBitmap(QRImage);
                    startLoginDetect();
                });
            } catch (IOException e) {
                CenterThreadPool.runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("获取二维码失败，网络错误");
                });
                e.printStackTrace();
            } catch (JSONException e) {
                CenterThreadPool.runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("登录接口可能失效，请找开发者");
                });
                e.printStackTrace();
            } catch (Exception e){
                CenterThreadPool.runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("遇到其他错误：\n" + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    public void startLoginDetect() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    Response response = LoginApi.getLoginState();
                    assert response.body() != null;
                    if(!isAdded()) {
                        this.cancel();
                        return;
                    }

                    JSONObject loginJson = new JSONObject(response.body().string());

                    int code = loginJson.getJSONObject("data").getInt("code");
                    switch (code) {
                        case 86090:
                            CenterThreadPool.runOnUiThread(() -> scanStat.setText("已扫描，请在手机上点击登录"));
                            break;
                        case 86101:
                            CenterThreadPool.runOnUiThread(() -> scanStat.setText("请使用官方手机端哔哩哔哩扫码登录\n点击二维码可以进行放大和缩小"));
                            break;
                        case 86038:
                            CenterThreadPool.runOnUiThread(() -> {
                                scanStat.setText("二维码已失效，点击上方重新获取");
                                qrImageView.setEnabled(true);
                            });
                            this.cancel();
                            break;
                        case 0:
                            this.cancel();
                            CenterThreadPool.runOnUiThread(()->scanStat.setText("正在处理登录……"));
                            String cookies = SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "");

                            SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token, loginJson.getJSONObject("data").getString("refresh_token"));

                            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.cookie_refresh, true);

                            Log.d("debug-login-cookies", cookies);
                            Log.e("debug-refresh-token", SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""));

                            InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
                            if (instance != null && !instance.isDestroyed()) instance.finish();

                            NetWorkUtil.refreshHeaders();

                            int activeResult = CookiesApi.activeCookieInfo();
                            if (activeResult != 0) MsgUtil.showMsg("警告：激活Cookies失败");
                            LoginApi.requestSSOs();
                            if (loginJson.getJSONObject("data").has("url")) {
                                try {
                                    NetWorkUtil.get(loginJson.getJSONObject("data").optString("url"));
                                } catch (Throwable ignored) {
                                }
                            }

                            startActivity(new Intent(requireContext(), SplashActivity.class));

                            if (isAdded()) requireActivity().finish();
                            break;
                        default:
                            CenterThreadPool.runOnUiThread(() -> scanStat.setText("二维码登录API可能变动，\n但你仍然可以尝试扫码登录。\n建议反馈给开发者"));
                            break;
                    }

                } catch (Exception e) {
                    if (isAdded()) CenterThreadPool.runOnUiThread(() -> {
                        qrImageView.setEnabled(true);
                        scanStat.setText("无法获取二维码信息，点击上方重试\n" + e.getMessage());
                        MsgUtil.err(e);
                    });
                    this.cancel();
                }
            }
        }, 500, 1000);
    }
}
