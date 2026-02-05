package com.homi.model.checkout.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退租初始化数据 VO
 * 用于前端打开退租页面时获取的初始数据
 * 包含合同信息、租客信息、未付账单、预填费用
 */
@Data
@Builder
public class CheckoutInitVO {

    // ===== 合同信息 =====

    /**
     * 租客ID
     */
    private Long tenantId;

    /**
     * 房源地址（如"12312栋12单元-104室"）
     */
    private String roomAddress;

    /**
     * 合同开始日
     */
    private Date leaseStart;

    /**
     * 合同到期日
     */
    private Date leaseEnd;

    /**
     * 承租人姓名
     */
    private String tenantName;

    /**
     * 承租人电话
     */
    private String tenantPhone;

    /**
     * 委托人信息（如"杨某-18871085323"）
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

    /**
     * 押几个月
     */
    private Integer depositMonths;

    // ===== 未付账单 =====

    /**
     * 未付账单列表
     */
    private List<UnpaidBillVO> unpaidBills;

    /**
     * 未付账单总额
     */
    private BigDecimal unpaidAmount;

    // ===== 预填费用列表（系统自动生成的退租清算费用行） =====

    /**
     * 预填费用列表
     */
    private List<PresetFeeVO> presetFees;

    /**
     * 收款人信息
     */
    private PayeeInfoVO payeeInfo;

    /**
     * 未付账单 VO
     */
    @Data
    @Builder
    public static class UnpaidBillVO {
        private Long billId;
        private String billCode;
        private Integer billType;
        private String billTypeName;
        private String billPeriod;
        private Date periodStart;
        private Date periodEnd;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private BigDecimal unpaidAmount;
    }

    /**
     * 预填费用行 VO
     */
    @Data
    @Builder
    public static class PresetFeeVO {
        /**
         * 收支类型：1=收，2=支
         */
        private Integer feeDirection;
        /**
         * 费用类型
         */
        private Integer feeType;
        /**
         * 费用子类名称
         */
        private String feeSubName;
        /**
         * 金额
         */
        private BigDecimal feeAmount;
        /**
         * 费用周期开始
         */
        private Date feePeriodStart;
        /**
         * 费用周期结束
         */
        private Date feePeriodEnd;
        /**
         * 备注
         */
        private String remark;
        /**
         * 关联账单ID
         */
        private Long billId;
    }

    /**
     * 收款人信息 VO
     */
    @Data
    @Builder
    public static class PayeeInfoVO {
        private String payeeName;
        private String payeePhone;
        private String payeeIdType;
        private String payeeIdNumber;
    }
}
