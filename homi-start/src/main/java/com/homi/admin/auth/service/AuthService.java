package com.homi.admin.auth.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.admin.auth.dto.login.UserLoginDTO;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.domain.enums.common.BizStatusEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.RoleDefaultEnum;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.User;
import com.homi.model.entity.SysUserRole;
import com.homi.model.mapper.SysRoleMapper;
import com.homi.model.mapper.UserMapper;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.service.system.SysMenuService;
import com.homi.service.system.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
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

    private final UserMapper userMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysMenuService sysMenuService;

    private final SysPermissionService sysPermissionService;

    // refresh token userId 名称
    public static final String JWT_USER_ID = "userId";
    public static final String JWT_EXP_TIME = "exp";

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 单位 秒
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateJwtToken(Long userId) {
        long time = DateUtil.date().offset(DateField.SECOND, (int) jwtExpiration).getTime();
        HashMap<String, Object> stringLongHashMap = new HashMap<>();
        stringLongHashMap.put(JWT_USER_ID, userId);
        stringLongHashMap.put(JWT_EXP_TIME, time);
        return JWTUtil.createToken(stringLongHashMap, jwtSecret.getBytes());
    }

    public Long getUserIdByToken(String token) {
        // 解析Token
        JWT jwt = JWTUtil.parseToken(token);

        // 校验签名
        boolean isValid = jwt.setKey(jwtSecret.getBytes()).verify();
        if (!isValid) {
            throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
        }

        // 获取过期时间字段
        Object exp = jwt.getPayload(JWT_EXP_TIME);
        if (exp != null) {
            long expMillis = Long.parseLong(exp.toString());
            boolean isExpired = System.currentTimeMillis() > expMillis;
            if (isExpired) {
                throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
            }
        } else {
            throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
        }

        return Long.valueOf(jwt.getPayload(JWT_USER_ID).toString());
    }

    public UserLoginVO loginSession(Long userId) {
        // 验证成功后的登录处理
        StpUtil.login(userId, "web");

        String refreshToken = generateJwtToken(userId);

        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.USER, user);

        // 获取当前回话的token
        String token = StpUtil.getTokenValue();
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setAccessToken(token);
        userLoginVO.setRefreshToken(refreshToken);
        userLoginVO.setExpires(DateUtil.date().offset(DateField.SECOND, (int) StpUtil.getTokenTimeout()).getTime());
        return userLoginVO;
    }

    /**
     * 用户登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/19 23:43
     *
     * @param userLoginDTO 参数说明
     * @return com.homi.admin.auth.vo.login.UserLoginVO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 校验用户是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userLoginDTO.getUsername()).or().eq(User::getEmail, userLoginDTO.getUsername()));
        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        if (user.getStatus().equals(BizStatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }
        // 密码校验
        String password = SaSecureUtil.md5(userLoginDTO.getPassword());
        if (!user.getPassword().equals(password)) {
            throw new BizException(ResponseCodeEnum.LOGIN_ERROR);
        }

        // 查询用户角色
        Pair<List<Long>, ArrayList<String>> roleList = getRoleList(user.getId());
        List<Long> roleIdList = roleList.getKey();
        ArrayList<String> roleCodeList = roleList.getValue();

        // 构建菜单树
        List<AsyncRoutesVO> asyncRoutesVOList = sysMenuService.buildMenuTreeByRoles(roleIdList);
        if (asyncRoutesVOList.isEmpty()) {
            throw new BizException(ResponseCodeEnum.USER_NO_ACCESS);
        }

        List<String> menuPermissionByRoles = sysPermissionService.getMenuPermissionByRoles(roleIdList);

        UserLoginVO userLoginVO = loginSession(user.getId());
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setRoles(roleCodeList);
        userLoginVO.setPermissions(menuPermissionByRoles);
        userLoginVO.setAsyncRoutes(asyncRoutesVOList);

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.ROLE_LIST, roleCodeList);
        currentSession.set(SaSession.PERMISSION_LIST, menuPermissionByRoles);

        return userLoginVO;
    }

    /**
     * 获取用户的角色id列表和角色code列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/6/9 13:40
     *
     * @param userId 参数说明
     * @return cn.hutool.core.lang.Pair<java.util.List < java.lang.Long>,java.util.ArrayList<java.lang.String>>
     */
    public Pair<List<Long>, ArrayList<String>> getRoleList(Long userId) {
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
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

        return Pair.of(roleIdList, roleCodeList);
    }

    public List<AsyncRoutesVO> getUserRoutes(Long userId) {
        Pair<List<Long>, ArrayList<String>> roleList = getRoleList(userId);
        List<Long> roleIdList = roleList.getKey();
        ArrayList<String> roleCodeList = roleList.getValue();
        // 构建菜单树
        return sysMenuService.buildMenuTreeByRoles(roleIdList);
    }
}
