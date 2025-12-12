package com.homi.model.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleIdDTO {
    /**
     * ID
     */
    @NotNull(message = "id不能为空")
    private Long id;
}
