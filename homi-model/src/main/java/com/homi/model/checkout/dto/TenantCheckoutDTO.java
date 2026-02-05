package com.homi.model.checkout.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退租单 DTO（退租并结账）
 */
@Data
public class TenantCheckoutDTO {

    /**
     * 退租单ID（修改时传）
     */
    private Long id;

    @Schema(description = "公司ID", hidden = true)
    private Long companyId;

    /**
     * 租客ID
     */
    @NotNull(message = "租客ID不能为空")
    private Long tenantId;

    /**
     * 退租类型：1=正常退，2=违约退
     */
    @NotNull(message = "退租类型不能为空")
    private Integer checkoutType;

    /**
     * 实际离房日期
     */
    @NotNull(message = "实际离房日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actualCheckoutDate;

    /**
     * 解约原因（违约退时选填）
     */
    private String breachReason;

    /**
     * 是否加收清洁费
     */
    private Boolean addCleaningFee;

    /**
     * 清洁费金额
     */
    private BigDecimal cleaningFeeAmount;

    /**
     * 费用明细列表
     */
    private List<TenantCheckoutFeeDTO> feeList;

    /**
     * 预计收/付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectedPaymentDate;

    /**
     * 账单处理方式：1=生成待付账单，2=线下付款，3=申请付款，4=标记坏账
     */
    @NotNull(message = "账单处理方式不能为空")
    private Integer settlementMethod;

    /**
     * 坏账原因（标记坏账时必填）
     */
    private String badDebtReason;

    /**
     * 退租备注
     */
    private String remark;

    /**
     * 退租凭证文件ID列表（附件）
     */
    private List<String> attachmentIds;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款人电话
     */
    private String payeePhone;

    /**
     * 收款人证件类型
     */
    private String payeeIdType;

    /**
     * 收款人证件号
     */
    private String payeeIdNumber;

    /**
     * 收款银行类型（银联等）
     */
    private String bankType;

    /**
     * 银行卡类型（借记卡/信用卡）
     */
    private String bankCardType;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 支行名称
     */
    private String bankBranch;

    /**
     * 是否发送退租确认单给租客
     */
    private Boolean sendConfirmation;

    /**
     * 退租确认单模板
     */
    private String confirmationTemplate;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
