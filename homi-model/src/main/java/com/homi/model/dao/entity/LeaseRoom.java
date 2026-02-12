package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 租约-房间关联表
 * </p>
 *
 * @author tk
 * @since 2026-02-13
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease_room")
@Schema(name = "LeaseRoom", description = "租约-房间关联表")
public class LeaseRoom implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    @Schema(description = "租约ID")
    @TableField("lease_id")
    private Long leaseId;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;
}
