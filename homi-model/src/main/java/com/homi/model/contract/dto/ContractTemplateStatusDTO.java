package com.homi.model.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/12
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractTemplateStatusDTO {
    @Schema(description = "合同模板ID")
    private Long id;

    @Schema(description = "合同状态：0=未生效，1=生效中")
    @NotNull(message = "合同状态不能为空")
    private Integer status;

    @Schema(description = "修改人ID")
    private Long updateBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
