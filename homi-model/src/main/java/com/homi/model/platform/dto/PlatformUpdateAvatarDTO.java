package com.homi.model.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlatformUpdateAvatarDTO {

    /**
     * ID
     */
    @NotNull(message = "用户编号不能为空")
    private Long id;

    /**
     * 上传图片
     */
    private String avatar;
}
