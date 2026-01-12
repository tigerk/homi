package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * 预定/定金表
 * </p>
 *
 * @author tk
 * @since 2026-01-09
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("booking")
@Schema(name = "Booking", description = "预定/定金表")
public class Booking implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "预定 ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "预定房间 ids")
    @TableField("room_ids")
    private String roomIds;

    @Schema(description = "租客类型：0=个人，1=企业")
    @TableField("tenant_type")
    private Integer tenantType;

    @Schema(description = "客户姓名")
    @TableField("tenant_name")
    private String tenantName;

    @Schema(description = "联系电话")
    @TableField("tenant_phone")
    private String tenantPhone;

    @Schema(description = "预定金金额")
    @TableField("booking_amount")
    private BigDecimal bookingAmount;

    @Schema(description = "预定时间")
    @TableField("booking_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bookingTime;

    @Schema(description = "预定到期时间（超过此时间未签合同可视为违约/过期）")
    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;

    @Schema(description = "预计租赁开始时间")
    @TableField("expected_lease_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedLeaseStart;

    @Schema(description = "预计租赁结束时间")
    @TableField("expected_lease_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedLeaseEnd;

    @Schema(description = "谈定的意向租金")
    @TableField("expected_rent_price")
    private BigDecimal expectedRentPrice;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业务人员ID")
    @TableField("salesman_id")
    private Long salesmanId;

    @Schema(description = "预定状态：1=预定中，2=已转合同，3=客户违约（没收定金），4=业主违约（退还定金），5=已取消/过期")
    @TableField("booking_status")
    private Integer bookingStatus;

    @Schema(description = "转合同后关联的租客表 ID")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "取消/过期原因备注")
    @TableField("cancel_reason")
    private String cancelReason;

    @Schema(description = "实际操作取消的时间")
    @TableField("cancel_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

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
