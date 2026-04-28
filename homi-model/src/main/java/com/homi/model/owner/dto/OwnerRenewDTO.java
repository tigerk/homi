package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业主合同续约DTO")
public class OwnerRenewDTO extends OwnerCreateDTO {
    @Schema(description = "续约来源业主合同ID")
    private Long sourceContractId;
}
