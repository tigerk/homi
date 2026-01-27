package com.homi.model.approval.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApprovalFlowQueryDTO extends PageDTO {
    /**
     * 公司ID
     */
    private Long companyId;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "是否启用：false=停用，true=启用")
    private Boolean enabled;

    @Schema(description = "业务类型：TENANT_CHECKIN=租客入住，TENANT_CHECKOUT=退租，HOUSE_CREATE=房源录入，CONTRACT_SIGN=合同签署")
    private String bizType;
}
