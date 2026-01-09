package com.homi.platform.web.controller;

import cn.hutool.core.date.DateUtil;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.PlatformMenu;
import com.homi.model.menu.dto.MenuCreateDTO;
import com.homi.model.menu.dto.MenuIdDTO;
import com.homi.model.menu.dto.MenuQueryDTO;
import com.homi.model.menu.vo.MenuVO;
import com.homi.model.menu.vo.SimpleMenuVO;
import com.homi.platform.service.service.perms.PlatformMenuService;
import com.homi.platform.web.config.PlatformLoginManager;
import com.homi.platform.web.vo.login.PlatformUserLoginVO;
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
@RequestMapping("/platform/menu")
public class PlatformMenuController {
    private final PlatformMenuService platformMenuService;

    /**
     * 返回菜单列表，树由前端构建（菜单管理）
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @PostMapping("/list")
//    @SaCheckPermission("system:menu:query")
    public ResponseResult<List<MenuVO>> listMenu(MenuQueryDTO queryDTO) {
        return ResponseResult.ok(platformMenuService.getMenuList(queryDTO));
    }

    /**
     * 返回简单菜单列表，树由前端构建（用户菜单）
     *
     * @return 所有数据
     */
    @GetMapping("/list/simple")
    public ResponseResult<List<SimpleMenuVO>> listSimple() {
        List<SimpleMenuVO> simpleMenuVOS = platformMenuService.listSimpleMenu();
        return ResponseResult.ok(simpleMenuVOS);
    }

    /**
     * 通过主键查询单条数据
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/12 14:47

      * @param getIdDTO 参数说明
     * @return com.homi.common.lib.response.ResponseResult<com.homi.model.dao.entity.PlatformMenu>
     */
    @PostMapping("/get")
    public ResponseResult<PlatformMenu> selectOne(@RequestBody MenuIdDTO getIdDTO) {
        return ResponseResult.ok(platformMenuService.getMenuById(getIdDTO.getId()));
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
//    @SaCheckPermission("system:menu:create")
    public ResponseResult<Void> createMenu(@RequestBody MenuCreateDTO createDTO) {
        PlatformUserLoginVO currentUser = PlatformLoginManager.getCurrentUser();
        createDTO.setUpdateBy(currentUser.getId());
        createDTO.setUpdateTime(DateUtil.date());

        if (Objects.isNull(createDTO.getId())) {
            createDTO.setCreateBy(currentUser.getId());
            createDTO.setCreateTime(DateUtil.date());
        } else {
            if (Objects.nonNull(createDTO.getParentId()) && createDTO.getParentId().equals(createDTO.getId())) {
                throw new BizException(ResponseCodeEnum.VALID_ERROR.getCode(), "父菜单不能为本身");
            }
        }

        platformMenuService.createMenu(createDTO);
        return ResponseResult.ok();
    }

    /**
     * 删除菜单
     *
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> delete(@RequestBody MenuIdDTO deleteDTO) {
        return ResponseResult.ok(platformMenuService.deleteById(deleteDTO.getId()));
    }
}
