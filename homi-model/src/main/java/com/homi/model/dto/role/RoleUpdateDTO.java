package com.homi.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "角色修改对象 DTO")
public class RoleUpdateDTO {

    /**
     * ID
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 状态（0正常 1停用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
