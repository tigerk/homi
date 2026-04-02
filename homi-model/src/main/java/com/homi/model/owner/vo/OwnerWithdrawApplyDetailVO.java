package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "业主提现申请详情VO")
public class OwnerWithdrawApplyDetailVO {
    @Schema(description = "提现申请ID")
    private Long applyId;

    @Schema(description = "提现单号")
    private String applyNo;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "业主联系电话")
    private String ownerPhone;

    @Schema(description = "申请金额")
    private BigDecimal applyAmount;

    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "审批状态")
    private Integer approvalStatus;

    @Schema(description = "打款状态")
    private Integer withdrawStatus;

    @Schema(description = "收款人")
    private String payeeName;

    @Schema(description = "收款账号")
    private String payeeAccountNo;

    @Schema(description = "开户行名称")
    private String payeeBankName;

    @Schema(description = "打款渠道")
    private String channel;

    @Schema(description = "第三方交易号")
    private String thirdTradeNo;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appliedAt;

    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approvedAt;

    @Schema(description = "打款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paidAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "账户流水列表")
    private List<OwnerAccountFlowVO> flowList;
}
