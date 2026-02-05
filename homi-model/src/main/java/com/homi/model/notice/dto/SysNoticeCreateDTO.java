package com.homi.model.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "系统公告创建/更新DTO")
public class SysNoticeCreateDTO {
    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "公告标题")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "公告内容（富文本）")
    @NotBlank(message = "公告内容不能为空")
    private String content;

    @Schema(description = "类型：1=系统公告 2=运营通知")
    @NotNull(message = "公告类型不能为空")
    private Integer noticeType;

    @Schema(description = "发布范围：1=全员 2=房东 3=租客 4=指定角色")
    private Integer targetScope;

    @Schema(description = "0=草稿 1=已发布 2=已撤回")
    private Integer status;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "指定角色ID列表（targetScope=4时使用）")
    private List<Long> roleIds;

    @Schema(description = "创建者")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新者")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
