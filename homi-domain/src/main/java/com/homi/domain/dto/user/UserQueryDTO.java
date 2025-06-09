package com.homi.domain.dto.user;

import com.homi.domain.base.BasePage;
import lombok.Data;

/**
 * @author sjh
 * @version 1.0
 * @date 2024-07-11 11:38
 * @description: 用户查询对象
 */
@Data
public class UserQueryDTO extends BasePage {

    private String username;

    private String email;

    private String phone;

    private String nickname;

    private Integer status;
}
