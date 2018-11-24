package listener;

/**
 * Http请求回调接口
 */
public interface HttpListener {
    /**
     * Post请求执行完成后回调
     *
     * @param response 响应
     */
    public void onPostSuccess(String response);
}
