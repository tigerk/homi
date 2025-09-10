package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.house.FocusBuildingDTO;
import com.homi.model.entity.FocusBuilding;
import com.homi.model.mapper.FocusBuildingMapper;
import com.homi.utils.BeanCopyUtils;
import com.homi.utils.SpringUtils;
import org.springframework.beans.BeanUtils;
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
public class FocusBuildingRepo extends ServiceImpl<FocusBuildingMapper, FocusBuilding> {

    /**
     * 更新集中式的楼栋信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:25

     * @param focusId 参数说明
     * @param buildingList 参数说明
     * @return boolean
     */
    public boolean saveFocusBuildings(Long focusId, List<FocusBuildingDTO> buildingList) {
        List<FocusBuilding> focusBuildings = buildingList.stream().map(building -> {
                FocusBuilding focusBuilding = getFocusBuilding(focusId, building.getBuilding(), building.getUnit());
                if (Objects.isNull(focusBuilding)) {
                    focusBuilding = BeanCopyUtils.copyBean(building, FocusBuilding.class);
                    Objects.requireNonNull(focusBuilding).setFocusId(focusId);
                } else {
                    BeanUtils.copyProperties(building, focusBuilding);
                }

                return focusBuilding;
            }
        ).toList();

        // 通过Spring工具类获取当前代理对象，确保事务生效
        FocusBuildingRepo proxy = SpringUtils.getAopProxy(this);
        return proxy.saveOrUpdateBatch(focusBuildings);

    }

    public FocusBuilding getFocusBuilding(Long focusId, String building, String unit) {
        LambdaQueryWrapper<FocusBuilding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FocusBuilding::getFocusId, focusId);
        queryWrapper.eq(FocusBuilding::getBuilding, building);
        queryWrapper.eq(FocusBuilding::getUnit, unit);

        return getOne(queryWrapper);
    }
}
