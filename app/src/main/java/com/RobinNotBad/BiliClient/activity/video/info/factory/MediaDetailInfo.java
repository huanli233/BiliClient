package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.media.MediaInfoFragment;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaDetailInfo extends DetailInfo {
    private final long mediaId;
    private List<Fragment> fragmentList;
    private MediaViewPager2Adapter vpfAdapter;


    ViewPager2 viewPager;

    public MediaDetailInfo(AppCompatActivity activity, long mediaId) {
        super(activity);
        this.mediaId = mediaId;
    }
    @Override
    protected View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.activity_simple_viewpager2, null, true);
    }


    @Override
    public void initView() {
        View rootView = getRootView();
        viewPager = rootView.findViewById(R.id.viewPager);
        rootView.findViewById(R.id.top).setOnClickListener(view -> activity.finish());
        TextView pageName = rootView.findViewById(R.id.pageName);
        pageName.setText("视频详情");
        fragmentList = createFragmentList();
        viewPager.setOffscreenPageLimit(fragmentList.size());
        vpfAdapter = new MediaViewPager2Adapter(activity, mediaId, fragmentList);
        viewPager.setAdapter(vpfAdapter);
        if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
            Toast.makeText(activity, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.putBoolean("first_videoinfo", false);
        }
    }

    public void setCurrentEpisodeInfo(MediaSectionInfo.EpisodeInfo currentEpisodeInfo) {
        fragmentList.set(1, VideoReplyFragment.newInstance(currentEpisodeInfo.aid, 1));
        vpfAdapter.notifyItemChanged(1);
    }

    private List<Fragment> createFragmentList() {
        List<Fragment> list = new ArrayList<>(2);
        list.add(MediaInfoFragment.newInstance(mediaId));
        list.add(VideoReplyFragment.newInstance(mediaId, 1));
        return list;
    }

    static class MediaViewPager2Adapter extends FragmentStateAdapter {
        private List<Fragment> fragmentList;

        public MediaViewPager2Adapter(FragmentActivity fragmentActivity, long mediaId, List<Fragment> fragmentList) {
            super(fragmentActivity);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return fragmentList.get(position).hashCode();
        }
        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}