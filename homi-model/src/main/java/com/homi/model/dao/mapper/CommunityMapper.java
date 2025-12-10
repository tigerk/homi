package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.Community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 住宅小区表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-09-18
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {

    /**
     * 获取小区的房间数量
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/26 01:21
     *
     * @param communityId 参数说明
     * @param leaseMode
     * @param companyId
     * @return java.lang.Integer
     */
    Integer getCommunityRoomCount(@Param("communityId") Long communityId, @Param("leaseMode") Integer leaseMode, @Param("companyId") Long companyId);
}
