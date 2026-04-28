package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主退房在租房间信息VO")
public class OwnerCheckoutLeaseRoomVO {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间名称")
    private String roomName;

    @Schema(description = "租约ID")
    private Long leaseId;

    @Schema(description = "租客ID")
    private Long tenantId;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

    @Schema(description = "租客合同状态")
    private Integer leaseStatus;

    @Schema(description = "租客合同状态名称")
    private String leaseStatusName;

    @Schema(description = "房间月租金")
    private BigDecimal rentPrice;

    @Schema(description = "租约开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租约结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;
}
