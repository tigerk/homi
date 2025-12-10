package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.FocusBuilding;
import com.homi.model.dao.mapper.FocusBuildingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 集中楼栋表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class FocusBuildingRepo extends ServiceImpl<FocusBuildingMapper, FocusBuilding> {
    public FocusBuilding getFocusBuilding(Long focusId, String building, String unit) {
        LambdaQueryWrapper<FocusBuilding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FocusBuilding::getFocusId, focusId);
        queryWrapper.eq(FocusBuilding::getBuilding, building);
        queryWrapper.eq(FocusBuilding::getUnit, unit);

        return getOne(queryWrapper);
    }

    public List<FocusBuilding> getBuildingsByFocusId(Long focusId) {
        LambdaQueryWrapper<FocusBuilding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FocusBuilding::getFocusId, focusId);

        return list(queryWrapper);
    }
}
