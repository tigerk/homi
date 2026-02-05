package com.homi.model.notice.vo;

import com.homi.model.dao.entity.SysNotice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统公告详情")
public class SysNoticeDetailVO {
    @Schema(description = "公告详情")
    private SysNotice notice;

    @Schema(description = "指定角色ID列表")
    private List<Long> roleIds;
}
