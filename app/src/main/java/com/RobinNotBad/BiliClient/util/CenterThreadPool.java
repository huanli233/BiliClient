package com.RobinNotBad.BiliClient.util;

import android.os.Build;
import android.os.Handler;

import android.os.Looper;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.RobinNotBad.BiliClient.BiliTerminal;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.*;

/**
 * @author silent碎月
 * 核心运行线程池
 * BuildersKt.launch 系列调用可以在java端调起协程,更加轻量
 */
public class CenterThreadPool {

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static CoroutineScope COROUTINE_SCOPE;
    private static AtomicReference<ExecutorService> threadPool;

    private static ExecutorService getThreadPoolInstance() {
        if(threadPool == null) return null;
        int bestThreadPoolSize = Runtime.getRuntime().availableProcessors();
        while (threadPool.get() == null) {
            threadPool.compareAndSet(null, new ThreadPoolExecutor(
                    bestThreadPoolSize / 2,
                    bestThreadPoolSize * 2,
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(20)
            ));
        }
        return threadPool.get();
    }

    private CenterThreadPool() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            COROUTINE_SCOPE = null;
            threadPool = new AtomicReference<>();
        } else {
            COROUTINE_SCOPE = CoroutineScopeKt.CoroutineScope((CoroutineContext) Dispatchers.getIO());
            threadPool = null;
        }
    }


    /**
     * 在后台运行, 用于网络请求等耗时操作
     *
     * @param runnable 要运行的任务
     */
    public static void run(Runnable runnable) {
        try {
            //能用协程用协程
            if (COROUTINE_SCOPE != null) {
                BuildersKt.launch(COROUTINE_SCOPE, EmptyCoroutineContext.INSTANCE, CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation<? super Unit> continuation) -> {
                    runnable.run();
                    return Unit.INSTANCE;
                });
                //协程不可用时尝试以原生线程池运行
            } else if (getThreadPoolInstance() != null) {
                getThreadPoolInstance().submit(runnable);
            } else {
                //都不可用再开线程
                new Thread(runnable).start();
            }
        } catch (Throwable e) {
            //最后再放手一博
            new Thread(runnable).start();
        }
    }

    /**
     * 在后台运行, 用于网络请求等耗时操作, 有返回值,
     * 在fragment, activity等位置使用LiveData.observe()获取返回值, 会自动切到主线程,不需要再runOnUiThread().
     *
     * @param supplier 要运行的任务
     * @param <T>      返回值类型
     * @return LiveData包装的返回值
     */
    public static <T> LiveData<Result<T>> supplyAsyncWithLiveData(Callable<T> supplier) {
        MutableLiveData<Result<T>> retval = new MutableLiveData<>();
        CenterThreadPool.run(() -> {
            try {
                T res = supplier.call();
                retval.postValue(Result.success(res));
            } catch (Throwable e) {
                retval.postValue(Result.failure(e));
                MsgUtil.err(e, BiliTerminal.context);
            }
        });
        return retval;
    }

    /**
     * 在后台运行， 有返回值
     * 使用 CenterThreadPool.observe方法对返回值进行观察
     *
     * @param supplier 一个带返回值的lambda表达式或Supplier的实现类
     * @param <T>      返回值类型
     * @return 返回一个可供CenterThreadPool观察的Future对象
     */
    public static <T> Future<T> supplyAsyncWithFuture(Callable<T> supplier) {
        FutureTask<T> ftask = new FutureTask<>(supplier);
        CenterThreadPool.run(ftask);
        return ftask;
    }

    /**
     * 对Deferred 对象进行观察， 无需切换线程， 自动在ui线程进行观察
     *
     * @param deferred 一个将要在未来返回一个 T 类型对象的对象
     * @param consumer 对T进行观察的lambda表达式或者类
     * @param <T>      要观察的类型
     */
    public static <T> void observe(Future<T> deferred, Consumer<T> consumer) {
        CenterThreadPool.run(() -> {
            try {
                T value = deferred.get();
                CenterThreadPool.runOnUiThread(() -> consumer.accept(value));
            } catch (Throwable ignored) {
            }
        });
    }


    public static <T> void observe(Future<T> future, Consumer<T> consumer, Consumer<Throwable> onFailure) {
        CenterThreadPool.run(() -> {
            try {
                T value = future.get();
                CenterThreadPool.runOnUiThread(() -> consumer.accept(value));
            } catch (Exception e) {
                onFailure.accept(e);
            }
        });
    }

    /**
     * 在主线程运行, 用于更新UI, 例如Toast, Snackbar等
     *
     * @param runnable 要运行的任务
     */
    public static void runOnUiThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
    public static void runOnUIThreadAfter(Long time, TimeUnit unit, Runnable runnable) {
        long millis = TimeUnit.MILLISECONDS.convert(time, unit);
        mainThreadHandler.postDelayed(runnable, millis);
    }

}