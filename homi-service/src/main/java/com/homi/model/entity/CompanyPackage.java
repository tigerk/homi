package com.homi.model.entity;

    import com.baomidou.mybatisplus.annotation.TableLogic;
    import com.baomidou.mybatisplus.annotation.TableName;
    import java.io.Serializable;
    import java.util.Date;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.ToString;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* <p>
    * 公司套餐表
    * </p>
*
* @author tk
* @since 2025-06-16
*/
    @EqualsAndHashCode(callSuper = false)
    @Data
    @ToString(callSuper = true)
    @TableName("company_package")
    public class CompanyPackage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
        private String packageMenus;

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

            /**
            * 是否删除（0否1是）
            */
        @TableLogic
        private Integer deleted;
}
