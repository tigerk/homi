package com.homi.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "用户角色分配DTO")
public class UserRoleAssignDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "至少分配一个角色")
    private Set<Long> roleIds;
}
