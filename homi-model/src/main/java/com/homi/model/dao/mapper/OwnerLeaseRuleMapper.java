package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerLeaseRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerLeaseRuleMapper extends BaseMapper<OwnerLeaseRule> {
    @Delete("delete from owner_lease_rule where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
