package com.homi.model.role.dto;

import com.homi.common.lib.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author tigerk
 * @version 1.0
 * @date 2024-07-01 21:04
 * @description: 角色查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQueryDTO extends PageDTO {
    private String name;

    private String code;

    private Integer status;
}
