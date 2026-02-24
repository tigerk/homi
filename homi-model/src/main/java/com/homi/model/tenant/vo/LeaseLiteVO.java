package com.homi.model.tenant.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "租约简化VO")
public class LeaseLiteVO {
    @Schema(description = "租约 ID")
    private Long leaseId;

    @Schema(description = "租客 ID")
    private Long tenantId;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门 ID")
    private Long deptId;

    @Schema(description = "租客名称")
    private String tenantName;

    @Schema(description = "租客联系电话")
    private String tenantPhone;

    @Schema(description = "租金价格")
    private BigDecimal rentPrice;

    @Schema(description = "租赁开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租赁结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    private Integer signStatus;

    @Schema(description = "租户退租状态：0=未退租、1=正常退、2=换房退、3=违约退、4=作废")
    private Integer checkOutStatus;

    @Schema(description = "合同状态：0=未生效，1=生效中，2=已退租，3=已逾期，4=已作废")
    private Integer status;
}
