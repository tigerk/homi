package com.homi.external.sign;

import com.homi.external.sign.dto.SignAccountRegisterDTO;

public interface SignProvider {

    /**
     * 对应 ContractSealSourceEnum.code
     */
    Integer getSource();

    /**
     * 注册第三方签章账号
     */
    String registerAccount(SignAccountRegisterDTO request);
}
