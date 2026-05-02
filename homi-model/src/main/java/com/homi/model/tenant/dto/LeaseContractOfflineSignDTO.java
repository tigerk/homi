package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "租客合同线下签约DTO")
public class LeaseContractOfflineSignDTO {
    @Schema(description = "租客签约合同文档ID")
    private Long leaseContractDocId;

    @Schema(description = "线下签约合同附件URL列表")
    private List<String> attachmentUrls;
}
