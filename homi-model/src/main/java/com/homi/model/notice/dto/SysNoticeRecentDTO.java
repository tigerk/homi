package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统通知最近数据查询DTO")
public class SysNoticeRecentDTO {
    @Schema(description = "最近天数")
    private Integer days = 3;
}
