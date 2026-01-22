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

    private Long id;

    private String itemCode;

    private String itemName;

    private String itemCategory;

    private String preValue;

    private String currentValue;

    private String itemUnit;

    private Boolean damaged;

    private String remark;

    private Integer sortOrder;
}
