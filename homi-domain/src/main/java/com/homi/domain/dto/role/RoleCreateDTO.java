package com.homi.domain.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "角色创建对象")
public class RoleCreateDTO {

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String code;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 备注
     */
    private String remark;
}
