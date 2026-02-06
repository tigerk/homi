package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 租约表
 * </p>
 *
 * @author tk
 * @since 2026-02-06
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("lease")
@Schema(name = "Lease", description = "租约表")
public class Lease implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租约 ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "租客 ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "上一份租约 ID")
    @TableField("parent_lease_id")
    private Long parentLeaseId;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房")
    @TableField("contract_nature")
    private Integer contractNature;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "部门 ID")
    @TableField("dept_id")
    private Long deptId;

    @Schema(description = "房间 ids")
    @TableField("room_ids")
    private String roomIds;

    @Schema(description = "租金价格")
    @TableField("rent_price")
    private BigDecimal rentPrice;

    @Schema(description = "押金月数")
    @TableField("deposit_months")
    private Integer depositMonths;

    @Schema(description = "支付周期（月）")
    @TableField("payment_months")
    private Integer paymentMonths;

    @Schema(description = "租赁开始时间")
    @TableField("lease_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租赁结束时间")
    @TableField("lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "实际入住时间")
    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkInTime;

    @Schema(description = "实际搬离时间")
    @TableField("check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkOutTime;

    @Schema(description = "初始录入租赁开始时间")
    @TableField("original_lease_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date originalLeaseStart;

    @Schema(description = "初始录入租赁结束时间")
    @TableField("original_lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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

    @Schema(description = "签约状态：0=待签字、1=已签字")
    @TableField("sign_status")
    private Integer signStatus;

    @Schema(description = "租户退租状态：0=未退租、1=正常退、2=换房退、3=违约退、4=作废")
    @TableField("check_out_status")
    private Integer checkOutStatus;

    @Schema(description = "合同状态：0=待签字，1=在租中，2=已退租，3=已作废")
    @TableField("status")
    private Integer status;

    @Schema(description = "审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回")
    @TableField("approval_status")
    private Integer approvalStatus;

    @Schema(description = "租客来源")
    @TableField("tenant_source")
    private Long tenantSource;

    @Schema(description = "成交渠道")
    @TableField("deal_channel")
    private Long dealChannel;

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
