package com.homi.model.company.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "企业可订购服务项")
public class CompanyProductOrderVO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品编码")
    private String productCode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "最小购买数量")
    private Integer minQuantity;

    @Schema(description = "介绍")
    private String description;

    @Schema(description = "剩余可用配额")
    private Integer remainQuota;

    @Schema(description = "总配额")
    private Integer totalQuota;

    @Schema(description = "已使用配额")
    private Integer usedQuota;

    @Schema(description = "冻结配额")
    private Integer frozenQuota;
}
