package com.homi.model.booking.vo;

import com.homi.model.vo.tenant.TenantTotalItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
@Schema(description = "预约状态统计")
public class BookingTotalVO implements Serializable {

    @Schema(description = "状态统计")
    private List<BookingTotalItemVO> statusList;
}
