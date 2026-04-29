package com.homi.model.owner.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "业主列表VO")
public class OwnerListVO {
    @Schema(description = "合同ID")
    private Long contractId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "业主类型")
    private Integer ownerType;

    @Schema(description = "业主名称")
    private String ownerName;

    @Schema(description = "业主联系电话")
    private String ownerPhone;

    @Schema(description = "业主标签")
    private String ownerTag;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合作模式")
    private String cooperationMode;

    @Schema(description = "合同模板名称")
    private String contractTemplateName;

    @Schema(description = "合同房源名称列表")
    private String subjectNames;

    @Schema(description = "合同房源数量")
    private Integer subjectCount;

    @Schema(description = "总面积")
    private BigDecimal totalArea;

    @Schema(description = "已配置房源数")
    private Integer configuredSubjectCount;

    @Schema(description = "合同开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractStart;

    @Schema(description = "合同结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contractEnd;

    @Schema(description = "签署状态")
    private Integer signStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "合同性质：1=新签，2=续约")
    private Integer contractNature;

    @Schema(description = "退房状态：0=未退房，1=已退房")
    private Integer checkoutStatus;

    @Schema(description = "退房日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkoutDate;

    @Schema(description = "退房原因")
    private String checkoutReason;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;
}
