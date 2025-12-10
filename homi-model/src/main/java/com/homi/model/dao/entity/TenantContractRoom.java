package com.homi.model.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serial;

/**
 * <p>
 * 合同房间表
 * </p>
 *
 * @author tk
 * @since 2025-11-19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@TableName("tenant_contract_room")
@Schema(name = "TenantContractRoom", description = "合同房间表")
public class TenantContractRoom implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "合同ID")
    @TableField("contract_id")
    private Long contractId;

    @Schema(description = "房间ID")
    @TableField("room_id")
    private Long roomId;
}
