package com.RobinNotBad.BiliClient.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author silent碎月
 * 核心运行线程池, 用CAS去做获取(watch端能不用synchroized就不用, 占性能)
 * 核心线程数: 1, 应对低并发
 * 最大线程数: CPU核心数 * 2, 这个数量是io密集型的最佳线程数
 * 特性: 创建的线程60s后才销毁, 期间如果有其他的runnable需要运行, 直接用已创建的线程
 */
public class CenterThreadPool {

    private static AtomicReference<ExecutorService> INSTANCE = new AtomicReference<>();
    private static ExecutorService getInstance(){
        while(INSTANCE.get() == null){
            INSTANCE.compareAndSet(null, new ThreadPoolExecutor(
                    1,
                    Runtime.getRuntime().availableProcessors() * 2,
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10)


            ));
        }
        return INSTANCE.get();
    }
    public static void run(Runnable runnable){
        getInstance().submit(runnable);
    }

// 想在这里实现一个自动切线程的网络请求一个方法,
// 但是这种方式需要json转换器, 例如Gson, Moshi的第三方库的引入
// 现在用的仍然是jsonObject做手动json转换, 先注释掉
//    public static requireNetWork<T>(String url, NetWorkUtil.Callback<T> callback){
//        CenterThreadPool.run(() -> {
//        JsonObject obj = request(url);
//        T res = Gson.fromJson(obj);
//        runOnUiThread(() -> callback.onSuccess(res));
//       });
//    }
}
