package com.RobinNotBad.BiliClient.util;

import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoInfo;

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

    public void setForwardContent(Object forwardContent) {
        this.forwardContent = forwardContent;
    }

    public Object getForwardContent() {
        return forwardContent;
    }

    public void enterVideoDetailPage(VideoInfo videoInfo) {
        currentDetailType = DetailType.Video;
        this.source = videoInfo;
    }

    public void enterArticleDetailPage(ArticleInfo articleInfo) {
        currentDetailType = DetailType.Article;
        this.source = articleInfo;
    }

    public void enterDynamicDetailPage(Dynamic dynamic) {
        currentDetailType = DetailType.Dynamic;
        this.source = dynamic;
    }

    public void leaveDetailPage() {
        currentDetailType = DetailType.None;
        this.source = null;
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

    private static TerminalContext INSTANCE;
    public static TerminalContext getInstance() {
        if (INSTANCE == null) {
            synchronized (TerminalContext.class) {
                if(INSTANCE == null) {
                    INSTANCE = new TerminalContext();
                }
            }
        }
        return INSTANCE;
    }
    public class IllegalTerminalStateException extends Exception {

    }
}
