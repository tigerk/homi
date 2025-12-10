package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 文件资源表（防孤儿文件）
 * </p>
 *
 * @author tk
 * @since 2025-11-05
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("file_meta")
@Schema(name = "FileMeta", description = "文件资源表（防孤儿文件）")
public class FileMeta implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "文件存储路径或访问URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "文件名")
    @TableField("file_name")
    private String fileName;

    @Schema(description = "文件内容MD5")
    @TableField("file_hash")
    private String fileHash;

    @Schema(description = "文件类型，如 image/png, image/jpeg")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "存储方式：0-本地、1-oss, qiniu, s3 等")
    @TableField("storage_type")
    private Integer storageType;

    @Schema(description = "是否已被业务使用：0=未使用，1=已使用")
    @TableField("is_used")
    private Integer isUsed;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;
}
