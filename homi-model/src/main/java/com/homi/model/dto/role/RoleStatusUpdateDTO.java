package com.homi.model.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleStatusUpdateDTO {
    /**
     * ID
     */
    @NotNull(message = "id不能为空")
    private Long id;
    /**
     * 角色名称
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
