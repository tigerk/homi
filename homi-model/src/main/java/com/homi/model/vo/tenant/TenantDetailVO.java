package com.homi.model.vo.tenant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.homi.model.dto.room.price.OtherFeeDTO;
import com.homi.model.vo.contract.TenantContractVO;
import com.homi.model.vo.room.RoomListVO;
import com.homi.model.vo.tenant.bill.TenantBillListVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "租客详情VO")
public class TenantDetailVO {
    @Schema(description = "租客 ID")
    private Long id;

    @Schema(description = "合同编号")
    private String contractCode;

    @Schema(description = "合同性质：1=新签，2=续签，3=转租，4=换房")
    private Integer contractNature;

    @Schema(description = "公司ID")
    private Long companyId;

    @Schema(description = "部门 ID")
    private Long deptId;
    private String deptName;

    @Schema(description = "房间 ids")
    private String roomIds;

    @Schema(description = "租客类型关联ID")
    private Long tenantTypeId;

    @Schema(description = "租客类型：0=个人，1=企业")
    private Integer tenantType;

    @Schema(description = "租客个人信息")
    private TenantPersonalVO tenantPersonal;

    @Schema(description = "租客企业信息")
    private TenantCompanyVO tenantCompany;

    @Schema(description = "合同房间列表")
    private List<RoomListVO> roomList;

    @Schema(description = "租客名称（冗余字段，便于查询）")
    private String tenantName;

    @Schema(description = "租客联系电话（冗余字段）")
    private String tenantPhone;

    @Schema(description = "租金价格")
    private BigDecimal rentPrice;

    @Schema(description = "押金月数")
    private Integer depositMonths;

    @Schema(description = "支付周期（月）")
    private Integer paymentMonths;

    @Schema(description = "租赁开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseStart;

    @Schema(description = "租赁结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date leaseEnd;

    @Schema(description = "实际入住时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkInTime;

    @Schema(description = "实际搬离时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkOutTime;

    @Schema(description = "初始录入租赁开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date originalLeaseStart;

    @Schema(description = "初始录入租赁结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date originalLeaseEnd;

    @Schema(description = "累计租房天数")
    private Integer leaseDurationDays;

    @Schema(description = "收租类型：1=提前，2=固定，3=延后")
    private Integer rentDueType;

    @Schema(description = "固定收租日（1-31，0=当月最后一天）")
    private Integer rentDueDay;

    @Schema(description = "收租偏移天数（提前/延后）")
    private Integer rentDueOffsetDays;

    @Schema(description = "业务人员ID")
    private Long salesmanId;
    private String salesmanName;

    @Schema(description = "协助人员ID")
    private Long helperId;

    @Schema(description = "签约状态：0=待签字、1=已签字")
    private Integer signStatus;

    @Schema(description = "租户退租状态：0=未退租、1=正常退、2=换房退、3=违约退、4=作废")
    private Integer checkOutStatus;

    @Schema(description = "合同状态：0=未生效，1=生效中，2=已退租，3=已逾期，4=已作废")
    private Integer status;

    @Schema(description = "租客来源")
    private Long tenantSource;
    private String tenantSourceName;

    @Schema(description = "成交渠道")
    private Long dealChannel;
    private String dealChannelName;

    @Schema(description = "合同备注")
    private String remark;

    @Schema(description = "是否删除")
    private Boolean deleted;

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

    @Schema(description = "租客合同")
    private TenantContractVO tenantContract;

    @Schema(description = "租客账单列表")
    private List<TenantBillListVO> tenantBillList;

    @Schema(description = "租客同住人列表")
    private List<TenantMateVO> tenantMateList;

    @Schema(description = "其他费用列表")
    private List<OtherFeeDTO> otherFees;
}
