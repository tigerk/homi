package com.homi.model.owner.dto;

import com.homi.common.lib.enums.owner.OwnerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主更新DTO")
public class OwnerUpdateDTO {
    @Schema(description = "业主类型")
    private OwnerTypeEnum ownerType;

    @Schema(description = "个人业主信息")
    private OwnerPersonalDTO ownerPersonal;

    @Schema(description = "企业业主信息")
    private OwnerCompanyDTO ownerCompany;

    @Schema(description = "业主合同")
    private OwnerContractDTO ownerContract;

    @Schema(description = "合同房源列表")
    private List<OwnerContractSubjectDTO> contractSubjectList;

    @Schema(description = "包租规则")
    private OwnerLeaseRuleDTO ownerLeaseRule;

    @Schema(description = "包租免租规则列表")
    private List<OwnerLeaseFreeRuleDTO> ownerLeaseFreeRuleList;

    @Schema(description = "更新人")
    private Long updateBy;
}
