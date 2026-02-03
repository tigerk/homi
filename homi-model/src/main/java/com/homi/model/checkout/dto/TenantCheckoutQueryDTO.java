package com.homi.model.checkout.dto;

import com.homi.common.lib.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退租单查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantCheckoutQueryDTO extends PageDTO {

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 租客ID
     */
    private Long tenantId;

    /**
     * 退租单编号
     */
    private String checkoutCode;

    /**
     * 租客姓名
     */
    private String tenantName;

    /**
     * 退租类型
     */
    private Integer checkoutType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 审批状态
     */
    private Integer approvalStatus;
}
