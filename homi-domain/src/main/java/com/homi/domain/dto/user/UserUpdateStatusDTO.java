package com.homi.domain.dto.user;

import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.validator.EnumValue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更改
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/28 18:23
 */
@Data
public class UserUpdateStatusDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 状态（0正常，1禁用）
     */
    @NotNull(message = "状态不能为空")
    @EnumValue(enumClass = StatusEnum.class, message = "状态只能为0（正常）或1（冻结）")
    private Integer status;
}
