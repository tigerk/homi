package com.homi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 通用文件资源表
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("file_attach")
@Schema(name = "FileAttach", description = "业务附件关联表")
public class FileAttach implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公司 ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业务类型，如 user_avatar, house_photo, contract_scan")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "关联的业务数据ID，比如用户ID、房源ID等")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "文件存储路径或访问URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "文件类型，如 image/png, image/jpeg")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "存储方式：0-本地、1-oss, qiniu, s3 等")
    @TableField("storage_type")
    private Integer storageType;

    @Schema(description = "是否删除：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;
}
