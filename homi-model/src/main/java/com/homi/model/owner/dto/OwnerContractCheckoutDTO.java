package com.homi.model.owner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

@Data
@Schema(description = "业主合同退房DTO")
public class OwnerContractCheckoutDTO {
    @Schema(description = "业主合同ID")
    private Long contractId;

    @Schema(description = "退房日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkoutDate;

    @Schema(description = "退房原因")
    private String checkoutReason;

    @Schema(description = "结算说明")
    private String settlementRemark;

    @Schema(description = "是否释放房源")
    private Boolean releaseSubject;

    @Schema(description = "是否作废退房日之后未付款账单")
    private Boolean voidUnpaidFutureBills;
}
