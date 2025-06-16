package com.homi.domain.dto.user;

import com.homi.domain.base.BasePageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询对象")
public class UserQueryDTO extends BasePageDTO {

    private String username;

    private String email;

    private String phone;

    private String nickname;

    private Integer status;
}
