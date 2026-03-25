package com.homi.saas.web.controller;

import com.homi.common.lib.annotation.RepeatSubmit;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.redis.RedisKey;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.trial.application.dto.TrialApplicationCreateDTO;
import com.homi.service.trial.application.TrialApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saas/trialApplication")
@RequiredArgsConstructor
public class TrialApplicationController {

    private final TrialApplicationService trialApplicationService;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/create")
    @RepeatSubmit
    public ResponseResult<Long> create(@Valid @RequestBody TrialApplicationCreateDTO dto) {
        String verifyCode = redisTemplate.opsForValue().get(RedisKey.SMS_CODE.format(dto.getPhone()));
        if (verifyCode == null) {
            throw new BizException("请先发送验证码");
        }

        if (!dto.getVerificationCode().equals(verifyCode)) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
        }

        return ResponseResult.ok(trialApplicationService.create(dto));
    }
}
