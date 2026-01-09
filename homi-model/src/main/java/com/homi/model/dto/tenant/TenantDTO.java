package com.homi.model.dto.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 合同数据传输对象
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/2
 */
@Data
public class TenantDTO {

    @Schema(description = "租客ID", example = "1")
    private BigInteger id;

    @Schema(description = "合同模板ID", example = "1")
    private Long contractTemplateId;

    @Schema(description = "合同编号", example = "C001")
    private String contractCode;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房", example = "1")
    private Integer contractNature;

    @Schema(description = "公司ID", example = "1")
    private Long companyId;

    @Schema(description = "部门ID", example = "D001")
    private Long deptId;

    @Schema(description = "房间ID列表", example = "[\"R001\", \"R002\"]")
    private List<Long> roomIds;

    @Schema(description = "租客类型ID", example = "1", hidden = true)
    private Long tenantTypeId;

    @Schema(description = "租客类型：0=个人，1=企业", example = "0")
    private Integer tenantType;

    @Schema(description = "租客名称（冗余字段，便于查询）", example = "张三")
    private String tenantName;

    @Schema(description = "租客联系电话（冗余字段）", example = "13800000000")
    private String tenantPhone;

    @Schema(description = "租金价格", example = "5000.00")
    private BigDecimal rentPrice;

    @Schema(description = "押金月数", example = "3")
    private Integer depositMonths;

    @Schema(description = "支付月数", example = "1")
    private Integer paymentMonths;

    @Schema(description = "首期账单收租日：0=跟随合同起租日，1=跟随合同创建日", example = "0")
    private Integer firstBillDay;

    @Schema(description = "租赁开始时间", example = "2025-01-01T00:00:00Z")
    private Date leaseStart;

    @Schema(description = "租赁结束时间", example = "2026-01-01T00:00:00Z")
    private Date leaseEnd;

    @Schema(description = "实际入住时间", example = "2025-01-05T00:00:00Z")
    private Date checkInTime;

    @Schema(description = "实际搬离时间", example = "2026-01-05T00:00:00Z")
    private Date checkOutTime;

    @Schema(description = "初始录入租赁开始时间", example = "2025-01-01T00:00:00Z")
    private Date originalLeaseStart;

    @Schema(description = "初始录入租赁结束时间", example = "2026-01-01T00:00:00Z")
    private Date originalLeaseEnd;

    @Schema(description = "累计租房天数", example = "365")
    private Integer leaseDurationDays;

    @Schema(description = "收租类型：1=提前，2=固定，3=延后", example = "2")
    private Integer rentDueType;

    @Schema(description = "固定收租日（1-31，0=当月最后一天）", example = "15")
    private Integer rentDueDay;

    @Schema(description = "收租偏移天数（提前/延后）", example = "7")
    private Integer rentDueOffsetDays;

    @Schema(description = "业务人员ID", example = "1")
    private Long salesmanId;

    @Schema(description = "协助人员ID", example = "2")
    private Long helperId;

    @Schema(description = "签约状态：0=待签字、1=已签字", example = "1")
    private Integer signStatus;

    @Schema(description = "租户退租状态：0=未退租、1=正常退、2=换房退、3=违约退、4=作废", example = "0")
    private Integer checkOutStatus;

    @Schema(description = "合同状态：0=未生效，1=生效中，2=已退租，3=已逾期，4=已作废", example = "1")
    private Integer status;

    @Schema(description = "租客来源", example = "1")
    private Integer tenantSource;

    @Schema(description = "成交渠道", example = "1")
    private Long dealChannel;

    @Schema(description = "合同备注", example = "这是一个测试备注")
    private String remark;

    @Schema(description = "创建人ID", hidden = true)
    private Long createBy;
}
