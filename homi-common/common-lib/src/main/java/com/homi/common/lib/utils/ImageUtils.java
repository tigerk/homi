package com.homi.common.lib.utils;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/20
 */
public class ImageUtils {
    private ImageUtils() {
        throw new IllegalStateException("ImageUtils class");
    }

    /**
     * 获取 文件的 md5
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 09:36
     *
     * @param file 参数说明
     * @return java.lang.String
     */
    public static String getFileMD5(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * 获取文件流的 md5
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 09:36
     *
     * @param inputStream 参数说明
     * @return java.lang.String
     */
    public static String getFileMD5(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * 判断两个图片是否一样
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 09:36
     *
     * @param f1 参数说明
     * @param f2 参数说明
     * @return boolean
     */
    public static boolean isSameImage(File f1, File f2) throws Exception {
        return getFileMD5(f1).equals(getFileMD5(f2));
    }

    /**
     * 根据 url 获取文件名
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/20 10:38

      * @param url 参数说明
     * @return java.lang.String
     */
    public static String getFileName(String url) {
        // 1️⃣ 先获取文件名（带后缀）
        return FileUtil.getName(url);
    }
}
