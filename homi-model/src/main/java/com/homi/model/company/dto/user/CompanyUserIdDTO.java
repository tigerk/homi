package com.homi.model.company.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "公司用户ID 的DTO")
public class CompanyUserIdDTO {

    @NotNull(message = "用户ID不能为空")
    private Long companyUserId;
}
