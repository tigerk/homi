package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.entity.TenantBill;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 租客账单表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-11-04
 */
@Mapper
public interface TenantBillMapper extends BaseMapper<TenantBill> {

}
