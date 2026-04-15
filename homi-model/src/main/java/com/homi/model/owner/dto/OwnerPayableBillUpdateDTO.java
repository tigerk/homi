package com.homi.model.owner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "包租业主应付单修改DTO")
public class OwnerPayableBillUpdateDTO extends OwnerPayableBillCreateDTO {
    @Schema(description = "应付单ID")
    private Long billId;
}
