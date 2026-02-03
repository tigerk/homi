package com.homi.model.checkout.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 退租单 DTO
 */
@Data
public class TenantCheckoutDTO {

    /**
     * 退租单ID（修改时传）
     */
    private Long id;

    @Schema(description = "公司ID", hidden = true)
    private Long companyId;

    /**
     * 租客ID
     */
    @NotNull(message = "租客ID不能为空")
    private Long tenantId;

    /**
     * 退租类型
     */
    @NotNull(message = "退租类型不能为空")
    private Integer checkoutType;

    /**
     * 退租原因
     */
    private String checkoutReason;

    /**
     * 实际退租日
     */
    @NotNull(message = "实际退租日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date actualCheckoutDate;

    /**
     * 押金总额
     */
    private BigDecimal depositAmount;

    /**
     * 费用明细列表
     */
    private List<TenantCheckoutFeeDTO> feeList;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
