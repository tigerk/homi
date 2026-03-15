package com.homi.model.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LeaseBillDetailDTO {
    @Schema(description = "账单ID", example = "1")
    private Long billId;
}
