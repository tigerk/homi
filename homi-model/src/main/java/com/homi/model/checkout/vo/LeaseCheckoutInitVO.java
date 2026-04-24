package com.homi.model.checkout.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "退租初始化数据 VO")
public class LeaseCheckoutInitVO {
    // ===== 合同信息 =====
    /**
     * 租客ID
     */
    @Schema(description = "租客ID")
    private Long tenantId;

    /**
     * 租约ID
     */
    @Schema(description = "租约ID")
    private Long leaseId;

    /**
     * 房源地址（如"12312栋12单元-104室"）
     */
    @Schema(description = "房源地址（如\"12312栋12单元-104室\"）")
    private String roomAddress;

    /**
     * 合同开始日
     */
    @Schema(description = "合同开始日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    /**
     * 合同到期日
     */
    @Schema(description = "合同到期日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
    @ArraySchema(schema = @Schema(implementation = LeaseCheckoutFeeVO.class))
    private List<LeaseCheckoutFeeVO> presetFees;

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
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date periodStart;
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date periodEnd;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private BigDecimal unpaidAmount;
    }

    /**
     * 收款人信息 VO
     */
        @Data
        @Builder
        public static class PayeeInfoVO {
        private String payeeName;
        private String payeePhone;
        @Schema(description = "收款人证件类型")
        private Integer payeeIdType;
        private String payeeIdNo;
    }
}
