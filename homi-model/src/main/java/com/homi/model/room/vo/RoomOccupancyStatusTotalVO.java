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
public class RoomOccupancyStatusTotalVO {
    /**
     * 房间占用状态
     */
    private Integer occupancyStatus;
    /**
     * 数量
     */
    private Integer total;
}
