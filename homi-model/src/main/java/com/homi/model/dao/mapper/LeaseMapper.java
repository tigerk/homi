package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Lease;
import com.homi.model.tenant.vo.LeaseLiteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaseMapper extends BaseMapper<Lease> {
    /**
     * 查询房间展示用租约。
     *
     * <p>该方法只返回一条租约，用于房间卡片、房源详情等页面展示。选择优先级为：
     * 在租中优先，其次当前日期覆盖的有效租约，再其次最近的未来有效租约。
     */
    LeaseLiteVO getDisplayLeaseByRoomId(
        @Param("roomId") Long roomId,
        @Param("status") List<Integer> status,
        @Param("checkOutStatus") Integer checkOutStatus
    );

    /**
     * 查询房间当前所有占用租约。
     *
     * <p>该方法返回所有未退租且状态有效的租约，用于业主退房等需要完整占用明细的业务场景。
     */
    List<LeaseLiteVO> listOccupyingLeasesByRoomIds(
        @Param("roomIds") List<Long> roomIds,
        @Param("status") List<Integer> status,
        @Param("checkOutStatus") Integer checkOutStatus
    );
}
