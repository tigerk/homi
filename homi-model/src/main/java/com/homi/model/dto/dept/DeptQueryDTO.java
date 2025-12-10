package com.homi.model.dto.dept;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "部门查询对象")
@AllArgsConstructor
@NoArgsConstructor
public class DeptQueryDTO extends PageDTO {
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "状态，1：启用，0：禁用")
    private Integer status;
}
