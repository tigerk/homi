package com.homi.admin.auth.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.admin.auth.dto.UserLoginDTO;
import com.homi.admin.auth.vo.UserLoginVO;
import com.homi.domain.enums.common.BizStatusEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.SysUser;
import com.homi.model.entity.SysUserRole;
import com.homi.model.mapper.SysRoleMapper;
import com.homi.model.mapper.SysUserMapper;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.service.system.SysMenuService;
import com.homi.service.system.SysPermissionService;
import com.homi.utils.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账号相关实现
 * <p>
 * {@code @author} tk
 * {@code @date} 2025/4/19 23:43
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysMenuService sysMenuService;

    private final SysPermissionService sysPermissionService;

    /**
     * 用户登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/19 23:43
     *
     * @param userLoginDTO 参数说明
     * @return com.homi.admin.auth.vo.UserLoginVO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, userLoginDTO.getUsername()).or().eq(SysUser::getEmail, userLoginDTO.getUsername()));
        if (Objects.isNull(sysUser)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        if (sysUser.getStatus().equals(BizStatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }
        // 密码校验
        String password = SaSecureUtil.md5(userLoginDTO.getPassword());
        if (!sysUser.getPassword().equals(password)) {
            throw new BizException(ResponseCodeEnum.LOGIN_ERROR);
        }

        // 查询用户角色
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
        List<Long> roleIdList = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIdList));
        ArrayList<String> roleCodeList = new ArrayList<>();
        boolean existDisable = false;
        boolean hasSuperAdmin = false;
        for (SysRole role : sysRoles) {
            roleCodeList.add(role.getRoleCode());
            if (role.getStatus() == BizStatusEnum.DISABLED.getValue()) {
                existDisable = true;
            }
            if (role.getId().equals(RoleDefaultEnum.SUPERADMIN.getId())) {
                hasSuperAdmin = true;
            }
        }
        if (existDisable && !hasSuperAdmin) {
            throw new BizException(ResponseCodeEnum.ROLE_FREEZE);
        }

        // 构建菜单树
        List<AsyncRoutesVO> asyncRoutesVOList = sysMenuService.buildMenuTreeByRoles(roleIdList);
        if (asyncRoutesVOList.isEmpty()) {
            throw new BizException(ResponseCodeEnum.USER_NO_ACCESS);
        }

        List<String> menuPermissionByRoles = sysPermissionService.getMenuPermissionByRoles(roleIdList);

        // 验证成功后的登录处理
        StpUtil.login(sysUser.getId());
        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getTokenSession();
        currentSession.set(SaSession.USER, sysUser.getUsername());
        currentSession.set(SaSession.ROLE_LIST, roleCodeList);
        currentSession.set(SaSession.PERMISSION_LIST, menuPermissionByRoles);

        // 获取当前回话的token
        String token = StpUtil.getTokenInfo().getTokenValue();
        UserLoginVO userLoginVO = BeanCopyUtils.copyBean(sysUser, UserLoginVO.class);
        userLoginVO.setRoles(roleCodeList);
        userLoginVO.setToken(token);
        userLoginVO.setPermissions(menuPermissionByRoles);
        userLoginVO.setAsyncRoutesVOList(asyncRoutesVOList);
        return userLoginVO;
    }
}
