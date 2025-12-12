package com.homi.platform.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.Region;
import com.homi.model.dao.repo.RegionRepo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/platform/region")
@RestController
@RequiredArgsConstructor
public class RegionController {
    private final RegionRepo regionRepo;

    @Operation(summary = "返回4级区域列表")
    @GetMapping("/list")
    public ResponseResult<List<RegionVO>> list() {
        List<RegionVO> list = regionRepo.list().stream().map(this::format).collect(Collectors.toList());

        return ResponseResult.ok(list);
    }

    @Operation(summary = "返回3级区域列表", description = "三级：省、城市、区、")
    @GetMapping("/list/three")
    public ResponseResult<List<RegionVO>> threeList() {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Region::getDeep, 2);
        List<RegionVO> list = regionRepo.list(wrapper).stream().map(this::format).collect(Collectors.toList());

        return ResponseResult.ok(list);
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
