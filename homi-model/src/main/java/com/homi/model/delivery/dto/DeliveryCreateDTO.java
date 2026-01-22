package com.homi.model.delivery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
@Schema(description = "交割单创建DTO")
public class DeliveryCreateDTO {
    @NotNull(message = "主体类型不能为空")
    private String subjectType;

    @NotNull(message = "主体ID不能为空")
    private Long subjectTypeId;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotNull(message = "交割类型不能为空")
    private String handoverType;

    @NotNull(message = "交割日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date handoverDate;

    private Long inspectorId;

    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;

    @Valid
    private List<DeliveryItemDTO> items;

    private List<String> imageList;

    private Long createBy;
}
