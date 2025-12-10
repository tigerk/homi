package com.homi.model.vo.company;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 nest
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/16
 */

@Data
@Schema(description = "公司创建返回VO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyCreateVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    @Schema(description = "返回消息文本")
    private String message;
}
