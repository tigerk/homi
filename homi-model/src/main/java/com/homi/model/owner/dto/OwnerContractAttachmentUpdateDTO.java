package com.homi.model.owner.dto;

import com.homi.model.common.dto.FileAttachGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主合同附件更新DTO")
public class OwnerContractAttachmentUpdateDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(description = "附件分组列表")
    private List<FileAttachGroupDTO> attachmentGroupList;
}
