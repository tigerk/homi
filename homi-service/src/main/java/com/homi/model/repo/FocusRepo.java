package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.Focus;
import com.homi.model.mapper.FocusMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class FocusRepo extends ServiceImpl<FocusMapper, Focus> {

    public Focus getFocusByHouseId(Long houseId) {
        LambdaQueryWrapper<Focus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Focus::getHouseId, houseId);
        return getOne(queryWrapper);
    }
}
