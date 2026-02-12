package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.LeaseRoom;
import com.homi.model.dao.mapper.LeaseRoomMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void saveLeaseRoomBatch(Long leaseId, List<Long> roomIds) {
        remove(new LambdaQueryWrapper<LeaseRoom>().eq(LeaseRoom::getLeaseId, leaseId));

        for (Long roomId : roomIds) {
            LeaseRoom leaseRoom = new LeaseRoom();
            leaseRoom.setLeaseId(leaseId);
            leaseRoom.setRoomId(roomId);

            save(leaseRoom);
            getBaseMapper().insert(leaseRoom);
        }
    }
}
