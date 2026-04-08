package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerContractSubject;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerContractSubjectMapper extends BaseMapper<OwnerContractSubject> {
    @Delete("delete from owner_contract_subject where contract_id = #{contractId}")
    int deleteByContractIdForce(@Param("contractId") Long contractId);
}
