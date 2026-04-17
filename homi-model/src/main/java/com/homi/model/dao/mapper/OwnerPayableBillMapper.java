package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.OwnerPayableBill;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OwnerPayableBillMapper extends BaseMapper<OwnerPayableBill> {
    @Delete({
        "<script>",
        "delete from owner_payable_bill where id in ",
        "<foreach collection='billIds' item='billId' open='(' separator=',' close=')'>",
        "#{billId}",
        "</foreach>",
        "</script>"
    })
    int physicalDeleteByIds(@Param("billIds") List<Long> billIds);
}
