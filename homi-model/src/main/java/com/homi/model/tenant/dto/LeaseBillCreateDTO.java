package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "租客账单新增DTO")
public class LeaseBillCreateDTO {
    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "账单顺序")
    private Integer sortOrder;

    @Schema(description = "账单类型")
    private Integer billType;

    @Schema(description = "账单周期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStart;

    @Schema(description = "账单周期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEnd;

    @Schema(description = "应收日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "是否历史账单")
    private Boolean historical;

    @Schema(description = "账单费用明细")
    private List<LeaseBillFeeDTO> feeList;
}
