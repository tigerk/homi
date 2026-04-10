package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import com.homi.common.lib.enums.owner.OwnerCooperationModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主账单列表VO")
public class OwnerBillListVO {
    @Schema(description = "账单ID")
    private Long billId;

    @Schema(description = "账单编号")
    private String billNo;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "业主联系电话")
    private String ownerPhone;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同房源类型")
    private OwnerContractSubjectTypeEnum subjectType;

    @Schema(description = "合同房源ID")
    private Long subjectId;

    @Schema(description = "合同房源名称")
    private String subjectName;

    @Schema(description = "合作模式")
    private OwnerCooperationModeEnum cooperationMode;

    @Schema(description = "账期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "收入金额")
    private BigDecimal incomeAmount;

    @Schema(description = "减免金额")
    private BigDecimal reductionAmount;

    @Schema(description = "费用金额")
    private BigDecimal expenseAmount;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "已结金额")
    private BigDecimal settledAmount;

    @Schema(description = "未结金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "可提现金额")
    private BigDecimal withdrawableAmount;

    @Schema(description = "账单状态")
    private Integer billStatus;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "结算状态")
    private Integer settlementStatus;

    @Schema(description = "生成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date generatedAt;

    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approvedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
