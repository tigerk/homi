package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerSettlementRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerSettlementRuleMapper extends BaseMapper<OwnerSettlementRule> {
    @Delete("delete from owner_settlement_rule where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
