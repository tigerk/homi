package com.homi.model.room.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.booking.vo.BookingListVO;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.room.dto.price.PriceConfigDTO;
import com.homi.model.tenant.vo.LeaseDetailVO;
import com.homi.model.tenant.vo.LeaseLiteVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
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
@Schema(description = "房间详情DTO，包含房间基本信息和价格配置")
public class RoomDetailVO {
    @Schema(description = "房间id")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "房间类型")
    private Integer roomType;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "出租价格")
    private BigDecimal price;

    @Schema(description = "可出租日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date availableDate;

    @Schema(description = "空置开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date vacancyStartTime;

    @Schema(description = "房间状态")
    private Integer roomStatus;

    @Schema(description = "房间备注")
    private String remark;

    @Schema(description = "设施、从字典dict_data获取并配置")
    private List<FacilityItemDTO> facilities;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "图片列表")
    private List<String> imageList;

    @Schema(description = "视频列表")
    private List<String> videoList;

    @Schema(description = "房间价格配置")
    private PriceConfigDTO priceConfig;

    @Schema(description = "租约信息")
    private LeaseLiteVO lease;

    @Schema(description = "房间预订信息")
    private BookingListVO booking;
}
