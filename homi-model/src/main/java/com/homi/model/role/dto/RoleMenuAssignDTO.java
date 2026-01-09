package com.homi.model.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;


@Data
@Schema(description = "角色菜单分配DTO")
public class RoleMenuAssignDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private Set<Long> menuIds;
}
