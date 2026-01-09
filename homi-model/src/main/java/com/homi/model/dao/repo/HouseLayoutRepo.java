package com.homi.model.dao.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.HouseLayout;
import com.homi.model.dao.mapper.HouseLayoutMapper;
import com.homi.model.house.dto.FacilityItemDTO;
import com.homi.model.house.dto.HouseLayoutDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
     * @param leaseModeId 参数说明
     * @param leaseMode   参数说明
     * @return java.util.List<com.homi.domain.dto.house.HouseLayoutDTO>
     */
    public List<HouseLayoutDTO> getLayoutListByLeaseModeId(Long leaseModeId, Integer leaseMode) {
        LambdaQueryWrapper<HouseLayout> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(HouseLayout::getLeaseModeId, leaseModeId);
        queryWrapper.eq(HouseLayout::getLeaseMode, leaseMode);
        return getBaseMapper().selectList(queryWrapper).stream().map(layout -> {

            HouseLayoutDTO houseLayoutDTO = BeanCopyUtils.copyBean(layout, HouseLayoutDTO.class);
            if (Objects.isNull(houseLayoutDTO)) {
                return houseLayoutDTO;
            }

            houseLayoutDTO.setNewly(Boolean.FALSE);
            if (Objects.nonNull(layout.getFacilities()) && JSONUtil.isTypeJSON(layout.getFacilities())) {
                houseLayoutDTO.setFacilities(JSONUtil.toList(layout.getFacilities(), FacilityItemDTO.class));
            }

            if (Objects.nonNull(layout.getTags()) && JSONUtil.isTypeJSON(layout.getTags())) {
                houseLayoutDTO.setTags(JSONUtil.toList(layout.getTags(), String.class));
            }

            return houseLayoutDTO;
        }).collect(Collectors.toList());
    }

    public HouseLayoutDTO getHouseLayoutById(Long houseLayoutId) {
        HouseLayout houseLayout = getById(houseLayoutId);
        HouseLayoutDTO houseLayoutDTO = BeanCopyUtils.copyBean(houseLayout, HouseLayoutDTO.class);
        assert houseLayoutDTO != null;

        if (Objects.nonNull(houseLayout.getTags()) && JSONUtil.isTypeJSON(houseLayout.getTags())) {
            houseLayoutDTO.setTags(JSONUtil.toList(houseLayout.getTags(), String.class));
        }

        if (Objects.nonNull(houseLayout.getImageList()) && JSONUtil.isTypeJSON(houseLayout.getImageList())) {
            houseLayoutDTO.setImageList(JSONUtil.toList(houseLayout.getImageList(), String.class));
        }

        houseLayoutDTO.setNewly(Boolean.FALSE);

        return houseLayoutDTO;
    }
}
