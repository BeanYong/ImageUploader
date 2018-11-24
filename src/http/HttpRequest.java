package http;

import listener.HttpListener;
import listener.UploadListener;
import okhttp3.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class HttpRequest {
    /**
     * 上传图片到服务器
     *
     * @param url            服务器api
     * @param fileName       文件名
     * @param bytes          文件字节数组
     * @param uploadListener 上传监听器
     */
    public static void upload(String url, String fileName, byte[] bytes, UploadListener uploadListener) {
        try {
            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("name", fileName);
            builder.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("image/jpeg"), bytes));
            MultipartBody requestBody = builder.build();

            //构建请求
            Request request = new Request.Builder()
                    .url(url)//地址
                    .post(requestBody)//添加请求体
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    uploadListener.onUploadFailed(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String res = response.body().string();
                        response.body().close();
                        uploadListener.onUploadSuccess(res);
                    } catch (IOException e) {
                        uploadListener.onUploadFailed(e);
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url          发送请求的 URL
     * @param params       请求参数集合
     * @param httpListener http请求监听器
     */
    public static void post(String url, Map<String, String> params, HttpListener httpListener) {
        OkHttpClient okHttpClient = new OkHttpClient();
        //Form表单格式的参数传递
        FormBody.Builder builder = new FormBody.Builder();

        // 获取参数
        Set<String> keys = params.keySet();
        for (String key : keys) {
            builder.add(key, params.get(key));
        }

        // 构建并发送请求
        FormBody formBody = builder.build();
        final Request request = new Request
                .Builder()
                .post(formBody)//Post请求的参数传递
                .url(url)
                .build();

        // 处理回调
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                JOptionPane.showMessageDialog(null, "网络请求出现故障，请检查网络状态", "提示", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 请求成功
                if (response.isSuccessful()) {
                    httpListener.onPostSuccess(response.body().string());
                } else {
                    // 请求失败
                    JOptionPane.showMessageDialog(null, "服务器出现异常，请稍后再试", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                response.body().close();
            }
        });
    }
}
