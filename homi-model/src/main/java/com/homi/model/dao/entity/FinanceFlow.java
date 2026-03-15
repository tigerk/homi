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
@TableName("finance_flow")
@Schema(name = "FinanceFlow", description = "财务流水表（业务层）")
public class FinanceFlow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId("id")
    private Long id;

    @Schema(description = "系统财务流水号")
    @TableField("flow_no")
    private String flowNo;

    @Schema(description = "公司ID")
    @TableField("company_id")
    private Long companyId;

    @Schema(description = "关联支付流水ID")
    @TableField("payment_flow_id")
    private Long paymentFlowId;

    @Schema(description = "业务类型")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务单据ID")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "业务单据编号")
    @TableField("biz_no")
    private String bizNo;

    @Schema(description = "流水类型")
    @TableField("flow_type")
    private String flowType;

    @Schema(description = "资金方向")
    @TableField("flow_direction")
    private String flowDirection;

    @Schema(description = "金额（分）")
    @TableField("amount")
    private Long amount;

    @Schema(description = "币种")
    @TableField("currency")
    private String currency;

    @Schema(description = "状态")
    @TableField("status")
    private String status;

    @Schema(description = "退款关联原始流水ID")
    @TableField("refund_flow_id")
    private Long refundFlowId;

    @Schema(description = "父流水ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "是否已拆分：0 否，1 是（仅主记录有效）")
    @TableField("is_split")
    private Integer isSplit;

    @Schema(description = "费用类型")
    @TableField("fee_type")
    private String feeType;

    @Schema(description = "关联费用ID")
    @TableField("fee_ref_id")
    private Long feeRefId;

    @Schema(description = "费用名称")
    @TableField("fee_name")
    private String feeName;

    @Schema(description = "流水发生时间")
    @TableField("flow_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date flowTime;

    @Schema(description = "付款方姓名")
    @TableField("payer_name")
    private String payerName;

    @Schema(description = "付款方手机号")
    @TableField("payer_phone")
    private String payerPhone;

    @Schema(description = "收款方名称")
    @TableField("receiver_name")
    private String receiverName;

    @Schema(description = "操作员工ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "操作员工姓名")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "扩展字段")
    @TableField("ext_json")
    private String extJson;

    @Schema(description = "是否删除：0 否，1 是")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Schema(description = "创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "创建人")
    @TableField("create_by")
    private Long createBy;

    @Schema(description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "更新人")
    @TableField("update_by")
    private Long updateBy;
}
