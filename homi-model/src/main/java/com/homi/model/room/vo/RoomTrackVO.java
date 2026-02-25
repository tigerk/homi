package com.homi.model.room.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/2/26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "房间跟进记录VO")
public class RoomTrackVO implements Serializable {
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    private Long roomId;

    @Schema(description = "跟进记录")
    private String trackContent;

    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long updateBy;
    private String updateByName;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
