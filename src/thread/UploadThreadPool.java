package thread;

import listener.ThreadPoolListener;
import listener.UploadListener;
import net.sf.json.JSONObject;
import http.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上传图片专用线程池
 */
public class UploadThreadPool {
    /**
     * 实例对象
     */
    private static UploadThreadPool mInstance = new UploadThreadPool();

    /**
     * 线程池对象
     */
    private static ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    /**
     * 待执行的上传任务
     */
    public static List<Runnable> mTasks = new ArrayList<>();

    /**
     * 线程池是否处在繁忙状态
     */
    public static boolean isBusy = false;

    /**
     * 上传成功的数量
     */
    private static int mSuccessCount = 0;

    /**
     * 上传失败的数量
     */
    private static int mFailCount = 0;

    /**
     * 线程池监听器
     */
    private static ThreadPoolListener mThreadPoolListener = null;

    /**
     * 单例模式
     */
    private UploadThreadPool() {
    }

    /**
     * 分发实例
     *
     * @param threadPoolListener 线程池监听器
     * @return 线程池实例
     */
    public static UploadThreadPool getInstance(ThreadPoolListener threadPoolListener) {
        // 初始化
        init(threadPoolListener);
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param threadPoolListener 线程池监听器
     */
    private static void init(ThreadPoolListener threadPoolListener) {
        // 初始化数据
        mThreadPoolListener = threadPoolListener;
        mFailCount = 0;
        mSuccessCount = 0;
        isBusy = false;

        // 线程池已经被关闭，重新创建线程池
        if (mThreadPool.isShutdown()) {
            mThreadPool = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 上传图片
     *
     * @param url      服务器api
     * @param fileName 文件名
     * @param bytes    文件字节数组
     */
    public void upload(String url, String fileName, byte[] bytes) {
        // 构建上传任务
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpRequest.upload(url, fileName, bytes, new UploadListener() {
                    @Override
                    public void onUploadSuccess(String response) {
                        JSONObject jsStr = JSONObject.fromObject(response);
                        // 上传完成，统计计数
                        if (jsStr.get("result").toString() == "true") {
                            // 上传成功
                            mSuccessCount += 1;
                            System.out.println("上传成功" + mSuccessCount);
                        } else {
                            // 上传失败
                            mFailCount += 1;
                            System.out.println("上传失败" + mFailCount);
                        }

                        // 判断所有任务是否已被执行完
                        if (mTasks.size() == 0) {
                            // 任务全部被执行完，回调传参
                            mThreadPoolListener.onThreadPoolFinished(mSuccessCount, mFailCount);
                        } else {
                            // 继续执行任务
                            mThreadPool.execute(mTasks.remove(0));
                        }
                    }

                    @Override
                    public void onUploadFailed(Exception e) {
                        // 上传失败
                        mFailCount += 1;
                        System.out.println("发生异常，上传失败" + mFailCount);

                        // 判断所有任务是否已被执行完
                        if (mTasks.size() == 0) {
                            // 任务全部被执行完，回调传参
                            mThreadPoolListener.onThreadPoolFinished(mSuccessCount, mFailCount);
                        } else {
                            // 继续执行任务
                            mThreadPool.execute(mTasks.remove(0));
                        }
                    }
                });
            }
        };

        // 线程池是否未在繁忙状态
        if (isBusy) {
            // 线程池在繁忙状态，将任务加入队列
            mTasks.add(runnable);
        } else {
            // 标记线程池为繁忙状态
            isBusy = true;
            // 线程池未在繁忙状态，直接执行当前任务
            mThreadPool.execute(runnable);
        }
    }
}
