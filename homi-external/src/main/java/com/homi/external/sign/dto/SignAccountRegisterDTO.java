package com.homi.external.sign.dto;

import lombok.Data;

@Data
public class SignAccountRegisterDTO {

    /**
     * 账号类型：1=个人，2=企业
     */
    private String accountType;

    /**
     * 平台方自定义唯一标识
     */
    private String openId;
}
