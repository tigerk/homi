package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主合同线下签约DTO")
public class OwnerContractOfflineSignDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "线下签约合同附件URL列表")
    private List<String> attachmentUrls;
}
