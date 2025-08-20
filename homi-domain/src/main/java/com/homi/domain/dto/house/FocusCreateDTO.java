package com.homi.domain.dto.house;

import com.homi.domain.dto.room.HouseLayoutDTO;
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

    @Schema(description = "经营模式", hidden = true)
    private Integer businessMode;

    @Schema(description = "区域id")
    private Long regionId;

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


    @Schema(description = "总楼层")
    private Integer floorTotal;

    @Schema(description = "房间号前缀")
    private String roomPrefix;

    @Schema(description = "房间号长度")
    private Integer roomNumberLength;

    @Schema(description = "去掉4")
    private Boolean excludeFour;

    @Schema(description = "关闭的楼层列表")
    private List<Integer> closedFloors;

    @Schema(description = "户型列表")
    private List<HouseLayoutDTO> houseLayoutList;

    @Schema(description = "房间列表")
    private List<FocusRoomDTO> roomList;

    @Schema(description = "业务员id")
    private Long salesmanId;

    @Schema(description = "门店联系电话")
    private String storePhone;

    @Schema(description = "水")
    private String water;

    @Schema(description = "电")
    private String electricity;

    @Schema(description = "供暖")
    private String heating;

    @Schema(description = "燃气")
    private Boolean hasGas;

    @Schema(description = "电梯")
    private Boolean hasElevator;

    private List<String> facilities;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "项目文件列表")
    private List<String> imageList;

    @Schema(description = "房源描述、项目介绍")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    private String businessDesc;

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

}
