package com.homi.service.external.qiniu;

import cn.hutool.json.JSONUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用于 nest
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/9/9
 */

@Component
public class QiniuClient {
    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    /**
     * 上传文件到七牛云
     *
     * @param bytes    文件字节数组
     * @param fileName 存储到七牛云的文件名
     * @return 文件外链URL
     */
    public String upload(byte[] bytes, String fileName) throws QiniuException {
        // 1. 构造配置类（选择存储区域，常见华东、华北、华南）
        Configuration cfg = Configuration.create(Region.autoRegion());

        // 2. 创建上传管理器
        UploadManager uploadManager = new UploadManager(cfg);

        // 3. 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket, fileName);

        // 4. 上传
        Response response = uploadManager.put(bytes, fileName, upToken);

        // 5. 解析上传结果
        DefaultPutRet putRet = JSONUtil.toBean(response.bodyString(), DefaultPutRet.class);

        // 6. 返回可访问的 URL
        return domain + "/" + putRet.key;
    }
}
