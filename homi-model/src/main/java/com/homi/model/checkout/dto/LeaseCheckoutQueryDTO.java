package com.homi.model.checkout.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退租单查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaseCheckoutQueryDTO extends PageDTO {

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 租客ID
     */
    private Long tenantId;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "退租单ID")
    private Long checkoutId;

    /**
     * 退租单编号
     */
    private String checkoutCode;

    /**
     * 租客姓名
     */
    private String tenantName;

    /**
     * 退租类型：1=正常退，2=违约退
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
