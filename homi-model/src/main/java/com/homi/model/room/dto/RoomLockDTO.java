package com.homi.model.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "房间锁房 DTO")
public class RoomLockDTO implements Serializable {
    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "锁房原因: 1-永久锁房, 2-指定时间")
    private Integer lockReason;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startAt;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "更新人ID", hidden = true)
    private Long updateBy;
}
