package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "业主提现申请创建DTO")
public class OwnerWithdrawCreateDTO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "申请金额")
    private BigDecimal applyAmount;

    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    @Schema(description = "收款人姓名")
    private String payeeName;

    @Schema(description = "收款账号")
    private String payeeAccountNo;

    @Schema(description = "开户行名称")
    private String payeeBankName;

    @Schema(description = "备注")
    private String remark;
}
