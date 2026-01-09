package com.homi.model.user;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户查询对象")
public class UserQueryDTO extends PageDTO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "角色ID")
    private Long roleId;

    private String email;

    private String phone;

    private String nickname;

    private Integer status;
}
