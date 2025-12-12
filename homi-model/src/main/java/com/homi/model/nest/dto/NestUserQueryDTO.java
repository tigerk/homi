package com.homi.model.nest.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询对象")
public class NestUserQueryDTO extends PageDTO {
    private String phone;

    private String username;

    private Integer status;
}
