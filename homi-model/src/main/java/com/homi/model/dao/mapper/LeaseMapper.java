package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.model.dao.entity.Lease;
import com.homi.model.tenant.dto.LeaseQueryDTO;
import com.homi.model.tenant.vo.LeaseLiteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaseMapper extends BaseMapper<Lease> {
    LeaseLiteVO getCurrentLeaseByRoomId(@Param("roomId") Long roomId, @Param("status") List<Integer> status);
}
