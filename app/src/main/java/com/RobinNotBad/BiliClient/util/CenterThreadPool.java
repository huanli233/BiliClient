package com.RobinNotBad.BiliClient.util;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.RobinNotBad.BiliClient.BiliTerminal;
import kotlin.Unit;
import kotlin.coroutines.*;
import kotlinx.coroutines.*;

import java.util.concurrent.*;

/**
 * @author silent碎月
 * 核心运行线程池
 * BuildersKt.launch 系列调用可以在java端调起协程,更加轻量
 */
public class CenterThreadPool {

    private CenterThreadPool(){}

    private static final CoroutineScope COROUTINE_SCOPE = CoroutineScopeKt.CoroutineScope((CoroutineContext) Dispatchers.getIO());

    /**
     * 在后台运行, 用于网络请求等耗时操作
     *
     * @param runnable 要运行的任务
     */
    public static void run(Runnable runnable) {
        //先将实现切换到协程上，在测试版看看，如果有崩溃，麻烦注释掉以下代码，并恢复原有线程池启动。
        BuildersKt.launch(COROUTINE_SCOPE, EmptyCoroutineContext.INSTANCE, CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation<? super Unit> continuation) -> {
            runnable.run();
            return Unit.INSTANCE;
        });
    }

    /**
     * 在后台运行, 用于网络请求等耗时操作, 有返回值,
     * 在fragment, activity等位置使用LiveData.observe()获取返回值, 会自动切到主线程,不需要再runOnUiThread().
     *
     * @param supplier 要运行的任务
     * @param <T>      返回值类型
     * @return LiveData包装的返回值
     */
    public static <T> LiveData<T> supplyAsyncWithLiveData(Callable<T> supplier) {
        MutableLiveData<T> retval = new MutableLiveData<>();
        CenterThreadPool.run(() -> {
            try {
                T res = supplier.call();
                retval.postValue(res);
            }catch (Exception e){
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
        BuildersKt.launch(COROUTINE_SCOPE, (CoroutineContext) Dispatchers.getMain(), CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation<? super Unit> continuation) -> {
            runnable.run();
            return Unit.INSTANCE;
        });
    }

}