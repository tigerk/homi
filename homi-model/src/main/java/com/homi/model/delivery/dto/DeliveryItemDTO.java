package com.homi.model.delivery.dto;

import com.homi.common.lib.enums.delivery.DeliveryItemCategoryEnum;
import com.homi.common.lib.enums.delivery.DeliveryItemCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
@Schema(description = "交割单项目DTO")
public class DeliveryItemDTO {
    @Schema(description = "项目编码", implementation = DeliveryItemCodeEnum.class)
    private String itemCode;

    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @NotBlank(message = "项目分类不能为空")
    @Schema(description = "项目分类", implementation = DeliveryItemCategoryEnum.class)
    private String itemCategory;

    @Schema(description = "项目单位")
    private String itemUnit;

    private String preValue;

    @NotBlank(message = "当前值不能为空")
    private String currentValue;

    private Boolean damaged = false;

    @Size(max = 255, message = "备注不能超过255字符")
    private String remark;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "读数凭证图片")
    private List<String> proofImageList;
}
