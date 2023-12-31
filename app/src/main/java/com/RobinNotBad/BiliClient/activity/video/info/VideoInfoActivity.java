package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.media.MediaInfoFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//视频详情页，但这只是个壳，瓤是VideoInfoFragment、VideoReplyFragment、VideoRcmdFragment

public class VideoInfoActivity extends BaseActivity {

    private String type = "video";
    private String bvid;
    private long aid;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        bvid = intent.getStringExtra("bvid");
        aid = intent.getLongExtra("aid",114514);
        type = intent.getStringExtra("type");
        if(type == null)
            type = "video";
        findViewById(R.id.top).setOnClickListener(view -> finish());

        ImageView loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("视频详情");

        ViewPager viewPager = findViewById(R.id.viewPager);
        
        
        Log.e("VideoInfoActivity",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
        /*
        new Thread(()->{
            try {
            	List<String> responseCookies = Objects.requireNonNull(Objects.requireNonNull(NetWorkUtil.get("https://www.bilibili.com/")).networkResponse()).headers("Set-Cookie");
                            String buvid3 = "";
                            for(int i = 0; i < responseCookies.size(); ++i) {
                            	if(responseCookies.get(i).startsWith("buvid3")) {
                            		buvid3 = responseCookies.get(i);
                            	}
                            }
                    
                            buvid3=buvid3.substring(buvid3.indexOf("buvid3"),buvid3.indexOf(";",buvid3.indexOf("buvid3"))+1);
                            Log.e("buvid3",buvid3);
            } catch(IOException err) {
            	Log.e("","buvid3获取失败");
            }
        }).start();
         */
        CenterThreadPool.run(()->{
            JSONObject data;
            try {
                VideoInfo videoInfo = null;
                // video 的获取部分先这么括起来, 因为剧集类型没法走这个获取, 改天用策略模式优化这部分代码 by silent碎月
                // TODO: 优化代码片段
                if(type.equals("video")) {
                    if (bvid == null || TextUtils.isEmpty(bvid)) data = VideoInfoApi.getJsonByAid(aid);
                    else data = VideoInfoApi.getJsonByBvid(bvid);

                    //ErrorUtil.showText(requireContext(),"调试",data.toString());

                    JSONArray tagList;
                    if (bvid == null || bvid.equals("")) tagList = VideoInfoApi.getTagsByAid(aid);
                    else tagList = VideoInfoApi.getTagsByBvid(bvid);
                    videoInfo = VideoInfoApi.getInfoByJson(data, tagList);
                }
                List<Fragment> fragmentList = createFragmentsFromVideoType(videoInfo);
                viewPager.setOffscreenPageLimit(fragmentList.size());

                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

                runOnUiThread(()->{
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(vpfAdapter);

                    if(SharedPreferencesUtil.getBoolean("first_videoinfo",true)){
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_videoinfo",false);
                    }
                });
                //没啥好说的，教科书式的ViewPager使用方法
            }catch (JSONException e){
                runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.jsonErr(e, this);
                });
                e.printStackTrace();
            }catch (IOException e){
                runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.quickErr(MsgUtil.err_net,this);
                });
                e.printStackTrace();
            }

        });
    }
    private List<Fragment> createFragmentsFromVideoType(VideoInfo videoInfo){
        //放侧滑页, 容量不多, 设置了初始值, 内存能省点, 为避免触发扩容, 以后在这里添加fragment的记得让初始容量等于塞进去的fragment数量.
        List<Fragment> fragmentList = new ArrayList<>(3);
        if(type.equals("video")){
            VideoInfoFragment viFragment = VideoInfoFragment.newInstance(videoInfo);
            fragmentList.add(viFragment);
            VideoReplyFragment vpFragment = VideoReplyFragment.newInstance(videoInfo.aid,1);
            fragmentList.add(vpFragment);
            if(SharedPreferencesUtil.getBoolean("related_enable",true)) {
                VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
                fragmentList.add(vrFragment);
            }
        }else if(type.equals("media")){
            //剧集----intent的传的aid就是media_id
            fragmentList.add(MediaInfoFragment.newInstance(aid));
        }
        return fragmentList;
    }
}