package com.homi.model.notice.vo;

import com.homi.model.dao.entity.SysMessage;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.entity.SysTodo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统通知最近数据")
public class RecentNoticeVO {
    @Schema(description = "个人消息")
    private List<SysMessage> messages;

    @Schema(description = "系统公告")
    private List<SysNotice> notices;

    @Schema(description = "待办消息")
    private List<SysTodo> todos;

    @Schema(description = "未读消息数量")
    private Long unreadMessageCount;

    @Schema(description = "未读公告数量")
    private Long unreadNoticeCount;

    @Schema(description = "未处理待办数量")
    private Long pendingTodoCount;
}
