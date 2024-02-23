package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.SwitchCompat;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

//选择播放器

public class SettingPlayerChooseActivity extends BaseActivity {

    String player = SharedPreferencesUtil.getString("player","null");
    MaterialCardView clientPlayer,mtvPlayer,aliangPlayer;
    SwitchCompat sw_highres;
    ArrayList<MaterialCardView> cardViewList;
    int checkPosition = -1;
    final String[] playerList = {"null","clientPlayer","mtvPlayer","aliangPlayer"};
    
    private boolean just_create = true;


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_player_choose);
        Log.e("debug","选择播放器");

        clientPlayer = findViewById(R.id.clientPlayer);
        mtvPlayer = findViewById(R.id.mtvPlayer);
        aliangPlayer = findViewById(R.id.aliangPlayer);
        sw_highres = findViewById(R.id.high_res);

        sw_highres.setChecked(SharedPreferencesUtil.getBoolean("high_res",false));

        cardViewList = new ArrayList<>();
        cardViewList.add(clientPlayer);
        cardViewList.add(mtvPlayer);
        cardViewList.add(aliangPlayer);

        switch (player){
            case "clientPlayer":
                setChecked(0);
                break;

            case "mtvPlayer":
                setChecked(1);
                break;

            case "aliangPlayer":
                setChecked(2);
                break;

            default:
                break;
        }

        setOnClick();
        clientPlayer.setOnLongClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this,SettingPlayerInsideActivity.class);
            startActivity(intent);
            return true;
        });
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onDestroy() {
        SharedPreferencesUtil.putString("player",playerList[checkPosition+1]);
        SharedPreferencesUtil.putBoolean("high_res",sw_highres.isChecked());
        Log.e("debug-选择",playerList[checkPosition+1]);

        super.onDestroy();
    }
    
    private void setOnClick(){
        for (int i = 0; i < cardViewList.size(); i++) {
            int finalI = i;
            cardViewList.get(i).setOnClickListener(view -> {
                setChecked(finalI);
                Log.e("debug","点击了"+finalI);
            });
        }
    }

    private void setChecked(int position){
        checkPosition = position;
        for (int i = 0; i < cardViewList.size(); i++) {
            if(position==i) {
                cardViewList.get(i).setStrokeColor(getResources().getColor(R.color.pink));
                cardViewList.get(i).setStrokeWidth(LittleToolsUtil.dp2px(1,this));
            }
            else{
                cardViewList.get(i).setStrokeColor(getResources().getColor(R.color.gray));
                cardViewList.get(i).setStrokeWidth(LittleToolsUtil.dp2px(0.1f,this));
            }
        }
        if(!just_create) switch(playerList[checkPosition+1]){
            case "clientPlayer":
                if(SharedPreferencesUtil.getBoolean("player_inside_firstchoose",true)) {
                    SharedPreferencesUtil.putBoolean("player_inside_firstchoose",false);
                    Intent intent = new Intent();
                    intent.setClass(this,SettingPlayerInsideActivity.class);
                    startActivity(intent);
                }
                break;
            case "mtvPlayer":
                if(Build.VERSION.SDK_INT <= 19) MsgUtil.showDialog(this,"提醒","您的安卓版本过低，请使用内置播放器或QQ群中提供的改版小电视播放器",-1,false,0);
                break;
            
            case "aliangPlayer":
                if(Build.VERSION.SDK_INT <= 19) MsgUtil.showDialog(this,"提醒","您的安卓版本过低，可能无法使用凉腕播放器，可以使用内置播放器或QQ群中提供的改版小电视播放器",-1,false,0);
                break;

            default:
                break;
        }
        just_create = false;
    }
}