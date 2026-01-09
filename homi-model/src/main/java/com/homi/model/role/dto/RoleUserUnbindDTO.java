package com.homi.model.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "角色用户解绑DTO")
public class RoleUserUnbindDTO {
    private Long companyUserId;

    private Long roleId;
}
