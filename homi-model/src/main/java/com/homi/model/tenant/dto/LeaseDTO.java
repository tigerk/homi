package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class LeaseDTO {
    @Schema(description = "租约ID")
    private Long id;

    @Schema(description = "租客ID（续签时传入已有租客ID）")
    private Long tenantId;

    @Schema(description = "上一份租约ID（续签时传入）")
    private Long parentLeaseId;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房")
    private Integer contractNature;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "房间ID列表")
    private List<Long> roomIds;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "租金价格")
    private BigDecimal rentPrice;

    @Schema(description = "押金月数")
    private Integer depositMonths;

    @Schema(description = "支付周期（月）")
    private Integer paymentMonths;

    @Schema(description = "首期账单收租日")
    private Integer firstBillDay;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkInTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkOutTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseStart;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseEnd;

    private Integer leaseDurationDays;
    private Integer rentDueType;
    private Integer rentDueDay;
    private Integer rentDueOffsetDays;
    private Long salesmanId;
    private Long helperId;
    private Integer signStatus;
    private Integer checkOutStatus;
    private Integer status;
    private Long tenantSource;
    private Long dealChannel;
    private String remark;
    private Long createBy;
}
