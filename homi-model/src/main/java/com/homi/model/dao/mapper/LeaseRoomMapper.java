package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.LeaseRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 租约-房间关联表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-02-13
 */
@Mapper
public interface LeaseRoomMapper extends BaseMapper<LeaseRoom> {

}
