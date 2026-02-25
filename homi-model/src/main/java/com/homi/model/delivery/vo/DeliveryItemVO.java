package com.homi.model.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
@Schema(description = "交割单物品VO")
public class DeliveryItemVO {
    @Schema(description = "交割单项目ID")
    private Long id;

    @Schema(description = "物品编码")
    private String itemCode;

    @Schema(description = "物品名称")
    private String itemName;

    @Schema(description = "项目分类")
    private String itemCategory;

    @Schema(description = "前值")
    private String preValue;

    @Schema(description = "当前值")
    private String currentValue;

    @Schema(description = "项目单位")
    private String itemUnit;

    @Schema(description = "是否损坏")
    private Boolean damaged;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否自定义项目")
    private boolean customized;
}
