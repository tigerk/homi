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
 * 租赁合同信息表
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_contract")
@Schema(name = "TenantContract", description = "租赁合同信息表")
public class TenantContract implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "合同ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租客ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "房源ID")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "合同编号")
    @TableField("contract_code")
    private String contractCode;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房")
    @TableField("contract_nature")
    private Integer contractNature;

    @Schema(description = "租金价格")
    @TableField("rental_price")
    private BigDecimal rentalPrice;

    @Schema(description = "押金月数")
    @TableField("deposit_month")
    private Integer depositMonth;

    @Schema(description = "支付周期（月）")
    @TableField("payment_cycle_month")
    private Integer paymentCycleMonth;

    @Schema(description = "租赁开始时间")
    @TableField("lease_start")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租赁结束时间")
    @TableField("lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "实际入住时间")
    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkInTime;

    @Schema(description = "实际搬离时间")
    @TableField("check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkOutTime;

    @Schema(description = "初始录入租赁开始时间")
    @TableField("original_lease_start")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseStart;

    @Schema(description = "初始录入租赁结束时间")
    @TableField("original_lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseEnd;

    @Schema(description = "累计租房天数")
    @TableField("lease_duration_days")
    private Integer leaseDurationDays;

    @Schema(description = "收租类型：1=提前，2=固定，3=延后")
    @TableField("rent_due_type")
    private Integer rentDueType;

    @Schema(description = "固定收租日（1-31，0=当月最后一天）")
    @TableField("rent_due_day")
    private Integer rentDueDay;

    @Schema(description = "收租偏移天数（提前/延后）")
    @TableField("rent_due_offset_days")
    private Integer rentDueOffsetDays;

    @Schema(description = "业务人员ID")
    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "协助人员ID")
    @TableField("helper_id")
    private Long helperId;

    @Schema(description = "合同状态：0=未生效，1=生效中，2=已退租，3=已逾期，4=已作废")
    @TableField("status")
    private Integer status;

    @Schema(description = "合同备注")
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
