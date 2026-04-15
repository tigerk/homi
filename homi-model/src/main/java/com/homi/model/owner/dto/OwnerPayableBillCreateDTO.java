package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.common.lib.enums.owner.OwnerContractSubjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "包租业主应付单新增DTO")
public class OwnerPayableBillCreateDTO {
    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "合同房源类型")
    private OwnerContractSubjectTypeEnum subjectType;

    @Schema(description = "合同房源ID")
    private Long subjectId;

    @Schema(description = "合同房源名称")
    private String subjectName;

    @Schema(description = "账期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billStartDate;

    @Schema(description = "账期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date billEndDate;

    @Schema(description = "应付日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dueDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "明细列表")
    private List<OwnerPayableBillLineDTO> lineList;
}
