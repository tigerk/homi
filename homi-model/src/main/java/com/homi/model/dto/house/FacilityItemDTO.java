package com.homi.model.dto.house;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/10/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityItemDTO {
    private String name;

    private String count;
}
