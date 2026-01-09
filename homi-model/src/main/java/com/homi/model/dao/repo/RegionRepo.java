package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Region;
import com.homi.model.dao.mapper.RegionMapper;
import com.homi.model.common.vo.IdNameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 区域表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-31
 */
@RequiredArgsConstructor
@Service
public class RegionRepo extends ServiceImpl<RegionMapper, Region> {

    public List<Long> findParentIdsById(Long id) {
        List<Long> parentIds = new ArrayList<>();
        Region region = getById(id);

        if (region != null) {
            Long parentId = region.getParentId();
            while (parentId != null && parentId != 0) {
                parentIds.add(parentId);
                Region parentRegion = getById(parentId);
                if (parentRegion != null) {
                    parentId = parentRegion.getParentId();
                } else {
                    break;
                }
            }
        }

        // 反转列表，使顺序从根节点到当前节点
        Collections.reverse(parentIds);
        return parentIds;
    }

    public List<Region> findParentRegionsById(Long id) {
        List<Region> parentRegions = new ArrayList<>();
        Region region = getById(id);

        if (region != null) {
            Long parentId = region.getParentId();
            while (parentId != null && parentId != 0) {
                Region parentRegion = getById(parentId);
                if (parentRegion != null) {
                    parentRegions.add(parentRegion);
                    parentId = parentRegion.getParentId();
                } else {
                    break;
                }
            }
        }

        // 反转列表，使顺序从根节点到当前节点
        Collections.reverse(parentRegions);
        return parentRegions;
    }

    public Region getRegionById(Long id) {
        return getById(id);
    }

    /**
     * 根据区域编码获取城市信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 13:20
     *
     * @param adcode 行政区划代码
     * @return com.homi.model.vo.IdNameVO
     */
    public IdNameVO getCityByAdcode(String adcode) {
        Region adcodeRegion = getById(Long.parseLong(adcode));
        if (adcodeRegion != null) {
            Region city = getById(adcodeRegion.getParentId());
            return new IdNameVO(city.getId(), city.getName());
        }

        return null;
    }
}
