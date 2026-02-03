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
 * 退租费用明细表
 * </p>
 *
 * @author tk
 * @since 2026-02-03
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_checkout_fee")
@Schema(name = "TenantCheckoutFee", description = "退租费用明细表")
public class TenantCheckoutFee implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "退租单ID")
    @TableField("checkout_id")
    private Long checkoutId;

    @Schema(description = "费用类型：1=欠缴租金，2=欠缴杂费，3=水电燃气，4=物品损坏，5=违约金，6=清洁费，7=其他扣款，8=多收租金退还，9=押金退还")
    @TableField("fee_type")
    private Integer feeType;

    @Schema(description = "费用名称")
    @TableField("fee_name")
    private String feeName;

    @Schema(description = "费用金额（正数）")
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @Schema(description = "方向：1=扣款（租客应付），2=退款（退还租客）")
    @TableField("fee_direction")
    private Integer feeDirection;

    @Schema(description = "关联账单ID（如有）")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否删除")
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
