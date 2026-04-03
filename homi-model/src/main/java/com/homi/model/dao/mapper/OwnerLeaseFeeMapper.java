package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerLeaseFee;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerLeaseFeeMapper extends BaseMapper<OwnerLeaseFee> {
    @Delete("delete from owner_lease_fee where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
