package listener;

/**
 * 上传图片回调接口
 */
public interface UploadListener {
    /**
     * 上传任务执行成功后回调
     *
     * @param response 响应
     */
    void onUploadSuccess(String response);

    /**
     * 上传任务执行失败后回调
     *
     * @param e 异常
     */
    void onUploadFailed(Exception e);
}
