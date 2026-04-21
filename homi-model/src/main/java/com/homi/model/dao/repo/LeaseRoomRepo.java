package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.mapper.LeaseRoomMapper;
import com.homi.model.tenant.dto.LeaseRoomDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 租约-房间关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-13
 */
@Service
public class LeaseRoomRepo extends ServiceImpl<LeaseRoomMapper, LeaseRoom> {

    public void saveLeaseRoomBatch(Long leaseId, List<Long> roomIds, List<LeaseRoomDTO> roomRentList) {
        remove(new LambdaQueryWrapper<LeaseRoom>().eq(LeaseRoom::getLeaseId, leaseId));

        Map<Long, BigDecimal> roomRentMap = new HashMap<>();
        if (roomRentList != null) {
            for (LeaseRoomDTO item : roomRentList) {
                if (item == null || item.getRoomId() == null) continue;
                roomRentMap.put(item.getRoomId(), item.getRentPrice());
            }
        }

        for (Long roomId : roomIds) {
            LeaseRoom leaseRoom = new LeaseRoom();
            leaseRoom.setLeaseId(leaseId);
            leaseRoom.setRoomId(roomId);
            leaseRoom.setRentPrice(roomRentMap.get(roomId));

            save(leaseRoom);
        }
    }

    /**
     * 根据租约ID获取房间列表
     *
     * @param leaseId 租约ID
     * @return 房间列表
     */
    public List<LeaseRoom> getListByLeaseId(Long leaseId) {
        return list(new LambdaQueryWrapper<LeaseRoom>().eq(LeaseRoom::getLeaseId, leaseId));
    }

    public List<LeaseRoom> getListByLeaseIds(List<Long> leaseIds) {
        if (leaseIds == null || leaseIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseRoom>().in(LeaseRoom::getLeaseId, leaseIds));
    }

    public List<LeaseRoom> getListByRoomIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<LeaseRoom>().in(LeaseRoom::getRoomId, roomIds));
    }
}
