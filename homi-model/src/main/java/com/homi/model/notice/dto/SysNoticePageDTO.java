package com.homi.model.notice.dto;

import com.homi.common.lib.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "系统通知分页查询DTO")
public class SysNoticePageDTO extends PageDTO {
    @Schema(description = "关键词（标题/内容）")
    private String keyword;
}
