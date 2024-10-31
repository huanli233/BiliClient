package com.RobinNotBad.BiliClient.util;

import androidx.core.util.Pair;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import java.util.Stack;

public class TerminalContext {
    enum DetailType {
        None,
        Video,
        Article,
        Dynamic,
    }

    private DetailType currentDetailType = DetailType.None;
    private Object source;
    private Object forwardContent;
    /**
     * 详情页以及对应数据对象的存储， 每进入一个页面，例如动态，动态点击进入视频， 视频下面有个专栏
     * 然后再返回，此时的逻辑就是像栈一样。
     */
    private final Stack<Pair<DetailType, Object>> stateStack;

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

    public void enterVideoDetailPage(VideoInfo videoInfo) {
        currentDetailType = DetailType.Video;
        this.source = videoInfo;
        stateStack.push(new Pair<>(currentDetailType, source));
    }

    public void enterArticleDetailPage(ArticleInfo articleInfo) {
        currentDetailType = DetailType.Article;
        this.source = articleInfo;
        stateStack.push(new Pair<>(currentDetailType, source));
    }

    public void enterDynamicDetailPage(Dynamic dynamic) {
        currentDetailType = DetailType.Dynamic;
        this.source = dynamic;
        stateStack.push(new Pair<>(currentDetailType,source));
    }

    public void leaveDetailPage() {
        stateStack.pop();
        if(stateStack.isEmpty()) {
            currentDetailType = DetailType.None;
            source = null;
        } else {
            Pair<DetailType, Object> previousState = stateStack.peek();
            currentDetailType = previousState.first;
            this.source = previousState.second;
        }
    }

    public VideoInfo getCurrentVideo() throws IllegalTerminalStateException {
        if (currentDetailType != DetailType.Video || source == null || !(source instanceof VideoInfo)) {
            throw new IllegalTerminalStateException();
        }
        return (VideoInfo) source;
    }

    public ArticleInfo getCurrentArticle() throws  IllegalTerminalStateException {
        if (currentDetailType != DetailType.Article || source == null || !(source instanceof ArticleInfo)) {
            throw new IllegalTerminalStateException();
        }
        return (ArticleInfo) source;
    }

    public Dynamic getCurrentDynamic() throws  IllegalTerminalStateException {
        if(currentDetailType != DetailType.Dynamic || source == null || !(source instanceof Dynamic)) {
            throw new IllegalTerminalStateException();
        }
        return (Dynamic) source;
    }
    public Object getSource() {
        if (currentDetailType == DetailType.None) {
            return null;
        }
        return this.source;
    }

    private static final class InstanceHolder {
        static final TerminalContext INSTANCE = new TerminalContext();
    }

    public static TerminalContext getInstance() {
        return InstanceHolder.INSTANCE;
    }
    public static class IllegalTerminalStateException extends Exception {

    }
}
