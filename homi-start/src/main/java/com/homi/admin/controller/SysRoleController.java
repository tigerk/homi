package com.homi.admin.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.admin.role.RoleConvert;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.role.RoleQueryDTO;
import com.homi.domain.dto.role.SysRoleCreateDTO;
import com.homi.domain.dto.role.SysRoleUpdateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.vo.role.RoleSimpleVO;
import com.homi.domain.vo.role.SysRoleVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRole;
import com.homi.service.system.SysRoleService;
import com.homi.service.system.SysUserRoleService;
import com.homi.utils.BeanCopyUtils;
import com.homi.utils.JsonUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 角色信息表(SysRole)表控制层
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/28 18:55
 */

@RequestMapping("admin/sys/role")
@RequiredArgsConstructor
public class SysRoleController {
    /**
     * 服务对象
     */

    private final SysRoleService sysRoleService;

    private final SysUserRoleService sysUserRoleService;

    /**
     * 查询角色列表
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @SaCheckPermission("system:role:query")
    public ResponseResult<IPage<SysRoleVO>> selectPage(RoleQueryDTO queryDTO) {
        return ResponseResult.ok(this.sysRoleService.listRolePage(queryDTO));
    }

    /**
     * 角色详情
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/get/{id}")
    public ResponseResult<SysRole> selectOne(@PathVariable Long id) {
        return ResponseResult.ok(this.sysRoleService.getRoleById(id));
    }

    /**
     * 新增角色
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @SaCheckPermission("system:role:create")
    public ResponseResult<Long> insert(@Valid @RequestBody SysRoleCreateDTO createDTO) {
        SysRole sysRole = BeanCopyUtils.copyBean(createDTO, SysRole.class);
        return ResponseResult.ok(this.sysRoleService.createRole(sysRole));
    }

    /**
     * 修改角色
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @SaCheckPermission("system:role:update")
    public ResponseResult<Long> update(@Valid @RequestBody SysRoleUpdateDTO updateDTO) {
        SysRole sysRole = BeanCopyUtils.copyBean(updateDTO, SysRole.class);
        return ResponseResult.ok(this.sysRoleService.updateRole(sysRole));
    }

    /**
     * 删除角色
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:role:delete")
    public ResponseResult<Boolean> delete(@PathVariable("id") Long id) {
        if (Objects.nonNull(RoleDefaultEnum.fromValue(id))) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "系统内置角色无法删除");
        }
        long count = sysUserRoleService.getUserCountByRoleId(id);

        if (count > 0) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该角色已绑定用户，无法删除");
        }

        return ResponseResult.ok(sysRoleService.deleteRoleById(id));
    }

    /**
     * 查询所有简单角色数据
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list/all")
//    @SaCheckPermission("system:role:listSimpleAll")
    public ResponseResult<List<RoleSimpleVO>> listSimpleAll(RoleQueryDTO queryDTO) {
        List<SysRole> list = sysRoleService.getSimpleList(queryDTO);
        return ResponseResult.ok(RoleConvert.INSTANCE.convertSimpleList(list));
    }
}

