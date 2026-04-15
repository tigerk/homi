package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("trial_application")
@Schema(name = "TrialApplication", description = "试用申请表")
public class TrialApplication implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("phone")
    private String phone;

    @TableField("region_id")
    private Long regionId;

    @TableField("city_name")
    private String cityName;

    @TableField("usage_remark")
    private String usageRemark;

    @TableField("status")
    private Integer status;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @TableField("create_by")
    private Long createBy;

    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;

    @TableField("update_by")
    private Long updateBy;
}
