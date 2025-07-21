package com.homi.domain.dto.house;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

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
    private Long businessMode;

    @Schema(description = "城市id")
    private Long cityId;

    @Schema(description = "部门id", hidden = true)
    private Long deptId;

    @Schema(description = "项目编号")
    private String houseCode;

    @Schema(description = "项目名称")
    private String houseName;

    @Schema(description = "楼盘id")
    private String propertyId;

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
}
