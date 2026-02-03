package com.homi.model.checkout.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 退租初始化数据 VO
 * 用于前端打开退租页面时获取的初始数据
 */
@Data
@Builder
public class CheckoutInitVO {

    /**
     * 租客ID
     */
    private Long tenantId;

    /**
     * 租客姓名
     */
    private String tenantName;

    /**
     * 租客电话
     */
    private String tenantPhone;

    /**
     * 房间信息
     */
    private String roomInfo;

    /**
     * 合同开始日
     */
    private Date leaseStart;

    /**
     * 合同到期日
     */
    private Date leaseEnd;

    /**
     * 月租金
     */
    private BigDecimal rentPrice;

    /**
     * 押金总额
     */
    private BigDecimal depositAmount;

    /**
     * 未付账单列表
     */
    private List<UnpaidBillVO> unpaidBills;

    /**
     * 未付账单总额
     */
    private BigDecimal unpaidAmount;

    /**
     * 未付账单 VO
     */
    @Data
    @Builder
    public static class UnpaidBillVO {
        /**
         * 账单ID
         */
        private Long billId;

        /**
         * 账单编号
         */
        private String billCode;

        /**
         * 费用类型
         */
        private Integer billType;

        /**
         * 账单周期
         */
        private String billPeriod;

        /**
         * 应付金额
         */
        private BigDecimal totalAmount;

        /**
         * 已付金额
         */
        private BigDecimal payAmount;

        /**
         * 未付金额
         */
        private BigDecimal unpaidAmount;
    }
}
