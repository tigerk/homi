package com.homi.model.entity;

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
 * 租客账单明细表
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_bill_detail")
@Schema(name = "TenantBillDetail", description = "租客账单明细表")
public class TenantBillDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "账单ID（关联 tenant_bill.id）")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "费用类型：1=租金，2=押金，3=水费，4=电费，5=物业费，6=卫生费，7=网络费，8=其他")
    @TableField("item_type")
    private Integer itemType;

    @Schema(description = "费用项目名称（如 租金、水费、电费）")
    @TableField("item_name")
    private String itemName;

    @Schema(description = "计费方式：1=固定金额，2=单价×数量，3=比例计费")
    @TableField("charge_mode")
    private Integer chargeMode;

    @Schema(description = "单价（charge_mode=2 时有效）")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "数量（charge_mode=2 时有效）")
    @TableField("quantity")
    private BigDecimal quantity;

    @Schema(description = "费率（charge_mode=3 时有效）")
    @TableField("rate")
    private BigDecimal rate;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "修改时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updateTime;
}
