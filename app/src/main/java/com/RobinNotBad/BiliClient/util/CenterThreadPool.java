package com.RobinNotBad.BiliClient.util;

import androidx.core.util.Consumer;
import androidx.core.util.Supplier;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bumptech.glide.util.Executors;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author silent碎月
 * 核心运行线程池, 最大线程容量为CPU核心数的2倍, 最小线程容量为1, 线程空闲时间为60s, 线程队列容量为20
 * 注释掉的代码是由于当前第三方库的import不是很满足所以注掉了
 * BuildersKt.launch 系列调用当引入kotlin协程库后可用于替代当前线程池的设计， 可以在java端调起协程
 */
public class CenterThreadPool {


    private static final AtomicReference<ExecutorService> INSTANCE = new AtomicReference<>();
    private static ExecutorService getInstance(){
        while(INSTANCE.get() == null){
            INSTANCE.compareAndSet(null, new ThreadPoolExecutor(
                    1,
                    Runtime.getRuntime().availableProcessors() * 2,
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(20)


            ));
        }
        return INSTANCE.get();
    }



    /**
     * 在后台运行, 用于网络请求等耗时操作
     * @param runnable 要运行的任务
     */
    public static void run(Runnable runnable){
      /*  BuildersKt.launch(INSTANCE, Dispatchers.getIO(), CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation continuation) -> {
            runnable.run();
            return Unit.INSTANCE;
        });*/
        getInstance().execute(runnable);
    }

    /**
     * 在后台运行, 用于网络请求等耗时操作, 有返回值,
     * 在fragment, activity等位置使用LiveData.observe()获取返回值, 会自动切到主线程,不需要再runOnUiThread().
     * @param supplier 要运行的任务
     * @return LiveData包装的返回值
     * @param <T> 返回值类型
     */
    public static <T> LiveData<T> supplyAsyncWithLiveData(Supplier<T> supplier) {
        MutableLiveData<T> retval = new MutableLiveData<>();
        /*BuildersKt.launch(INSTANCE, Dispatchers.getIO(), CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation continuation) -> {
            T res = supplier.get();
            retval.postValue(res);
            return Unit.INSTANCE;
        });*/
        getInstance().execute(() -> {
            T res = supplier.get();
            retval.postValue(res);
        });
        return retval;
    }

    /**
     * 在后台运行， 有返回值
     * 使用 CenterThreadPool.observe方法对返回值进行观察
     * @param supplier 一个带返回值的lambda表达式或Supplier的实现类
     * @return 返回一个可供CenterThreadPool观察的Future对象
     * @param <T> 返回值类型
     */
    public static <T> Future<T> supplyAsyncWithFuture(Supplier<T> supplier){
        return getInstance().submit(supplier::get);
    }

    /**
     * 对Future 对象进行观察， 无需切换线程， 自动在ui线程进行观察
     * @param future 一个将要在未来返回一个 T 类型对象的对象
     * @param consumer 对T进行观察的lambda表达式或者类
     * @param <T> 要观察的类型
     */
    public static <T> void observe(Future<T> future, Consumer<T> consumer){
        getInstance().execute(() -> {
            try {
                T res = future.get();
                CenterThreadPool.runOnUiThread(() -> { consumer.accept(res); });
            }catch(Exception ignored){
            }
        });
    }


    public static <T> void observe(Future<T> future, Consumer<T> consumer, Consumer<Throwable> onFailure){
        getInstance().execute(() -> {
            try {
                T res = future.get();
                CenterThreadPool.runOnUiThread(() -> { consumer.accept(res); });
            }catch(Exception e){
                onFailure.accept(e);
            }
        });
    }

    /**
     * 在主线程运行, 用于更新UI, 例如Toast, Snackbar等
     * @param runnable 要运行的任务
     */
    public static void runOnUiThread(Runnable runnable){
       /* BuildersKt.launch(INSTANCE, Dispatchers.getMain(), CoroutineStart.DEFAULT, (CoroutineScope scope, Continuation continuation) -> {
            runnable.run();
            return Unit.INSTANCE;
        });*/
        Executors.mainThreadExecutor().execute(runnable);
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
