package com.homi.external.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/2/10
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MailClient {
    private final ObjectProvider<JavaMailSender> javaMailSenderProvider;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Async
    public void send(String email, String title, String content) {
        JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
        if (javaMailSender == null) {
            log.warn("跳过发送邮件，JavaMailSender 未配置, to={}, title={}", email, title);
            return;
        }
        if (!StringUtils.hasText(mailFrom)) {
            log.warn("跳过发送邮件，spring.mail.username 未配置, to={}, title={}", email, title);
            return;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setFrom(mailFrom);
        mailMessage.setSubject(title);
        mailMessage.setText(content);

        log.info("发送了新邮件, {}", mailMessage);

        javaMailSender.send(mailMessage);
    }
}
