package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.house.FocusBuildingDTO;
import com.homi.model.entity.FocusBuilding;
import com.homi.model.mapper.FocusBuildingMapper;
import com.homi.utils.BeanCopyUtils;
import com.homi.utils.SpringUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
}
