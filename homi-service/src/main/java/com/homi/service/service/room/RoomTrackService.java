package com.homi.service.service.room;

import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.RoomTrack;
import com.homi.model.dao.repo.RoomTrackRepo;
import com.homi.model.room.dto.RoomTrackDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/8/7
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomTrackService {
    private final RoomTrackRepo roomTrackRepo;

    public Long addRoomTrack(RoomTrackDTO dto) {
        RoomTrack roomTrack = BeanCopyUtils.copyBean(dto, RoomTrack.class);
        assert roomTrack != null;
        roomTrack.setCreateBy(dto.getUpdateBy());

        roomTrackRepo.save(roomTrack);
        return roomTrack.getId();
    }
}
