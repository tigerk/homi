package com.homi.model.booking.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "预定ID查询 DTO")
public class BookingIdDTO extends PageDTO implements Serializable {
    @Schema(description = "预定ID")
    private Long bookingId;
}
