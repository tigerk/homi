package com.homi.model.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Schema(description = "公司创建对象")
@AllArgsConstructor
@NoArgsConstructor
public class DeptCreateDTO {
    /**
     * 主键ID
     */
    @Schema(description = "id，修改时需要传")
    private Long id;

    /**
     * 公司Id
     */
    private Long companyId;

    @Schema(description = "部门名称")
    private String name;

    /**
     * 父节点id
     */
    @Schema(description = "父节点id")
    private Long parentId;

    /**
     * 部门负责人
     */
    @Schema(description = "部门负责人")
    private String principal;

    /**
     * 部门负责人手机号
     */
    @Schema(description = "部门负责人手机号")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    private Integer sort;

    /**
     * 状态（1，0不启用）
     */
    @Schema(description = "状态（1，0不启用）")
    private Integer status;

    /**
     * 是否为门店（1，0不是门店）
     */
    @Schema(description = "是否为门店（1，0不是门店）")
    private Boolean isStore;

    /**
     * 备注
     */
    @Schema(description = "备注")
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
