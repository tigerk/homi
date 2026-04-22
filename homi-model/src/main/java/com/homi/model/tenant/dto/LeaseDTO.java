package com.homi.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "租约信息")
public class LeaseDTO {
    @Schema(description = "租约ID")
    private Long id;

    @Schema(description = "租客ID（续签时传入已有租客ID）")
    private Long tenantId;

    @Schema(description = "上一份租约ID（续签时传入）")
    private Long parentLeaseId;

    @Schema(description = "合同模板ID")
    private Long contractTemplateId;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房")
    private Integer contractNature;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "房间ID列表")
    private List<Long> roomIds;

    @Schema(description = "租约房间配置")
    private List<LeaseRoomDTO> roomRentList;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "租金价格")
    private BigDecimal rentPrice;

    @Schema(description = "押金月数")
    private Integer depositMonths;

    @Schema(description = "支付周期（月）")
    private Integer paymentMonths;

    @Schema(description = "首期账单收租日")
    private Integer firstBillDay;

    @Schema(description = "租约开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租约结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkInAt;

    @Schema(description = "退租日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkOutAt;

    @Schema(description = "原租约开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseStart;

    @Schema(description = "原租约结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalLeaseEnd;

    @Schema(description = "租约时长天数")
    private Integer leaseDurationDays;

    @Schema(description = "租金到期类型")
    private Integer rentDueType;

    @Schema(description = "租金到期日")
    private Integer rentDueDay;

    @Schema(description = "租金到期偏移天数")
    private Integer rentDueOffsetDays;

    @Schema(description = "业务员ID")
    private Long salesmanId;

    @Schema(description = "协助人员ID")
    private Long helperId;

    @Schema(description = "签约状态")
    private Integer signStatus;

    @Schema(description = "退租状态")
    private Integer checkOutStatus;

    @Schema(description = "租约状态")
    private Integer status;

    @Schema(description = "租客来源")
    private Long tenantSource;

    @Schema(description = "成交渠道")
    private Long dealChannel;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

}
