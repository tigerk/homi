package com.homi.model.repo;

import com.homi.model.entity.Room;
import com.homi.model.mapper.RoomMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class RoomRepo extends ServiceImpl<RoomMapper, Room> {

}
