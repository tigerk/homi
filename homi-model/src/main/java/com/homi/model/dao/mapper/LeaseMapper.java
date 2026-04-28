package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Lease;
import com.homi.model.tenant.vo.LeaseLiteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaseMapper extends BaseMapper<Lease> {
    LeaseLiteVO getCurrentLeaseByRoomId(
        @Param("roomId") Long roomId,
        @Param("status") List<Integer> status,
        @Param("checkOutStatus") Integer checkOutStatus
    );

    /**
     * 查询房间列表，当前所有占用租约，未退租且有效的
     *
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/28 21:34

     * @param roomIds 参数说明
     * @param status 参数说明
     * @param checkOutStatus 参数说明
     * @return java.util.List<com.homi.model.tenant.vo.LeaseLiteVO>
     */
    List<LeaseLiteVO> listOccupyingLeasesByRoomIds(
        @Param("roomIds") List<Long> roomIds,
        @Param("status") List<Integer> status,
        @Param("checkOutStatus") Integer checkOutStatus
    );
}
