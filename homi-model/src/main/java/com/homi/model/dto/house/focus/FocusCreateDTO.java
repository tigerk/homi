package com.homi.model.dto.house.focus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.dto.community.CommunityDTO;
import com.homi.model.dto.house.HouseLayoutDTO;
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

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "房源租赁类型：1、集中式；2、整租、3、合租")
    private Integer leaseMode;

    @Schema(description = "项目编号")
    private String focusCode;

    @Schema(description = "项目名称")
    private String focusName;

    @Schema(description = "住宅小区")
    private CommunityDTO community;

    @Schema(description = "项目地址")
    private String address;

    @Schema(description = "楼栋列表")
    private List<FocusBuildingDTO> buildings;

    @Schema(description = "户型列表")
    private List<HouseLayoutDTO> houseLayoutList;

    @Schema(description = "房间列表")
    private List<FocusHouseDTO> houseList;

    @Schema(description = "门店联系电话")
    private String storePhone;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "业务员ID")
    private Long salesmanId;

    @Schema(description = "水")
    private String water;

    @Schema(description = "电")
    private String electricity;

    @Schema(description = "供暖")
    private String heating;

    @Schema(description = "是否有电梯")
    private Boolean hasElevator;

    @Schema(description = "是否有燃气")
    private Boolean hasGas;

    @Schema(description = "房间数 为0表示未分配房间")
    private Integer roomCount;

    @Schema(description = "房源描述、项目介绍")
    private String houseDesc;

    @Schema(description = "商圈介绍、广告语")
    private String businessDesc;

    @Schema(description = "项目描述")
    private String remark;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "设施、从字典dict_data获取并配置")
    private List<String> facilities;

    @Schema(description = "图片列表")
    private List<String> imageList;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
