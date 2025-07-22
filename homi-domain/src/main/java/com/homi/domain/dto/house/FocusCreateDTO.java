package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "集中式房源创建DTO")
public class FocusCreateDTO {
    /**
     * 主键ID
     */
    @Schema(description = "id，修改时需要传")
    private Long id;

    /**
     * 公司Id
     */
    @Schema(description = "公司id", hidden = true)
    private Long companyId;

    @Schema(description = "经营模式")
    private Integer businessMode;

    @Schema(description = "城市id")
    private Long cityId;

    @Schema(description = "部门id", hidden = true)
    private Long deptId;

    @Schema(description = "项目编号")
    private String houseCode;

    @Schema(description = "项目名称")
    private String houseName;

    @Schema(description = "楼盘id")
    private Long propertyId;

    @Schema(description = "项目地址")
    private String address;

    @Schema(description = "经度")
    private String lng;

    @Schema(description = "纬度")
    private String lat;

    @Schema(description = "楼栋")
    private String building;

    @Schema(description = "单元号")
    private String unit;

    @Schema(description = "门牌号")
    private String doorNumber;

    @Schema(description = "业务员id")
    private Long salesmanId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", hidden = true)
    private Date createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", hidden = true)
    private Long createBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", hidden = true)
    private Date updateTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人", hidden = true)
    private Long updateBy;

    @Schema(description = "总楼层")
    private Integer floorTotal;

    @Schema(description = "朝向")
    private Integer orientation;

    @Schema(description = "是否有电梯")
    private Boolean hasElevator;

    @Schema(description = "是否有燃气")
    private Boolean hasGas;

    @Schema(description = "房间数量", hidden = true)
    private Integer roomNumber;

    @Schema(description = "房间号前缀")
    private String prefix;

    @Schema(description = "房间号长度")
    private Integer roomNoLength;

    @Schema(description = "去掉4")
    private Boolean excludeFour;

    @Schema(description = "是否已分配")
    private Boolean allocated;

    @Schema(description = "房间户型")
    private List<FocusRoomLayoutDTO> roomLayouts;

}
