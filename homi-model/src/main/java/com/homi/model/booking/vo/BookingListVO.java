package com.homi.model.booking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingListVO implements Serializable {
    @Schema(description = "预定 ID")
    private Long id;

    @Schema(description = "预定编号")
    private String bookingCode;

    @Schema(description = "预定房间 ids")
    private List<Long> roomIds;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "客户姓名")
    private String tenantName;

    @Schema(description = "联系电话")
    private String tenantPhone;

    @Schema(description = "预定金金额")
    private BigDecimal bookingAmount;

    @Schema(description = "预定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bookingTime;

    @Schema(description = "预定到期时间（超过此时间未签合同可视为违约/过期）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;

    @Schema(description = "预计租赁开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedLeaseStart;

    @Schema(description = "预计租赁结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedLeaseEnd;

    @Schema(description = "谈定的意向租金")
    private BigDecimal expectedRentPrice;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "业务人员ID")
    private Long salesmanId;

    @Schema(description = "预定状态：1=预定中，2=已转合同，3=客户违约（没收定金），4=业主违约（退还定金），5=已取消/过期")
    private Integer bookingStatus;

    @Schema(description = "预定状态名称")
    private String bookingStatusName;

    @Schema(description = "转合同后关联的租客表 ID")
    private Long tenantId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "修改人ID")
    private Long updateBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
