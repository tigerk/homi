package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.room.HouseLayoutDTO;
import com.homi.model.entity.HouseLayout;
import com.homi.model.mapper.HouseLayoutMapper;
import com.homi.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 房型设置 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-08-07
 */
@Service
public class HouseLayoutRepo extends ServiceImpl<HouseLayoutMapper, HouseLayout> {

    /**
     * 获取房型列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/21 12:23

      * @param houseId 参数说明
     * @return java.util.List<com.homi.domain.dto.room.HouseLayoutDTO>
     */
    public List<HouseLayoutDTO> getHouseLayoutListByHouseId(Long houseId) {
        LambdaQueryWrapper<HouseLayout> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(HouseLayout::getHouseId, houseId);
        return getBaseMapper().selectList(queryWrapper).stream().map(layout -> {

            HouseLayoutDTO houseLayoutDTO = BeanCopyUtils.copyBean(layout, HouseLayoutDTO.class);
            if (houseLayoutDTO != null) {
                houseLayoutDTO.setNewly(Boolean.FALSE);
            }

            return houseLayoutDTO;
        }).collect(Collectors.toList());
    }
}
