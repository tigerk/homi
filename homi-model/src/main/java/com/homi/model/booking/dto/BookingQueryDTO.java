package com.homi.model.booking.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "预定查询参数")
public class BookingQueryDTO extends PageDTO implements Serializable {
    @Schema(description = "客户姓名")
    private String tenantName;

    @Schema(description = "联系电话")
    private String tenantPhone;

    @Schema(description = "预定状态")
    private Integer bookingStatus;


}
