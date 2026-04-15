package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "轻托管业主结算单详情VO")
public class OwnerSettlementBillDetailVO {
    @Schema(description = "结算单ID")
    private Long billId;

    @Schema(description = "结算单号")
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

    @Schema(description = "账期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStartDate;

    @Schema(description = "账期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEndDate;

    @Schema(description = "收入金额")
    private BigDecimal incomeAmount;

    @Schema(description = "减免金额")
    private BigDecimal reductionAmount;

    @Schema(description = "费用金额")
    private BigDecimal expenseAmount;

    @Schema(description = "调整金额")
    private BigDecimal adjustAmount;

    @Schema(description = "应结金额")
    private BigDecimal payableAmount;

    @Schema(description = "已结金额")
    private BigDecimal settledAmount;

    @Schema(description = "未结金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "冻结金额")
    private BigDecimal freezeAmount;

    @Schema(description = "可提现金额")
    private BigDecimal withdrawableAmount;

    @Schema(description = "单据状态")
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

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "结算单明细")
    private List<OwnerSettlementBillLineVO> lineList;

    @Schema(description = "减免明细")
    private List<OwnerSettlementBillReductionVO> reductionList;
}
