package com.homi.model.finance.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付流水详情查询DTO")
public class PaymentFlowIdDTO extends PageDTO {
    @Schema(description = "支付流水ID")
    @NotNull(message = "支付流水ID不能为空")
    private Long id;
}
