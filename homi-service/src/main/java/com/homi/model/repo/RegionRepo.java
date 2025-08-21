package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.Region;
import com.homi.model.mapper.RegionMapper;
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

}
