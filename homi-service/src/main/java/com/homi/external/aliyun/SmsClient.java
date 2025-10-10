package com.homi.external.aliyun;

import cn.hutool.json.JSONUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;


/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/10
 */

@Slf4j
@Service
public class SmsClient {
    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.sms.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.endpoint}")
    private String endPoint;

    @Value("${aliyun.sms.regionId}")
    private String regionId;

    private AsyncClient client;

    @PostConstruct
    public void init() {
        try {
            createClient();
        } catch (Exception e) {
            log.error("sms client创建失败", e);
        }
    }

    /**
     * 使用AK&amp;SK初始化账号Client
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/10/10 21:04
     */
    public void createClient() {
        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                // Please ensure that the environment variables ALIBABA_CLOUD_ACCESS_KEY_ID and ALIBABA_CLOUD_ACCESS_KEY_SECRET are set.
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build());

        client = AsyncClient.builder()
                .region(regionId)
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(endPoint)
                                .setConnectTimeout(Duration.ofSeconds(30))
                ).build();
    }

    public void send(String phoneNumber, String signName, String templateCode, String templateParam) {
        // Parameter settings for API request
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .phoneNumbers(phoneNumber)
                .signName(signName)
                .templateCode(templateCode)
                .templateParam(templateParam)
                .build();

        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        response.thenAccept(resp -> {
            log.info("短信发送成功, 手机号: {}, 短信签名: {}, 短信模板: {}, 短信参数: {}, resp: {}", phoneNumber, signName, templateCode, templateParam, JSONUtil.toJsonStr(resp));
        }).exceptionally(throwable -> { // Handling exceptions
            log.error("短信发送失败, 手机号: {}, 短信签名: {}, 短信模板: {}, 短信参数: {}", phoneNumber, signName, templateCode, templateParam, throwable);
            return null;
        });
    }

    @PreDestroy
    public void closeClient() {
        if (client != null) {
            client.close();
        }
    }
}
