package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerContractHouse;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerContractHouseMapper extends BaseMapper<OwnerContractHouse> {
    @Delete("delete from owner_contract_house where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
