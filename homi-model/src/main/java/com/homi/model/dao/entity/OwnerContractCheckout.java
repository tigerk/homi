package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("owner_contract_checkout")
@Schema(name = "OwnerContractCheckout", description = "业主合同退房单")
public class OwnerContractCheckout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "SaaS企业ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "业主合同ID")
    @TableField("owner_contract_id")
    private Long ownerContractId;

    @Schema(description = "业主ID")
    @TableField("owner_id")
    private Long ownerId;

    @Schema(description = "合作模式")
    @TableField("cooperation_mode")
    private String cooperationMode;

    @Schema(description = "退房日期")
    @TableField("checkout_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkoutDate;

    @Schema(description = "退房原因")
    @TableField("checkout_reason")
    private String checkoutReason;

    @Schema(description = "结算说明")
    @TableField("settlement_remark")
    private String settlementRemark;

    @Schema(description = "是否释放房源")
    @TableField("release_subject")
    private Boolean releaseSubject;

    @Schema(description = "是否作废退房日之后未付款账单")
    @TableField("void_unpaid_future_bills")
    private Boolean voidUnpaidFutureBills;

    @Schema(description = "状态：1=已提交，2=已完成，3=已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "创建时间")
    @TableField("create_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;

    @Schema(description = "更新时间")
    @TableField("update_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
