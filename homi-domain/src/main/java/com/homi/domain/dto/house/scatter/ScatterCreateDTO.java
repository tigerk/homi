package com.homi.domain.dto.house.scatter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.domain.dto.community.CommunityDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "分散式房源创建DTO")
public class ScatterCreateDTO {
    /**
     * 主键ID
     */
    @Schema(description = "id，修改时需要传")
    private Long id;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "房源租赁类型：1、集d中式；2、分散式")
    private Integer leaseMode;

    @Schema(description = "出租类型：1=整租，2=合租")
    private Integer rentalType;

    @Schema(description = "住宅小区")
    private CommunityDTO community;

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

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "房间列表")
    private List<ScatterHouseDTO> houseList;
}
