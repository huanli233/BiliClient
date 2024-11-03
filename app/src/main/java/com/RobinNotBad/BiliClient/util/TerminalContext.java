package com.RobinNotBad.BiliClient.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.live.LiveInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.api.*;
import com.RobinNotBad.BiliClient.model.*;
import org.json.JSONObject;

import java.util.Stack;
import java.util.concurrent.Future;

public class TerminalContext {
    enum DetailType {
        None,
        Video,
        Article,
        Dynamic,
        Live,
    }

    private DetailType currentDetailType = DetailType.None;
    private LiveData<Result<Object>> source;
    private Object forwardContent;
    /**
     * 详情页以及对应数据对象的存储， 每进入一个页面，例如动态，动态点击进入视频， 视频下面有个专栏
     * 然后再返回，此时的逻辑就是像栈一样。
     */
    private final Stack<Pair<DetailType, LiveData<Result<Object>>>> stateStack;

    private TerminalContext() {
        source = null;
        stateStack = new Stack<>();
    }

    public void setForwardContent(Object forwardContent) {
        this.forwardContent = forwardContent;
    }

    public Object getForwardContent() {
        return forwardContent;
    }

    // 视频详情页跳转
    public void enterVideoDetailPage(Context context, long aid){
        enterVideoDetailPage(context, aid, null, "video", -1);
    }
    public void enterVideoDetailPage(Context context, String bvid) {
        enterVideoDetailPage(context, -1, bvid, "video", -1);
    }
    public void enterVideoDetailPage(Context context, long aid, String bvid) {
        enterVideoDetailPage(context, aid, bvid, null, -1);
    }
    public void enterVideoDetailPage(Context context, long aid, String bvid, String type) {
        enterVideoDetailPage(context, aid, bvid, type, -1);
    }
    public void enterVideoDetailPage(Context context, long aid, String bvid, String type, long seekReply) {
        // 拉取视频信息
        LiveData<Result<Object>> videoInfo = CenterThreadPool.supplyAsyncWithLiveData(() -> {
            JSONObject data;
            if (TextUtils.isEmpty(bvid)) data = VideoInfoApi.getJsonByAid(aid);
            else data = VideoInfoApi.getJsonByBvid(bvid);
            if (data == null) {
                return null;
            }
            return VideoInfoApi.getInfoByJson(data);
        });
        //放入数据源缓冲区
        currentDetailType = DetailType.Video;
        this.source = videoInfo;
        stateStack.push(new Pair<>(currentDetailType, source));

        //创建intent并填充信息
        Intent intent = new Intent(context, VideoInfoActivity.class);
        intent.putExtra("aid", aid);
        if (!TextUtils.isEmpty(bvid)) {
            intent.putExtra("bvid", bvid);
        }
        if(type != null) {
            intent.putExtra("type", type);
        }
        intent.putExtra("seekReply", seekReply);
        //启动activity
        context.startActivity(intent);
    }

    //专栏详情页跳转
    public void enterArticleDetailPage(Context context, long cvid) {
        enterArticleDetailPage(context, cvid, -1);
    }
    public void enterArticleDetailPage(Context context, long cvid, long seekReply) {
        LiveData<Result<Object>> articleInfo = CenterThreadPool.supplyAsyncWithLiveData(() -> ArticleApi.getArticle(cvid));
        currentDetailType = DetailType.Article;
        this.source = articleInfo;
        stateStack.push(new Pair<>(currentDetailType, source));
        Intent intent= new Intent(context, ArticleInfoActivity.class);
        intent.putExtra("cvid", cvid);
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }

    // 动态详情页跳转
    public void enterDynamicDetailPage(Context context, long id){
        enterDynamicDetailPage(context, id, 0, -1);
    }
    public void enterDynamicDetailPage(Context context, long id, int position) {
        enterDynamicDetailPage(context, id, position, -1);
    }
    public void enterDynamicDetailPage(Context context, long id, int position, long seekReply) {
        LiveData<Result<Object>> dynamicInfo = CenterThreadPool.supplyAsyncWithLiveData(() -> DynamicApi.getDynamic(id));
        currentDetailType = DetailType.Dynamic;
        this.source = dynamicInfo;
        stateStack.push(new Pair<>(currentDetailType, source));
        Intent intent = new Intent(context, DynamicInfoActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }
    /*
     * 由于动态有可删除的特性，部分页面依赖动态页面activity的result实现页面更新，这里加入额外的一个兼容方法
     */
    public void enterDynamicDetailPageForResult(Activity activity, long id, int position, int requestId) {
        LiveData<Result<Object>> dynamicInfo = CenterThreadPool.supplyAsyncWithLiveData(() -> DynamicApi.getDynamic(id));
        currentDetailType = DetailType.Dynamic;
        source = dynamicInfo;
        stateStack.push(new Pair<>(currentDetailType, dynamicInfo));
        Intent intent = new Intent(activity, DynamicInfoActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("position", position);
        activity.startActivityForResult(intent, requestId);
    }

    public void enterLiveDetailPage(Context context, long roomId) {
        LiveData<Result<Object>> liveInfo = CenterThreadPool.supplyAsyncWithLiveData(() -> {
            //同时下载UserInfo跟LivePlayInfo
            Future<LivePlayInfo> livePlayInfoFuture = CenterThreadPool.supplyAsyncWithFuture(()-> LiveApi.getRoomPlayInfo(roomId, 80));
            //利用future的特性让UserInfo在后面慢慢下着，同时开始下载LiveRoom，这里要等待LiveRoom下载完成。
            LiveRoom liveRoom = LiveApi.getRoomInfo(roomId);
            if(liveRoom == null) {
                return null;
            }
            //LiveRoom下载完成后下UserInfo
            UserInfo userInfo = UserInfoApi.getUserInfo(liveRoom.uid);
            LivePlayInfo playInfo = livePlayInfoFuture.get();
            return new LiveInfo(userInfo, liveRoom, playInfo);
        });
        currentDetailType = DetailType.Live;
        source = liveInfo;
        stateStack.push(new Pair<>(currentDetailType, liveInfo));

        Intent intent = new Intent(context, LiveInfoActivity.class);
        intent.putExtra("room_id", roomId);
        context.startActivity(intent);
    }

    public void leaveDetailPage() {
        if (stateStack.isEmpty()) {
           currentDetailType = DetailType.None;
           source = null;
           return;
        }
        stateStack.pop();
        if(stateStack.isEmpty()) {
            currentDetailType = DetailType.None;
            source = null;
        } else {
            Pair<DetailType, LiveData<Result<Object>>> previousState = stateStack.peek();
            currentDetailType = previousState.first;
            this.source = previousState.second;
        }
    }
    public LiveData<Result<VideoInfo>> getCurrentVideoLiveData() {
        if(currentDetailType != DetailType.Video || source == null) {
            return new MutableLiveData<>(Result.failure(new IllegalTerminalStateException()));
        }
        return (LiveData) source;
    }

    public VideoInfo getCurrentVideoInfo() throws IllegalTerminalStateException {
        if (currentDetailType != DetailType.Video || source == null) {
            throw new IllegalTerminalStateException();
        }
        return resultSafeUnPack(source.getValue(), VideoInfo.class);
    }

    public LiveData<Result<ArticleInfo>> getCurrentArticleLiveData() {
        if (currentDetailType != DetailType.Article || source == null) {
            return new MutableLiveData<>(Result.failure(new IllegalTerminalStateException()));
        }
        return (LiveData) source;
    }
    public ArticleInfo getCurrentArticleInfo() throws  IllegalTerminalStateException {
        if (currentDetailType != DetailType.Article || source == null) {
            throw new IllegalTerminalStateException();
        }
        return resultSafeUnPack(source.getValue(), ArticleInfo.class);
    }

    public LiveData<Result<Dynamic>> getCurrentDynamicLiveData() {
        if (currentDetailType != DetailType.Dynamic || source == null) {
            return new MutableLiveData<>(Result.failure(new IllegalTerminalStateException()));
        }
        return (LiveData) source;
    }

    public Dynamic getCurrentDynamic() throws  IllegalTerminalStateException {
        if(currentDetailType != DetailType.Dynamic || source == null) {
            throw new IllegalTerminalStateException();
        }
        return resultSafeUnPack(source.getValue(), Dynamic.class);
    }

    public LiveData<Result<LiveInfo>> getCurrentLiveInfoLiveData() {
        if (currentDetailType != DetailType.Live || source == null) {
            return new MutableLiveData<>(Result.failure(new IllegalTerminalStateException()));
        }
        return (LiveData) source;
    }

    public LiveInfo getCurrentLiveInfo() throws IllegalTerminalStateException {
        if(currentDetailType != DetailType.Live || source == null) {
            throw new IllegalTerminalStateException();
        }
        return resultSafeUnPack(source.getValue(), LiveInfo.class);
    }

    public Object getSource() {
        if (currentDetailType == DetailType.None) {
            return null;
        }
        return resultSafeUnPack(source.getValue(), Object.class);
    }

    private static final class InstanceHolder {
        static final TerminalContext INSTANCE = new TerminalContext();
    }

    public static TerminalContext getInstance() {
        return InstanceHolder.INSTANCE;
    }
    private <T> T resultSafeUnPack(Result<Object> result, Class<T> type) {
        if (result == null) {
            return null;
        }
        Object value = result.getOrNull();
        if (value == null){
            return null;
        }
        try {
            if(type.isInstance(value)) {
                return (T) value;
            } else {
                return null;
            }
        }catch (Throwable e) {
            return null;
        }
    }
    public static class IllegalTerminalStateException extends Exception {

    }
}
