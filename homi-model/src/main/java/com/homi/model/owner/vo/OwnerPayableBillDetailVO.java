package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "包租业主应付单详情VO")
public class OwnerPayableBillDetailVO {
    @Schema(description = "应付单ID")
    private Long billId;

    @Schema(description = "应付单号")
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

    @Schema(description = "应付日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "已付金额")
    private BigDecimal paidAmount;

    @Schema(description = "未付金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "调整金额")
    private BigDecimal adjustAmount;

    @Schema(description = "付款状态")
    private Integer paymentStatus;

    @Schema(description = "单据状态")
    private Integer billStatus;

    @Schema(description = "作废原因")
    private String cancelReason;

    @Schema(description = "作废操作人ID")
    private Long cancelBy;

    @Schema(description = "作废操作人名称")
    private String cancelByName;

    @Schema(description = "作废时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelAt;

    @Schema(description = "生成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date generatedAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "明细列表")
    private List<OwnerPayableBillLineVO> lineList;

    @Schema(description = "付款记录")
    private List<OwnerPayableBillPaymentVO> paymentList;

    @Schema(description = "操作日志")
    private List<BizOperateLogVO> operateLogList;
}
