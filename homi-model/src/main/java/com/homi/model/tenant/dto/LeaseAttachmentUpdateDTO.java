package com.homi.model.tenant.dto;

import com.homi.model.common.dto.FileAttachGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "租约资料附件更新DTO")
public class LeaseAttachmentUpdateDTO {
    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(description = "附件分组列表")
    private List<FileAttachGroupDTO> attachmentGroupList;
}
