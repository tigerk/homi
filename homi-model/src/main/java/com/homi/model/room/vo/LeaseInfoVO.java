package com.homi.model.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/16
 */
@Data
@Builder
@Schema(description = "房间的租约信息，包括预定")
public class LeaseInfoVO {
    private Long leaseId;
    private Long bookingId;

    @Schema(description = "租约开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseStartDate;

    @Schema(description = "租约结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseEndDate;

    @Schema(description = "欠费天数")
    private Integer arrearsDays;

    @Schema(description = "租户姓名")
    private String tenantName;

    @Schema(description = "租户手机号")
    private String tenantPhone;
}
