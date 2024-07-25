package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshMainActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.WatchLaterApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//稍后再看
//2023-08-17

public class WatchLaterActivity extends RefreshMainActivity {

    private int longClickPosition = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("稍后再看");
        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            try {
                ArrayList<VideoCard> videoCardList = WatchLaterApi.getWatchLaterList();
                VideoCardAdapter adapter = new VideoCardAdapter(this, videoCardList);

                adapter.setOnLongClickListener(position -> {
                    if (longClickPosition == position) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = WatchLaterApi.delete(videoCardList.get(position).aid);
                                longClickPosition = -1;
                                if (result == 0) runOnUiThread(() -> {
                                    MsgUtil.showMsg("删除成功", this);
                                    videoCardList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, videoCardList.size() - position);
                                });
                                else
                                    runOnUiThread(() -> MsgUtil.showMsg("删除失败，错误码：" + result, this));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        longClickPosition = position;
                        MsgUtil.showMsg("再次长按删除", this);
                    }
                });

                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

}