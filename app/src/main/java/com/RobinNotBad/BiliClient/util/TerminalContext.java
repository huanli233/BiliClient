package com.RobinNotBad.BiliClient.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.live.LiveInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.api.*;
import com.RobinNotBad.BiliClient.model.*;

import java.util.concurrent.Future;

/**
 * @author silent碎月
 * @date 2024/11/03
 * 哔哩终端的中央上下文，所有不方便传的
 * 希望在任何地方都能拿得到，不想再额外建工具类的话
 * 扔这里就好，这里是屎山的集中地
 * 所有的工具类，也可以往这里扔一个实现
 */
public class TerminalContext {

    //要转发的东西的数据源
    private Object forwardContent;
    /**
     * 详情页以及对应数据对象的存储， 每进入一个页面，例如动态，动态点击进入视频， 视频下面有个专栏
     * 然后再返回，此时的逻辑就是像栈一样。
     */
    private final LruCache<String, Object> contentLruCache;

    private TerminalContext() {
        contentLruCache = new LruCache<>(10);
    }

    // ------------------------转发功能数据源上下文 start-------------------------------
    public void setForwardContent(Object forwardContent) {
        this.forwardContent = forwardContent;
    }

    public Object getForwardContent() {
        return forwardContent;
    }
    //-------------------------转发功能数据源上下文 end ----------------------------------

    // --------------------------详情页跳转功能 start  ----------------------------------
    // 视频详情页跳转
    public void enterVideoDetailPage(Context context, long aid) {
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
        //创建intent并填充信息
        Intent intent = new Intent(context, VideoInfoActivity.class);
        intent.putExtra("aid", aid);
        if (!TextUtils.isEmpty(bvid)) {
            intent.putExtra("bvid", bvid);
        }
        if (type != null) {
            intent.putExtra("type", type);
        }
        intent.putExtra("seekReply", seekReply);
        //启动activity
        context.startActivity(intent);
    }

    private Result<VideoInfo> fetchVideoInfoByAid(long aid, boolean saveToCache) {
        VideoInfo videoInfo;
        try {
            Logu.v("aid");
            videoInfo = VideoInfoApi.getVideoInfo(aid);
            if (videoInfo != null) {
                if (saveToCache) {
                    contentLruCache.put(ContentType.Video.getTypeCode() + "_" + aid, videoInfo);
                }
                return Result.success(videoInfo);
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
        return Result.failure(new IllegalTerminalStateException("video object is null"));
    }

    private Result<VideoInfo> fetchVideoInfoByBvId(String bvid, boolean saveToCache) {
        VideoInfo videoInfo;
        try {
            Logu.v("bvid");
            videoInfo = VideoInfoApi.getVideoInfo(bvid);
            if (videoInfo != null) {
                if (saveToCache) {
                    contentLruCache.put(ContentType.Video.getTypeCode() + "_" + videoInfo.aid, videoInfo);
                }
                return Result.success(videoInfo);
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
        return Result.failure(new IllegalTerminalStateException("video object is null"));
    }

    private Result<VideoInfo> fetchVideoInfoByAidOrBvId(long aid, String bvid, boolean saveToCache) {
        if (aid != 0) {
            return fetchVideoInfoByAid(aid, saveToCache);
        } else {
            return fetchVideoInfoByBvId(bvid, saveToCache);
        }
    }

    //专栏详情页跳转
    public void enterArticleDetailPage(Context context, long cvid) {
        enterArticleDetailPage(context, cvid, -1);
    }

    public void enterArticleDetailPage(Context context, long cvid, long seekReply) {
        Intent intent = new Intent(context, ArticleInfoActivity.class);
        intent.putExtra("cvid", cvid);
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }

    private Result<ArticleInfo> fetchArticleInfo(long cvid, boolean saveToCache) {
        try {
            ArticleInfo article = ArticleApi.getArticle(cvid);
            if (article != null && saveToCache) {
                contentLruCache.put(ContentType.Article.getTypeCode() + "_" + cvid, article);
            }
            return Result.success(article);
        } catch (Exception t) {
            return Result.failure(t);
        }
    }

    // 动态详情页跳转
    public void enterDynamicDetailPage(Context context, long id) {
        enterDynamicDetailPage(context, id, 0, -1);
    }

    public void enterDynamicDetailPage(Context context, long id, int position) {
        enterDynamicDetailPage(context, id, position, -1);
    }

    public void enterDynamicDetailPage(Context context, long id, int position, long seekReply) {
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
        Intent intent = new Intent(activity, DynamicInfoActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("position", position);
        activity.startActivityForResult(intent, requestId);
    }

    private Result<Dynamic> fetchDynamic(long id, boolean saveToCache) {
        try {
            Dynamic dynamic = DynamicApi.getDynamic(id);
            if (saveToCache) {
                contentLruCache.put(ContentType.Dynamic.getTypeCode() + "_" + id, dynamic);
            }
            return Result.success(dynamic);
        } catch (Exception t) {
            return Result.failure(t);
        }
    }

    /**
     * 进行一个直播详情页的启动
     *
     * @param context Android上下文对象
     * @param roomId  直播房间号
     */
    public void enterLiveDetailPage(Context context, long roomId) {
        Intent intent = new Intent(context, LiveInfoActivity.class);
        intent.putExtra("room_id", roomId);
        context.startActivity(intent);
    }

    public Result<LiveInfo> fetchLiveInfo(long roomId, boolean saveToCache) {
        Future<LivePlayInfo> livePlayInfoFuture = CenterThreadPool.supplyAsyncWithFuture(() -> LiveApi.getRoomPlayInfo(roomId, 80));
        //利用future的特性让UserInfo在后面慢慢下着，同时开始下载LiveRoom，这里要等待LiveRoom下载完成。
        try {
            LiveRoom liveRoom = LiveApi.getRoomInfo(roomId);
            if (liveRoom == null) {
                return Result.failure(new IllegalTerminalStateException("liveRoom is null"));
            }
            //LiveRoom下载完成后下UserInfo
            UserInfo userInfo = UserInfoApi.getUserInfo(liveRoom.uid);
            LivePlayInfo playInfo = livePlayInfoFuture.get();
            LiveInfo liveInfo = new LiveInfo(userInfo, liveRoom, playInfo);
            if (saveToCache) {
                contentLruCache.put(ContentType.Live.getTypeCode() + "_" + roomId, liveInfo);
            }
            return Result.success(liveInfo);
        } catch (Exception t) {
            return Result.failure(t);
        }
    }
    // ---------------------------详情页跳转功能 end---------------------------------------

    /**
     * 退出详情页的调用，所有启动详情页的Activity中需要再onDestroy的回调中调用该方法，释放自己的上下文对象
     */
    public void leaveDetailPage() {
    }

    public Result<Reply> fetchReply(ContentType contentType, long contentId, long replyId, boolean saveToCache) {
        Result<Reply> replyResult = ReplyApi.getRootReply(contentType, contentId, replyId);
        if (replyResult.isSuccess() && saveToCache) {
            Reply reply = replyResult.getOrNull();
            if (reply != null) {
                contentLruCache.put(contentType.getTypeCode() + "_" + contentId + "_" + replyId, reply);
            }
        }
        return replyResult;
    }

    // ---------------------------- 数据源上下文 ----------------------------------------
    public LiveData<Result<VideoInfo>> getVideoInfoByAidOrBvId(long aid, String bvid) {
        String key;
        if (aid != 0) {
            key = ContentType.Video.getTypeCode() + "_" + aid;
        } else {
            key = ContentType.Video.getTypeCode() + "_" + bvid;
        }
        Object obj = contentLruCache.get(key);
        if (!(obj instanceof VideoInfo)) {
            return CenterThreadPool.supplyAsyncWithLiveData(() -> fetchVideoInfoByAidOrBvId(aid, bvid, true).getOrThrow());
        } else {
            return new MutableLiveData<>(Result.success((VideoInfo) obj));
        }
    }

    public LiveData<Result<ArticleInfo>> getArticleInfoByCvId(long cvid) {
        String key = ContentType.Article.getTypeCode() + "_" + cvid;
        Object obj = contentLruCache.get(key);
        if (!(obj instanceof ArticleInfo)) {
            return CenterThreadPool.supplyAsyncWithLiveData(() -> fetchArticleInfo(cvid, true).getOrThrow());
        } else {
            return new MutableLiveData<>(Result.success((ArticleInfo) obj));
        }
    }

    public LiveData<Result<Dynamic>> getDynamicById(long id) {
        String key = ContentType.Dynamic.getTypeCode() + "_" + id;
        Object obj = contentLruCache.get(key);
        if (!(obj instanceof Dynamic)) {
            return CenterThreadPool.supplyAsyncWithLiveData(() -> fetchDynamic(id, true).getOrThrow());
        } else {
            return new MutableLiveData<>(Result.success((Dynamic) obj));
        }
    }

    public LiveData<Result<LiveInfo>> getLiveInfoByRoomId(long roomId) {
        String key = ContentType.Live.getTypeCode() + "_" + roomId;
        Object obj = contentLruCache.get(key);
        if (!(obj instanceof LiveInfo)) {
            return CenterThreadPool.supplyAsyncWithLiveData(() -> fetchLiveInfo(roomId, true).getOrThrow());
        } else {
            return new MutableLiveData<>(Result.success((LiveInfo) obj));
        }
    }
    public LiveData<Result<Reply>> getReply(ContentType contentType, long contentId, long replyId) {
        String key = contentType.getTypeCode() + "_" + contentId + "_" + replyId;
        Object obj = contentLruCache.get(key);
        if(obj instanceof Reply) {
            return new MutableLiveData<>(Result.success((Reply) obj));
        } else {
            return CenterThreadPool.supplyAsyncWithLiveData(() -> fetchReply(contentType, contentId, replyId, true).getOrThrow());
        }
    }


    public String getTerminalKey(Object item) {
        if (item instanceof VideoInfo) {
            VideoInfo videoInfo = (VideoInfo) item;
            if (TextUtils.isEmpty(videoInfo.bvid)) {
                return ContentType.Video.getTypeCode() + "_" + videoInfo.aid;
            } else {
                return ContentType.Video.getTypeCode() + "_" + videoInfo.bvid;
            }
        } else if (item instanceof ArticleInfo) {
            return ContentType.Article.getTypeCode() + "_" + ((ArticleInfo) item).id;
        } else if (item instanceof Dynamic) {
            return ContentType.Dynamic.getTypeCode() + "_" + ((Dynamic) item).dynamicId;
        } else if (item instanceof LiveInfo) {
            return ContentType.Live.getTypeCode() + "_" + ((LiveInfo) item).getLiveRoom().roomid;
        } else if (item instanceof Reply) {
            Reply reply = (Reply) item;
        }
        return null;
    }
    // ------------------------- 数据源上下文 end ------------------------------------


    //----------------------------------私有函数区----------------------------------------
    private static final class InstanceHolder {
        static final TerminalContext INSTANCE = new TerminalContext();
    }

    public static TerminalContext getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static class IllegalTerminalStateException extends Exception {
        private final String description;

        public IllegalTerminalStateException() {
            description = "";
        }

        public IllegalTerminalStateException(String description) {
            this.description = description;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public String getMessage() {
            return this.description;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public String getLocalizedMessage() {
            return this.description;
        }
    }
}
