package com.homi.model.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "公司查询对象")
public class UserCompanyListDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司用户类型：20=管理员、21=员工")
    private Integer userType;
}
