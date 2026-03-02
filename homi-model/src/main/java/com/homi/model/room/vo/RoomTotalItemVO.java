package com.homi.model.room.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTotalItemVO {
    /**
     * 展示名称
     */
    private String roomStatusName;
    /**
     * 展示颜色
     */
    private String roomStatusColor;
    /**
     * 数量
     */
    private Integer total;
    /**
     * 筛选类型，前端根据此字段决定用什么条件查询
     * 0 / 1 / 2
     */
    private Integer filterType;
    /**
     * filterType = 0 时有值（0~3）
     * filterType = 1 / 2 时为 null
     */
    private Integer roomStatus;
}
