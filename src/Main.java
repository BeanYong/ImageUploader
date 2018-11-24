import http.HttpRequest;
import listener.HttpListener;
import listener.ThreadPoolListener;
import net.sf.json.JSONObject;
import thread.UploadThreadPool;
import util.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    /**
     * 全局UI面板
     */
    private static JPanel mPanel = null;

    /**
     * 程序入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        // 创建 JFrame 实例
        JFrame frame = new JFrame("菜谱专家-图片上传器");
        // Setting the width and height of frame
        frame.setSize(600, 360);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // 启动时窗口在屏幕居中

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        mPanel = new JPanel();
        // 添加面板
        frame.add(mPanel);
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        placeComponents();

        // 设置界面可见
        frame.setVisible(true);
    }

    /**
     * 布局组件
     */
    private static void placeComponents() {
        mPanel.setLayout(null);

        // 创建 JLabel
        JLabel userLabel = new JLabel("账号:");
        /* 这个方法定义了组件的位置。
         * setBounds(x, y, width, height)
         * x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小。
         */
        userLabel.setBounds(100, 100, 80, 25);
        mPanel.add(userLabel);

        /*
         * 创建文本域用于用户输入
         */
        JTextField userText = new JTextField();
        userText.setBounds(180, 100, 300, 25);
        mPanel.add(userText);

        // 输入密码的文本域
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(100, 140, 80, 25);
        mPanel.add(passwordLabel);

        /*
         *这个类似用于输入的文本域
         * 但是输入的信息会以点号代替，用于包含密码的安全性
         */
        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(180, 140, 300, 25);
        mPanel.add(passwordText);

        // 创建登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBounds(400, 180, 80, 25);
        mPanel.add(loginButton);
        // 为登录按钮添加点击事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = userText.getText();
                String password = new String(passwordText.getPassword());
                login(account, password);
            }
        });
    }

    /**
     * 向服务器发送登录请求
     *
     * @param account  登录账号
     * @param password 登录密码
     */
    private static void login(String account, String password) {
        // 对数据判空
        if (account.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "账号和密码不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuilder buf = new StringBuilder();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("account", account);
            params.put("password", buf.toString());

            // 发送 登录 请求
            HttpRequest.post(Constants.LOGIN_URL, params, new HttpListener() {
                @Override
                public void onPostSuccess(String response) {
                    JSONObject jsStr = JSONObject.fromObject(response);
                    JOptionPane.showMessageDialog(null, jsStr.get("message"), "提示", JOptionPane.INFORMATION_MESSAGE);

                    // 请求成功，重绘UI
                    if (jsStr.get("result").toString() == "true") {
                        // 重新布局上传UI
                        resetUI();
                    }
                }
            });
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "程序出现异常，请重新启动", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 重绘UI，用于上传图片
     */
    private static void resetUI() {
        // 清空面板
        mPanel.removeAll();

        // 创建文本域用于用户输入
        JTextField dirPath = new JTextField();
        dirPath.setBounds(120, 120, 300, 26);
        mPanel.add(dirPath);

        // 添加按钮
        JButton uploadBtn = new JButton("上传");
        uploadBtn.setBounds(418, 120, 60, 25);
        mPanel.add(uploadBtn);

        // 创建JLabel显示tip
        JLabel tip = new JLabel("只允许上传20M以内的图片，超过20M会上传失败");
        tip.setBounds(120, 155, 300, 26);
        mPanel.add(tip);
        mPanel.updateUI();

        JLabel uploadLabel = new JLabel();
        uploadLabel.setBounds(120, 180, 300, 26);

        uploadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = dirPath.getText();
                File dirFile = new File(path);
                // 判断是否为合法的目录
                if (dirFile.isDirectory()) {
                    // 创建JLabel显示上传提示
                    uploadLabel.setText("正在上传第1张图片，请耐心等候......");
                    mPanel.add(uploadLabel);
                    mPanel.updateUI();

                    // 读取图片
                    File[] imageFiles = dirFile.listFiles();

                    // 获取线程池
                    UploadThreadPool threadPool = null;
                    if (imageFiles.length != 0) {
                        threadPool = UploadThreadPool.getInstance(new ThreadPoolListener() {
                            @Override
                            public void onThreadPoolFinished(int successCount, int failCount) {
                                JOptionPane.showMessageDialog(null,
                                        "上传完成",
                                        "提示", JOptionPane.INFORMATION_MESSAGE);
                                uploadLabel.setText("上传成功：" + successCount + "张图片，上传失败：" + failCount + "张图片");
                                mPanel.updateUI();
                            }
                        });
                    }

                    // 禁止同时上传多个文件夹
                    if(threadPool == null){
                        JOptionPane.showMessageDialog(null, "请等待当前文件夹上传完成", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // 清空路径输入框
                    dirPath.setText("");

                    // 将图片提交到线程池
                    for (File image : imageFiles) {
                        String imageName = image.getName();

                        // 图片格式符合要求
                        if (imageName.contains(".") && "jpg".equalsIgnoreCase(imageName.split("\\.")[1])) {
                            // 将图片提交到线程池
                            byte[] bytes = FileUtil.fileToBytes(image);
                            threadPool.upload(Constants.UPLOAD_URL, imageName, bytes, uploadLabel);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "请输入正确的文件夹路径", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }
}
