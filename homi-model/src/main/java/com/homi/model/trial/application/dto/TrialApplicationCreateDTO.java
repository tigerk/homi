package com.homi.model.trial.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "试用申请创建参数")
public class TrialApplicationCreateDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String verificationCode;

    @NotNull(message = "城市不能为空")
    private Long regionId;

    @Schema(description = "如何使用系统")
    private String usageRemark;
}
