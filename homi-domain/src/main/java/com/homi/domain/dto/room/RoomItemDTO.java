package com.homi.domain.dto.room;

import com.homi.domain.dto.house.HouseLayoutDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
public class RoomItemDTO {
    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "房源ID")
    private Long houseId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "房源编号")
    private String houseCode;

    @Schema(description = "房源名称")
    private String houseName;

    @Schema(description = "来源id")
    private Long modeRefId;

    @Schema(description = "房源租赁类型：1、集中式；2、整租、3、合租")
    private Integer leaseMode;

    @Schema(description = "房型")
    private HouseLayoutDTO houseLayout;

    @Schema(description = "部门id")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "楼栋号")
    private String building;

    @Schema(description = "单元号")
    private String unit;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "出租价格")
    private BigDecimal price;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "朝向")
    private String direction;

    @Schema(description = "房间状态")
    private Integer roomStatus;

    @Schema(description = "房间状态，参考：RoomStatusEnum")
    private String roomStatusName;

    @Schema(description = "房间状态颜色，参考：RoomStatusEnum")
    private String roomStatusColor;

    @Schema(description = "锁定状态")
    private Boolean locked;

    @Schema(description = "禁用状态：是否已禁用")
    private Boolean closed;

    @Schema(description = "出租状态")
    private Boolean leased;

    @Schema(description = "负责人id")
    private String salesmanId;

    @Schema(description = "负责人姓名")
    private String salesmanName;

    @Schema(description = "负责人手机号")
    private String salesmanPhone;
}
