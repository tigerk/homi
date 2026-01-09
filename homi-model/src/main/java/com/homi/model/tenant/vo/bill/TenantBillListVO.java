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
public class TenantBillListVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "账单顺序")
    private Integer sortOrder;

    @Schema(description = "账单类型：1=租金，2=押金，3=杂费，4=退租结算")
    private Integer billType;

    @Schema(description = "账单租期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rentPeriodStart;

    @Schema(description = "账单租期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rentPeriodEnd;

    @Schema(description = "租金金额")
    private BigDecimal rentalAmount;

    @Schema(description = "押金金额")
    private BigDecimal depositAmount;

    @Schema(description = "其他费用（如水电、物业）")
    private BigDecimal otherFeeAmount;

    @Schema(description = "账单合计金额")
    private BigDecimal totalAmount;

    @Schema(description = "应收日期（根据 rent_due_xxx 计算）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "实际支付日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @Schema(description = "实际支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付状态：0=未支付，1=部分支付，2=已支付，3=逾期")
    private Integer payStatus;

    @Schema(description = "支付方式：1=现金，2=转账，3=支付宝，4=微信，5=其他")
    private Integer payChannel;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "其他费用明细列表")
    private List<TenantBillOtherFeeVO> otherFees;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
