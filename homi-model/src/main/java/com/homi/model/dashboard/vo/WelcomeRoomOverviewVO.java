package com.homi.model.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "欢迎页房源概况")
public class WelcomeRoomOverviewVO {
    @Schema(description = "房源租赁类型：1=集中式，2=分散式")
    private Integer leaseMode;

    @Schema(description = "房源租赁类型名称")
    private String leaseModeName;

    @Schema(description = "房间总数")
    private Integer total;

    @Schema(description = "空置数量")
    private Integer availableCount;

    @Schema(description = "配置中数量")
    private Integer preparingCount;

    @Schema(description = "已租数量")
    private Integer leasedCount;

    @Schema(description = "即将搬入数量（30天内）")
    private Integer upcomingCheckInCount;

    @Schema(description = "即将搬出数量（30天内）")
    private Integer upcomingCheckOutCount;

    @Schema(description = "到期未退数量")
    private Integer overdueCheckOutCount;

    @Schema(description = "出租率")
    private BigDecimal occupancyRate;
}
