package com.homi.model.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.house.HouseLayoutDTO;
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
     * {@code @date} 2025/9/17 21:46
     *
     * @param modeRefId 参数说明
     * @param leaseMode 参数说明
     * @return java.util.List<com.homi.domain.dto.house.HouseLayoutDTO>
     */
    public List<HouseLayoutDTO> getLayoutListByModeRefId(Long modeRefId, Integer leaseMode) {
        LambdaQueryWrapper<HouseLayout> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(HouseLayout::getModeRefId, modeRefId);
        queryWrapper.eq(HouseLayout::getLeaseMode, leaseMode);
        return getBaseMapper().selectList(queryWrapper).stream().map(layout -> {

            HouseLayoutDTO houseLayoutDTO = BeanCopyUtils.copyBean(layout, HouseLayoutDTO.class);
            if (houseLayoutDTO != null) {
                houseLayoutDTO.setNewly(Boolean.FALSE);
            }

            return houseLayoutDTO;
        }).collect(Collectors.toList());
    }

    public HouseLayoutDTO getHouseLayoutById(Long houseLayoutId) {
        HouseLayout houseLayout = getById(houseLayoutId);
        HouseLayoutDTO houseLayoutDTO = BeanCopyUtils.copyBean(houseLayout, HouseLayoutDTO.class);
        assert houseLayoutDTO != null;

        return houseLayoutDTO;
    }
}
