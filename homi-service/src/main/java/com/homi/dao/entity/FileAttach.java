package com.homi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * 业务附件关联表
 * </p>
 *
 * @author tk
 * @since 2025-11-05
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

    @Schema(description = "关联的业务数据ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "文件访问URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "文件类型，如 image/png, image/jpeg")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "存储方式：0-本地，1-OSS，2-S3，3-其他")
    @TableField("storage_type")
    private Integer storageType;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "逻辑删除标记：0 否，1 是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
