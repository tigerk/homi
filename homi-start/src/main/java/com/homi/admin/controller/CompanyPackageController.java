package com.homi.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.company.CompanyPackageCreateDTO;
import com.homi.domain.dto.company.CompanyPackageQueryDTO;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.vo.company.CompanyPackageVO;
import com.homi.domain.vo.company.IdNameVO;
import com.homi.domain.vo.menu.SimpleMenuVO;
import com.homi.exception.BizException;
import com.homi.service.company.CompanyPackageService;
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
        UserLoginVO currentUser = LoginManager.getCurrentUser();
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

        UserLoginVO currentUser = LoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        return ResponseResult.ok(companyPackageService.changeStatus(createDTO));
    }

    @PostMapping("/menus/get")
    public ResponseResult<List<Long>> getMenusById(@RequestBody CompanyPackageCreateDTO createDTO) {
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
     * @return com.homi.domain.base.ResponseResult<java.util.List < com.homi.domain.vo.menu.SimpleMenuVO>>
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

