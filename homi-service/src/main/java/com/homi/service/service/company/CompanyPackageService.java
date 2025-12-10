package com.homi.service.service.company;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.CompanyPackage;
import com.homi.model.dao.repo.CompanyPackageRepo;
import com.homi.model.dto.company.CompanyPackageCreateDTO;
import com.homi.model.dto.company.CompanyPackageQueryDTO;
import com.homi.model.dto.menu.MenuQueryDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.vo.company.CompanyPackageVO;
import com.homi.model.vo.menu.MenuVO;
import com.homi.model.vo.menu.SimpleMenuVO;
import com.homi.service.service.system.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class CompanyPackageService {
    private final CompanyPackageRepo companyPackageRepo;

    private final MenuService menuService;

    public PageVO<CompanyPackageVO> getPackageList(CompanyPackageQueryDTO queryDTO) {
        Page<CompanyPackage> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<CompanyPackage> queryWrapper = new LambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(queryDTO.getName())) {
            queryWrapper.like(CompanyPackage::getName, queryDTO.getName());
        }

        if (Objects.nonNull(queryDTO.getStatus())) {
            queryWrapper.eq(CompanyPackage::getStatus, queryDTO.getStatus());
        }

        IPage<CompanyPackage> companyPackagePage = companyPackageRepo.page(page, queryWrapper);


        PageVO<CompanyPackageVO> pageVO = new PageVO<>();
        pageVO.setTotal(companyPackagePage.getTotal());
        // 格式化数据
        pageVO.setList(companyPackagePage.getRecords().stream().map(companyPackage -> {
            CompanyPackageVO companyPackageVO = BeanCopyUtils.copyBean(companyPackage, CompanyPackageVO.class);
            companyPackageVO.setPackageMenus(JSONUtil.toList(companyPackage.getPackageMenus(), Long.class));
            return companyPackageVO;
        }).toList());
        pageVO.setCurrentPage(companyPackagePage.getCurrent());
        pageVO.setPageSize(companyPackagePage.getSize());
        pageVO.setPages(companyPackagePage.getPages());

        return pageVO;
    }

    public Boolean createCompanyPackage(CompanyPackageCreateDTO createDTO) {
        boolean exists = companyPackageRepo.exists(new LambdaQueryWrapper<CompanyPackage>().eq(CompanyPackage::getName, createDTO.getName()));
        if (exists) {
            throw new BizException("套餐名称已存在，不允许重复创建");
        }

        CompanyPackage companyPackage = BeanCopyUtils.copyBean(createDTO, CompanyPackage.class);

        companyPackage.setPackageMenus(JSONUtil.toJsonStr(createDTO.getPackageMenus()));

        companyPackageRepo.save(companyPackage);

        return true;
    }

    public Boolean updateCompanyPackage(CompanyPackageCreateDTO createDTO) {
        boolean exists = companyPackageRepo.exists(new LambdaQueryWrapper<CompanyPackage>()
            .eq(CompanyPackage::getName, createDTO.getName())
            .ne(CompanyPackage::getId, createDTO.getId())
        );
        if (exists) {
            throw new BizException("套餐名称已存在，不允许重复创建");
        }

        CompanyPackage companyPackage = companyPackageRepo.getBaseMapper().selectById(createDTO.getId());
        companyPackage.setName(createDTO.getName());
        companyPackage.setRemark(createDTO.getRemark());
        companyPackage.setUpdateBy(createDTO.getUpdateBy());
        companyPackage.setUpdateTime(createDTO.getUpdateTime());

        companyPackageRepo.updateById(companyPackage);

        return true;
    }

    public Boolean changeStatus(CompanyPackageCreateDTO createDTO) {
        CompanyPackage companyPackage = companyPackageRepo.getBaseMapper().selectById(createDTO.getId());
        companyPackage.setStatus(createDTO.getStatus());

        companyPackageRepo.getBaseMapper().updateById(companyPackage);

        return true;
    }

    public List<Long> getMenusById(Long id) {
        CompanyPackage companyPackage = companyPackageRepo.getBaseMapper().selectById(id);

        return JSONUtil.toList(companyPackage.getPackageMenus(), Long.class);
    }

    /**
     * 获取公司套餐可配置的权限列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/16 22:54
     *
     * @return java.util.List<java.lang.Long>
     */
    public List<SimpleMenuVO> getMenuList() {
        MenuQueryDTO queryDTO = new MenuQueryDTO();
        List<MenuVO> menuList = menuService.getPlatformMenuList(queryDTO);

        return menuList.stream().map(menuVO -> BeanCopyUtils.copyBean(menuVO, SimpleMenuVO.class)).toList();
    }

    public Boolean saveMenus(CompanyPackageCreateDTO createDTO) {
        CompanyPackage companyPackage = companyPackageRepo.getBaseMapper().selectById(createDTO.getId());

        companyPackage.setPackageMenus(JSONUtil.toJsonStr(createDTO.getPackageMenus()));

        companyPackageRepo.getBaseMapper().updateById(companyPackage);

        return true;
    }

    public List<IdNameVO> listSimple() {
        LambdaQueryWrapper<CompanyPackage> queryWrapper = new LambdaQueryWrapper<CompanyPackage>()
            .eq(CompanyPackage::getStatus, StatusEnum.ACTIVE.getValue());

        return companyPackageRepo.list(queryWrapper).stream()
            .map(companyPackage -> new IdNameVO(companyPackage.getId(), companyPackage.getName()))
            .toList();
    }
}
