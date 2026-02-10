package com.homi.model.checkout.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退租单 VO（退租并结账详情）
 */
@Data
public class LeaseCheckoutVO {

    /**
     * 退租单ID
     */
    private Long id;

    /**
     * 退租单编号
     */
    private String checkoutCode;

    /**
     * 公司ID
     */
    private Long companyId;

    // ===== 合同信息 =====

    /**
     * 租客ID
     */
    private Long tenantId;

    /**
     * 租约ID
     */
    private Long leaseId;

    /**
     * 房源地址
     */
    private String roomAddress;

    /**
     * 合同开始日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    /**
     * 合同到期日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private java.util.Date leaseEnd;

    /**
     * 承租人姓名
     */
    private String tenantName;

    /**
     * 承租人电话
     */
    private String tenantPhone;

    /**
     * 委托人信息
     */
    private String agentInfo;

    /**
     * 月租金
     */
    private BigDecimal rentPrice;

    /**
     * 押金总额
     */
    private BigDecimal depositAmount;

    // ===== 退租信息 =====

    /**
     * 交割单ID
     */
    private Long deliveryId;

    /**
     * 退租类型：1=正常退，2=违约退
     */
    private Integer checkoutType;

    /**
     * 退租类型名称
     */
    private String checkoutTypeName;

    /**
     * 实际离房日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actualCheckoutDate;

    /**
     * 解约原因（违约退时填写）
     */
    private String breachReason;

    // ===== 费用结算 =====

    /**
     * 收入总额（租客应付）
     */
    private BigDecimal incomeAmount;

    /**
     * 支出总额（退还租客）
     */
    private BigDecimal expenseAmount;

    /**
     * 最终结算金额（负数=应退租客，正数=租客补缴）
     */
    private BigDecimal finalAmount;

    /**
     * 预计收/付款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectedPaymentDate;

    /**
     * 账单处理方式
     */
    private Integer settlementMethod;

    /**
     * 账单处理方式名称
     */
    private String settlementMethodName;

    /**
     * 费用明细列表
     */
    private List<LeaseCheckoutFeeVO> feeList;

    // ===== 其他信息 =====

    /**
     * 退租备注
     */
    private String remark;

    /**
     * 退租凭证附件列表
     */
    private List<String> attachmentUrls;

    // ===== 收款人信息 =====

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
     * 银行类型
     */
    private String bankType;

    /**
     * 银行卡类型
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

    // ===== 状态 =====

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 审批状态
     */
    private Integer approvalStatus;

    /**
     * 审批状态名称
     */
    private String approvalStatusName;

    /**
     * 结算完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date settlementTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建人姓名
     */
    private String createByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
