package com.RobinNotBad.BiliClient.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author silent碎月
 * 核心运行线程池, 用CAS去做获取(watch端能不用synchroized就不用, 占性能)
 * 现在先使用cachedThraedPool
 * 特性: 创建的线程60s后才销毁, 期间如果有其他的runnable需要运行, 直接用已创建的线程
 */
public class CenterThreadPool {
    private static AtomicReference<ExecutorService> INSTANCE;
    private static ExecutorService getInstance(){
        while(INSTANCE.get() == null){
            INSTANCE.compareAndSet(null, Executors.newCachedThreadPool());
        }
        return INSTANCE.get();
    }
    public static void run(Runnable runnable){
        getInstance().submit(runnable);
    }
}
