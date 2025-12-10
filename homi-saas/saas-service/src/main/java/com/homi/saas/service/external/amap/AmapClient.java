package com.homi.saas.service.external.amap;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/12/10
 */

@Service
@RequiredArgsConstructor
public class AmapClient {
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

    /**
     * 根据经纬度获取城市信息
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/10 13:17
     *
     * @param lat 参数说明
     * @param lon 参数说明
     * @return com.homi.model.vo.IdNameVO
     */
    public String getCityByLocation(String lat, String lon) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String key = amapConfig.getKeys().get((int) (Math.random() * amapConfig.getKeys().size()));
        paramMap.put("key", key);
        paramMap.put("location", lon + "," + lat);

        String result = HttpUtil.get("https://restapi.amap.com/v3/geocode/regeo", paramMap);

        JSONObject entries = JSONUtil.parseObj(result);
        if (entries.getByPath("status", Integer.class) == 1) {
            return entries.getByPath("regeocode.addressComponent.adcode", String.class);
        }

        return null;
    }
}
