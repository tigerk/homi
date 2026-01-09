package com.homi.platform.web.controller;

import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.Menu;
import com.homi.model.menu.dto.MenuCreateDTO;
import com.homi.model.menu.dto.MenuIdDTO;
import com.homi.model.menu.dto.MenuQueryDTO;
import com.homi.model.menu.vo.MenuVO;
import com.homi.model.menu.vo.SimpleMenuVO;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
import com.homi.service.service.sys.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 应用于 nest-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/platform/company/menu")
public class CompanyMenuController {
    private final MenuService menuService;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/list")
    public ResponseResult<List<MenuVO>> listMenu(MenuQueryDTO queryDTO) {
        return ResponseResult.ok(menuService.getMenuList(queryDTO));
    }

    /**
     * 返回简单菜单列表，树由前端构建（用户菜单）
     *
     * @return 所有数据
     */
    @GetMapping("/list/simple")
    public ResponseResult<List<SimpleMenuVO>> listSimple() {
        List<SimpleMenuVO> simpleMenuVOS = menuService.listSimpleMenu();
        return ResponseResult.ok(simpleMenuVOS);
    }

    /**
     * 通过主键查询单条数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/28 10:04
     *
     * @param getIdDTO 参数说明
     * @return com.nest.domain.base.ResponseResult<com.nest.model.entity.Menu>
     */
    @PostMapping("/get")
    public ResponseResult<Menu> selectOne(@RequestBody MenuIdDTO getIdDTO) {
        return ResponseResult.ok(menuService.getMenuById(getIdDTO.getId()));
    }

    /**
     * 新增菜单
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/21 16:04
     *
     * @param createDTO 参数说明
     * @return com.nest.domain.base.ResponseResult<java.lang.Boolean>
     */
    @PostMapping("/create")
    public ResponseResult<Void> createMenu(@RequestBody MenuCreateDTO createDTO) {
        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();

        if (Objects.isNull(createDTO.getId())) {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
        } else {
            if (Objects.nonNull(createDTO.getParentId()) && createDTO.getParentId().equals(createDTO.getId())) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "父菜单不能为本身");
            }
        }

        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        menuService.createMenu(createDTO);
        return ResponseResult.ok();
    }

    /**
     * 删除菜单
     *
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody MenuIdDTO deleteDTO) {
        return ResponseResult.ok(menuService.deleteById(deleteDTO.getId()));
    }
}
