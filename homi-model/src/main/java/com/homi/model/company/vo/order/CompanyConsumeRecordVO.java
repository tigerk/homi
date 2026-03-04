package com.homi.model.company.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "企业服务使用记录")
public class CompanyConsumeRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "消费流水号")
    private String consumeNo;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "商品编码")
    private String productCode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "业务单号")
    private String bizNo;

    @Schema(description = "使用数量")
    private Integer quantity;

    @Schema(description = "状态：1成功，2失败，3已退还")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "记录时间")
    private Date createTime;
}
