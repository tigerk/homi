package com.homi.model.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "业主合同退房初始化VO")
public class OwnerContractCheckoutInitVO {
    @Schema(description = "在租房间列表")
    private List<OwnerCheckoutLeaseRoomVO> leasedRoomList;
}
