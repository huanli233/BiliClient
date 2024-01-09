package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.RobinNotBad.BiliClient.activity.media.MediaInfoFragment;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.databinding.ActivitySimpleViewpagerBinding;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaDetailInfo extends DetailInfo<ActivitySimpleViewpagerBinding> {
    private final long mediaId;
    private MediaSectionInfo.EpisodeInfo currentEpisodeInfo;

    private List<Fragment> fragmentList;
    public MediaDetailInfo(AppCompatActivity activity, long mediaId) {
        super(activity);
        this.mediaId = mediaId;
    }


    @Override
    protected ActivitySimpleViewpagerBinding createViewBinding(Context context) {
        return ActivitySimpleViewpagerBinding.inflate(activity.getLayoutInflater());
    }

    @Override
    protected void initView() {
        ViewPager viewPager = binding.viewPager;
        binding.top.setOnClickListener(view -> activity.finish());
        binding.pageName.setText("视频详情");
        fragmentList = createFragmentList();
        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(activity.getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(vpfAdapter);
        if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
            Toast.makeText(activity, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.putBoolean("first_videoinfo", false);
        }
    }

    public void setCurrentEpisodeInfo(MediaSectionInfo.EpisodeInfo currentEpisodeInfo) {
        this.currentEpisodeInfo = currentEpisodeInfo;
        ((VideoReplyFragment)fragmentList.get(1)).setAid(currentEpisodeInfo.aid);
    }

    private List<Fragment> createFragmentList(){
        List<Fragment> list = new ArrayList<>(2);
        list.add(MediaInfoFragment.newInstance(mediaId));
        list.add(VideoReplyFragment.newInstance(mediaId, 1));
        return list;
    }
}
