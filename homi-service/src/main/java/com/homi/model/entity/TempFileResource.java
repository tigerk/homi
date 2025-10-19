package com.homi.model.entity;

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
 * 临时文件资源表（防孤儿文件）
 * </p>
 *
 * @author tk
 * @since 2025-10-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("temp_file_resource")
@Schema(name = "TempFileResource", description = "临时文件资源表（防孤儿文件）")
public class TempFileResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "上传标识，用于前端和后端追踪文件归属")
    @TableField("upload_token")
    private String uploadToken;

    @Schema(description = "文件路径或URL")
    @TableField("file_url")
    private String fileUrl;

    @Schema(description = "是否已被业务使用：0=未使用，1=已使用")
    @TableField("is_used")
    private Integer isUsed;

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
