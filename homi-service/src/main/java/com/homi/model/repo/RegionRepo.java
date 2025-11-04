package com.homi.model.repo;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.config.AmapConfig;
import com.homi.model.entity.Region;
import com.homi.model.mapper.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final AmapConfig amapConfig;

    /**
     * 高德地图的poi搜索
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/17 23:14
     *
     * @param cityName 参数说明
     * @param keyword  参数说明
     * @return
     */
    public List<Object> poiTips(String cityName, String keyword) {
        String key = amapConfig.getKeys().get((int) (Math.random() * amapConfig.getKeys().size()));
        /*
         * 地点文本搜索接口支持按照设定的 POI 类型限定地点搜索结果；地点类型与 poi typecode 是同类内容，可以传入多个 poi typecode，相互之间用“|”分隔，内容可以参考 POI 分类码表；地点（POI）列表的排序会按照高德搜索能力进行综合权重排序；
         */
        String type = "120000|120100|120200|120201|120202|120203|120300|120301|120302|120303|120304";

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("key", key);
        paramMap.put("keywords", keyword);
        paramMap.put("type", type);
        paramMap.put("city", cityName);
        paramMap.put("citylimit", "true");

        String result = HttpUtil.get("https://restapi.amap.com/v3/assistant/inputtips", paramMap);

        JSONObject entries = JSONUtil.parseObj(result);

        return entries.getBeanList("tips", Object.class);
    }

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

    public Long getCityByLocation(String lat, String lon) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String key = amapConfig.getKeys().get((int) (Math.random() * amapConfig.getKeys().size()));
        paramMap.put("key", key);
        paramMap.put("location", lon + "," + lat);

        String result = HttpUtil.get("https://restapi.amap.com/v3/geocode/regeo", paramMap);

        JSONObject entries = JSONUtil.parseObj(result);
        if (entries.getByPath("status", Integer.class) == 1) {
            String adcode = entries.getByPath("regeocode.addressComponent.adcode", String.class);
            Region adcodeRegion = getById(Long.parseLong(adcode));
            if (adcodeRegion != null) {
                Region city = getById(adcodeRegion.getParentId());
                return city.getId();
            }
        }

        return null;
    }
}
