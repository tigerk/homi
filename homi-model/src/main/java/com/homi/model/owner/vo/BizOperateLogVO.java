package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "业务操作日志VO")
public class BizOperateLogVO {
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "操作类型")
    private String operateType;

    @Schema(description = "操作描述")
    private String operateDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作前快照")
    private String beforeSnapshot;

    @Schema(description = "操作后快照")
    private String afterSnapshot;

    @Schema(description = "扩展数据")
    private String extraData;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;
}
