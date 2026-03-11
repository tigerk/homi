package com.homi.model.contract.dto.seal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractSealDeleteDTO {
    @Schema(description = "合同电子印章ID")
    @NotNull
    private Long id;
}
