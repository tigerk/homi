package com.homi.model.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送站内信DTO")
public class SysMessageSendDTO {
    @Schema(description = "接收人ID")
    @NotNull(message = "接收人不能为空")
    private Long receiverId;

    @Schema(description = "消息标题")
    @NotBlank(message = "消息标题不能为空")
    private String title;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "消息类型")
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;
}
