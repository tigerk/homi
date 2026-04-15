package com.homi.model.tenant.vo.bill;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
@Schema(description = "租客账单VO")
public class LeaseBillListVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "账单顺序")
    private Integer sortOrder;

    @Schema(description = "账单类型：1=租金，2=押金，3=杂费，4=退租结算，5=押金结转入，6=押金结转出")
    private Integer billType;

    @Schema(description = "账单状态：1=正常，2=已作废")
    private Integer status;

    @Schema(description = "结转来源账单ID")
    private Long carryOverFromBillId;

    @Schema(description = "结转目标账单ID")
    private Long carryOverToBillId;

    @Schema(description = "账单周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "账单合计金额")
    private BigDecimal totalAmount;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "待收金额")
    private BigDecimal unpaidAmount;

    @Schema(description = "应收日期（根据 rent_due_xxx 计算）")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付")
    private Integer payStatus;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "是否历史账单：0=否，1=是")
    private Boolean historical;

    @Schema(description = "作废原因")
    private String voidReason;

    @Schema(description = "作废时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date voidAt;

    @Schema(description = "作废人")
    private Long voidBy;

    @Schema(description = "作废人昵称")
    private String voidByName;

    @Schema(description = "房源地址")
    private String roomAddress;

    @Schema(description = "付款人姓名")
    private String payerName;

    @Schema(description = "付款人手机号")
    private String payerPhone;

    @Schema(description = "付款人证件类型")
    private Integer payerIdType;

    @Schema(description = "付款人证件类型名称")
    private String payerIdTypeName;

    @Schema(description = "付款人证件号")
    private String payerIdNo;

    @Schema(description = "财务流水信息")
    private List<FinanceFlowVO> financeFlowList;

    @Schema(description = "支付流水信息")
    private List<PaymentFlowVO> paymentFlowList;

    @Schema(description = "账单费用明细列表")
    private List<LeaseBillFeeVO> feeList;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
