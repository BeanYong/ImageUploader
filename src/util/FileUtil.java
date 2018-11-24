package util;

import javax.swing.*;
import java.io.*;

public class FileUtil {
    /**
     * 将文件转换为字节数组
     *
     * @param file 文件对象
     * @return 文件的字节数组
     */
    public static byte[] fileToBytes(File file) {
        byte[] buffer = null;

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int n;

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            buffer = bos.toByteArray();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "图片不存在", "提示", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "上传出现异常，请重新尝试", "提示", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "上传出现异常，请重新尝试", "提示", JOptionPane.INFORMATION_MESSAGE);
            } finally {
                try {
                    if (null != fis) {
                        fis.close();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "上传出现异常，请重新尝试", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        return buffer;
    }
}
