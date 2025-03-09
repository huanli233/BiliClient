package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Map;

//选择播放器

public class SettingPlayerChooseActivity extends BaseActivity {

    final String playerCurr = SharedPreferencesUtil.getString("player", "null");
    MaterialCardView terminalPlayer, mtvPlayer, aliangPlayer, qn_choose;
    ArrayList<MaterialCardView> cardViewList;
    int checkPosition = -1;
    final String[] playerList = {"null", "terminalPlayer", "mtvPlayer", "aliangPlayer"};

    private boolean just_create = true;


    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_player_choose, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();
            Log.e("debug", "选择播放器");

            terminalPlayer = findViewById(R.id.terminalPlayer);
            mtvPlayer = findViewById(R.id.mtvPlayer);
            aliangPlayer = findViewById(R.id.aliangPlayer);
            qn_choose = findViewById(R.id.qn_choose);

            qn_choose.setOnClickListener((view) -> handleQnChoose());

            cardViewList = new ArrayList<>();
            cardViewList.add(terminalPlayer);
            cardViewList.add(mtvPlayer);
            cardViewList.add(aliangPlayer);

            for (int i = 1; i < playerList.length; i++) {
                if (playerList[i].equals(playerCurr)) {
                    setChecked(i - 1);
                    break;
                }
            }

            setOnClick();
            terminalPlayer.setOnLongClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingTerminalPlayerActivity.class);
                startActivity(intent);
                return true;
            });

            updateQn();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQn();
    }

    private void updateQn() {
        if (findViewById(R.id.qn_tv) != null) {
            int savedVal = SharedPreferencesUtil.getInt("play_qn", 16);
            for (Map.Entry<String, Integer> entry : SettingQualityActivity.qnMap.entrySet()) {
                if (entry.getValue() == savedVal) {
                    ((TextView) findViewById(R.id.qn_tv)).setText(entry.getKey());
                    break;
                }
            }
        }
    }

    private void handleQnChoose() {
        startActivity(new Intent(this, SettingQualityActivity.class));
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onDestroy() {
        SharedPreferencesUtil.putString("player", playerList[checkPosition + 1]);
        Log.e("debug-选择", playerList[checkPosition + 1]);

        super.onDestroy();
    }

    private void setOnClick() {
        for (int i = 0; i < cardViewList.size(); i++) {
            int finalI = i;
            cardViewList.get(i).setOnClickListener(view -> {
                setChecked(finalI);
                Log.e("debug", "点击了" + finalI);
            });
        }
    }

    private void setChecked(int position) {
        checkPosition = position;
        for (int i = 0; i < cardViewList.size(); i++) {
            if (position == i) {
                cardViewList.get(i).setStrokeColor(getResources().getColor(R.color.pink));
                cardViewList.get(i).setStrokeWidth(ToolsUtil.dp2px(1));
            } else {
                cardViewList.get(i).setStrokeColor(getResources().getColor(R.color.gray));
                cardViewList.get(i).setStrokeWidth(ToolsUtil.dp2px(0.1f));
            }
        }
        if (!just_create) switch (playerList[checkPosition + 1]) {
            case "terminalPlayer":
                if (SharedPreferencesUtil.getBoolean("player_inside_firstchoose", true)) {
                    SharedPreferencesUtil.putBoolean("player_inside_firstchoose", false);
                    Intent intent = new Intent();
                    intent.setClass(this, SettingTerminalPlayerActivity.class);
                    startActivity(intent);
                }
                break;
            case "mtvPlayer":
                MsgUtil.showDialog("提醒", "不再推荐使用小电视播放器，许多功能已不再支持，推荐使用内置播放器", -1);
                break;

            case "aliangPlayer":
                if (Build.VERSION.SDK_INT <= 19)
                    MsgUtil.showDialog("提醒", "您的安卓版本过低，可能无法使用凉腕播放器，可以使用内置播放器（改版小电视不再推荐）", -1);
                break;

            default:
                break;
        }
        else just_create = false;
    }
}