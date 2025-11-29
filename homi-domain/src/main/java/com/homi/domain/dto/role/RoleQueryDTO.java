package com.homi.domain.dto.role;

import com.homi.domain.base.PageDTO;
import lombok.Data;

/**
 * @author sjh
 * @version 1.0
 * @date 2024-07-01 21:04
 * @description: 角色查询对象
 */
@Data
public class RoleQueryDTO extends PageDTO {

    private String name;

    private String code;

    private Integer status;
}
