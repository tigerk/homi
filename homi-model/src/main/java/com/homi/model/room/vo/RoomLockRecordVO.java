package com.homi.model.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(name = "RoomLockRecordVO", description = "房间锁房记录")
public class RoomLockRecordVO implements Serializable {

    @Schema(description = "锁房记录ID")
    private Long id;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "锁房原因: 1-永久锁房, 2-指定时间")
    private Integer lockReason;

    @Schema(description = "锁房原因名称")
    private String lockReasonName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "锁房备注")
    private String remark;

    @Schema(description = "锁房状态: 1-生效中, 0-已失效")
    private Integer lockStatus;

    @Schema(description = "锁房状态名称")
    private String lockStatusName;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建人名称")
    private String createByName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人ID")
    private Long updateBy;

    @Schema(description = "更新人名称")
    private String updateByName;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
