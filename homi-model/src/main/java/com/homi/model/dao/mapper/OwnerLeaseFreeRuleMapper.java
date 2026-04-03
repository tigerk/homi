package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerLeaseFreeRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerLeaseFreeRuleMapper extends BaseMapper<OwnerLeaseFreeRule> {
    @Delete("delete from owner_lease_free_rule where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
