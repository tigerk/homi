package com.homi.saas.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.Region;
import com.homi.model.dao.repo.RegionRepo;
import com.homi.model.common.vo.IdNameVO;
import com.homi.service.external.amap.AmapClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/region")
public class RegionController {
    private final RegionRepo regionRepo;

    private final AmapClient amapClient;

    @Operation(summary = "poi搜索")
    @GetMapping("/poi/tips")
    public ResponseResult<List<Object>> poiTips(@RequestParam("r") String cityName, @RequestParam("k") String keyword) {
        return ResponseResult.ok(amapClient.poiTips(cityName, keyword));
    }

    @Operation(summary = "返回4级区域列表")
    @GetMapping("/list")
    public ResponseResult<List<RegionVO>> list() {
        List<RegionVO> list = regionRepo.list().stream().map(this::format).collect(Collectors.toList());

        return ResponseResult.ok(list);
    }

    @Operation(summary = "返回城市区域列表", description = "获取城市列表")
    @GetMapping("/list/city")
    public ResponseResult<List<RegionVO>> cityList() {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getDeep, 1);
        List<RegionVO> list = regionRepo.list(wrapper).stream().map(this::format).collect(Collectors.toList());

        return ResponseResult.ok(list);
    }

    @Operation(summary = "返回3级区域列表", description = "三级：省、城市、区、")
    @GetMapping("/list/three")
    public ResponseResult<List<RegionVO>> threeList() {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getDeep, 2);
        List<RegionVO> list = regionRepo.list(wrapper).stream().map(this::format).collect(Collectors.toList());

        return ResponseResult.ok(list);
    }

    @Operation(summary = "根据经纬度获取城市区域ID", description = "根据经纬度获取城市区域ID")
    @GetMapping("/getCityByLocation")
    public ResponseResult<IdNameVO> getCityByLocation(@RequestParam("lat") String lat, @RequestParam("lon") String lon) {
        String adcode = amapClient.getCityByLocation(lat, lon);
        if (adcode == null) {
            return ResponseResult.fail(ResponseCodeEnum.SYSTEM_ERROR);
        }

        return ResponseResult.ok(regionRepo.getCityByAdcode(adcode));
    }

    private RegionVO format(Region region) {
        RegionVO regionVO = new RegionVO();
        regionVO.setId(region.getId());
        regionVO.setName(region.getName());
        regionVO.setParentId(region.getParentId());
        regionVO.setDeep(region.getDeep());

        return regionVO;
    }

    @Data
    public static class RegionVO {
        private Long id;
        private Long parentId;
        private Integer deep;
        private String name;
    }
}
