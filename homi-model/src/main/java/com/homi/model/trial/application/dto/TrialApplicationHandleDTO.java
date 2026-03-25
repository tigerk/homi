package com.homi.model.trial.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "试用申请处理参数")
public class TrialApplicationHandleDTO {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    @NotNull(message = "处理状态不能为空")
    private Integer status;

    @NotBlank(message = "备注不能为空")
    private String handleRemark;
}
