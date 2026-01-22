package com.homi.model.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
public class DeliveryUpdateDTO {

    @NotNull(message = "交割单ID不能为空")
    private Long id;

    @NotNull(message = "交割日期不能为空")
    private LocalDate handoverDate;

    private Long inspectorId;

    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;

    @Valid
    private List<DeliveryItemDTO> items;

    private List<String> imageList;
}
