package com.homi.platform.web.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dto.company.CompanyPackageCreateDTO;
import com.homi.model.dto.company.CompanyPackageIdDTO;
import com.homi.model.dto.company.CompanyPackageQueryDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.vo.company.CompanyPackageVO;
import com.homi.model.vo.menu.SimpleMenuVO;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import com.homi.service.service.company.CompanyPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RequestMapping("/admin/company/package")
@RestController
@RequiredArgsConstructor
public class CompanyPackageController {
    private final CompanyPackageService companyPackageService;

    @PostMapping("/list")
    public ResponseResult<PageVO<CompanyPackageVO>> list(@RequestBody CompanyPackageQueryDTO queryDTO) {
        return ResponseResult.ok(companyPackageService.getPackageList(queryDTO));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> list(@RequestBody CompanyPackageCreateDTO createDTO) {
        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
        createDTO.setCreateBy(currentUser.getId());
        createDTO.setCreateTime(DateUtil.date());
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());
        createDTO.setStatus(StatusEnum.ACTIVE.getValue());

        if (Objects.isNull(createDTO.getId())) {
            return ResponseResult.ok(companyPackageService.createCompanyPackage(createDTO));
        } else {
            return ResponseResult.ok(companyPackageService.updateCompanyPackage(createDTO));
        }
    }

    @PostMapping("/status/change")
    public ResponseResult<Boolean> changeStatus(@RequestBody CompanyPackageCreateDTO createDTO) {
        if (Objects.isNull(createDTO.getId())) {
            throw new BizException("id 不能为空");
        }

        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        return ResponseResult.ok(companyPackageService.changeStatus(createDTO));
    }

    @PostMapping("/menus/get")
    public ResponseResult<List<Long>> getMenusById(@RequestBody CompanyPackageIdDTO createDTO) {
        return ResponseResult.ok(companyPackageService.getMenusById(createDTO.getId()));
    }

    /**
     * 获取公司套餐可配置的菜单列表
     * <p>
     * 菜单列表，树由前端构建（菜单管理）
     * </p>
     * {@code @author} tk
     * {@code @date} 2025/6/16 23:08
     *
     * @return com.nest.domain.base.ResponseResult<java.util.List < com.nest.domain.vo.menu.SimpleMenuVO>>
     */
    @PostMapping("/menus/list")
    public ResponseResult<List<SimpleMenuVO>> getMenus() {
        return ResponseResult.ok(companyPackageService.getMenuList());
    }

    @PostMapping("/menus/save")
    public ResponseResult<Boolean> saveMenus(@RequestBody CompanyPackageCreateDTO createDTO) {
        return ResponseResult.ok(companyPackageService.saveMenus(createDTO));
    }

    @PostMapping("/list/simple")
    public ResponseResult<List<IdNameVO>> listSimple() {
        return ResponseResult.ok(companyPackageService.listSimple());
    }
}

