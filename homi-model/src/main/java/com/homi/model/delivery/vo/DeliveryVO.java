package com.homi.model.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Data
@Schema(description = "交割单VO")
public class DeliveryVO {

    private Long id;

    private String subjectType;

    private Long subjectTypeId;

    private Long roomId;

    private String handoverType;

    private Integer status;

    private LocalDate handoverDate;

    private Long inspectorId;

    private String inspectorName;

    private String remark;

    private List<DeliveryItemVO> items;

    private List<String> imageList;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
