package com.homi.domain.dto.dept;

import com.homi.domain.base.BasePageDTO;
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
public class DeptQueryDTO extends BasePageDTO {

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "状态，1：启用，0：禁用")
    private Integer status;
}
