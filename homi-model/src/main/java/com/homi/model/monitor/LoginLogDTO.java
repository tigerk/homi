package com.homi.model.monitor;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/7/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "登录日志DTO")
public class LoginLogDTO extends PageDTO {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录状态")
    private Integer status;

    @Schema(description = "登录时间")
    private List<OffsetDateTime> loginTime;
}
