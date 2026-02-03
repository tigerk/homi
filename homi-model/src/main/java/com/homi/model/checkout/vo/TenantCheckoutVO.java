package com.homi.model.checkout.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退租单 VO
 */
@Data
public class TenantCheckoutVO {

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
     * 交割单ID
     */
    private Long deliveryId;

    /**
     * 退租类型
     */
    private Integer checkoutType;

    /**
     * 退租类型名称
     */
    private String checkoutTypeName;

    /**
     * 退租原因
     */
    private String checkoutReason;

    /**
     * 合同到期日
     */
    private LocalDate leaseEnd;

    /**
     * 实际退租日
     */
    private LocalDate actualCheckoutDate;

    /**
     * 押金总额
     */
    private BigDecimal depositAmount;

    /**
     * 扣款总额
     */
    private BigDecimal deductionAmount;

    /**
     * 应退金额
     */
    private BigDecimal refundAmount;

    /**
     * 最终结算
     */
    private BigDecimal finalAmount;

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
    private LocalDateTime settlementTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 费用明细列表
     */
    private List<TenantCheckoutFeeVO> feeList;

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
    private LocalDateTime createTime;
}
