package listener;

/**
 * 线程池监听器
 */
public interface ThreadPoolListener {
    /**
     * 当线程池执行完毕时回调
     *
     * @param successCount 上传成功数量
     * @param failCount    上传失败数量
     */
    void onThreadPoolFinished(int successCount, int failCount);
}
