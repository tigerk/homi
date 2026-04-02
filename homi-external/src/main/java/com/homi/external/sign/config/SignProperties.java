package com.homi.external.sign.config;

import com.homi.common.lib.enums.contract.ContractSealSourceEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sign")
public class SignProperties {

    /**
     * 默认电子签供应商，默认法大大
     */
    private Integer defaultSource = ContractSealSourceEnum.FADADA.getCode();
}
