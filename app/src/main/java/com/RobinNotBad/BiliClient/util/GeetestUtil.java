package com.RobinNotBad.BiliClient.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;

import com.geetest.sdk.GT3ConfigBean;
import com.geetest.sdk.GT3ErrorBean;
import com.geetest.sdk.GT3GeetestUtils;
import com.geetest.sdk.GT3Listener;
import com.geetest.sdk.views.GT3GeetestButton;

/**
 * 极验工具类
 */
public class GeetestUtil {

    private GT3GeetestUtils mGt3GeetestUtils;
    private GT3ConfigBean gt3ConfigBean;

    private static final String TAG = "GeetestUtils";

    /**
     * 0 绑定 1 非绑定 2 一键通过
     *
     * @param activity 上下文
     * @param button   按钮
     * @param type     类型
     * @param handler  handler
     */
    public void customVerity(Activity activity, GT3GeetestButton button, int type, GeetestHandler handler) {
        // 请在oncreate方法里初始化以获取足够手势数据来保证第一轮验证成功率
        mGt3GeetestUtils = new GT3GeetestUtils(activity);
        // 配置bean文件，也可在oncreate初始化
        gt3ConfigBean = new GT3ConfigBean();
        // 设置验证模式，1：bind，2：unbind
        int pattern = type == 3 ? type - 1 : type;//type等于3-1 type=2
        gt3ConfigBean.setPattern(pattern);
        // 设置点击灰色区域是否消失，默认不消失
        gt3ConfigBean.setCanceledOnTouchOutside(false);
        // 设置加载webview超时时间，单位毫秒，默认10000，仅且webview加载静态文件超时，不包括之前的http请求
        gt3ConfigBean.setTimeout(30000);
        // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
        gt3ConfigBean.setWebviewTimeout(30000);
        // 设置回调监听
        gt3ConfigBean.setListener(new GT3Listener() {

            /**
             * 验证码加载完成
             * @param duration 加载时间和版本等信息，为json格式
             */
            @Override
            public void onDialogReady(String duration) {
                Log.e(TAG, "GT3BaseListener-->onDialogReady-->" + duration);
            }

            /**
             * 图形验证结果回调
             * @param code 1为正常 0为失败
             */
            @Override
            public void onReceiveCaptchaCode(int code) {
                Log.e(TAG, "GT3BaseListener-->onReceiveCaptchaCode-->" + code);
            }

            /**
             * 自定义api2回调
             * @param result，api2请求上传参数
             */
            @Override
            public void onDialogResult(String result) {
                Log.e(TAG, "GT3BaseListener-->onDialogResult-->" + result);
                handler.onDialogResult(mGt3GeetestUtils, result);
            }

            /**
             * 统计信息，参考接入文档
             * @param result
             */
            @Override
            public void onStatistics(String result) {
                Log.e(TAG, "GT3BaseListener-->onStatistics-->" + result);
            }

            /**
             * 验证码被关闭
             * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
             */
            @Override
            public void onClosed(int num) {
                Log.e(TAG, "GT3BaseListener-->onClosed-->" + num);

            }

            /**
             * 验证成功回调
             * @param result
             */
            @Override
            public void onSuccess(String result) {
                Log.e(TAG, "GT3BaseListener-->onSuccess-->" + result);

            }

            /**
             * 验证失败回调
             * @param errorBean 版本号，错误码，错误描述等信息
             */
            @Override
            public void onFailed(GT3ErrorBean errorBean) {
                Log.e(TAG, "GT3BaseListener-->onFailed-->" + errorBean.toString());
            }

            /**
             * 自定义api1回调
             */
            @Override
            public void onButtonClick() {
                handler.onRequestCaptcha(gt3ConfigBean);
            }
        });
        mGt3GeetestUtils.init(gt3ConfigBean);
        if (type == 1) {
            // 开启验证
            mGt3GeetestUtils.startCustomFlow();
        } else {
            button.setGeetestUtils(mGt3GeetestUtils);
        }
    }

    /**
     * 页面关闭时释放资源
     */
    public void onDestroy() {
        if (mGt3GeetestUtils != null) {
            mGt3GeetestUtils.destory();
            mGt3GeetestUtils = null;
        }
    }

    /**
     * 界面切换
     */
    public void changeDialogLayout(Configuration configuration) {
        /**
         * 设置后，界面横竖屏不会关闭验证码，推荐设置
         */
        if (mGt3GeetestUtils != null) {
            mGt3GeetestUtils.changeDialogLayout();
        }
    }

    public interface GeetestHandler {
        void onRequestCaptcha(GT3ConfigBean configBean);

        void onDialogResult(GT3GeetestUtils utils, String result);
    }
}
