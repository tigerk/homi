package com.homi.domain.vo.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/6/16
 */

@Data
@Schema(description = "公司套餐列表")
public class CompanyPackageVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 关联菜单id
     */
    private List<Long> packageMenus;

    /**
     * 状态（0正常，-1禁用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Long updateBy;
}
