package com.homi.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.base.ResponseResult;
import com.homi.model.entity.Region;
import com.homi.model.repo.RegionRepo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@Slf4j
@RequestMapping("/admin/region")
@RestController
@RequiredArgsConstructor
public class RegionController {
    private final RegionRepo regionRepo;

    @Operation(summary = "返回4级区域列表")
    @GetMapping("/list")
    public ResponseResult<List<Region>> list() {
        return ResponseResult.ok(regionRepo.list());
    }

    @Operation(summary = "返回3级区域列表", description = "三级：省、城市、区、")
    @GetMapping("/list/three")
    public ResponseResult<List<Region>> threeList() {
        LambdaQueryWrapper<Region> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Region::getDeep, 2);
        return ResponseResult.ok(regionRepo.list(wrapper));
    }
}
