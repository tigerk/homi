package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 * 租客账单其他费用明细表
 * </p>
 *
 * @author tk
 * @since 2025-12-26
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_bill_other_fee")
@Schema(name = "TenantBillOtherFee", description = "租客账单其他费用明细表")
public class TenantBillOtherFee implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账单ID（关联 tenant_bill.id）")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "费用字典 ID")
    @TableField("dict_data_id")
    private Long dictDataId;

    @Schema(description = "费用项目名称（如 租金、水费、电费）")
    @TableField("dict_data_name")
    private String dictDataName;

    @Schema(description = "费用金额（计算结果）")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "备注信息")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除：0=否，1=是")
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
