package com.homi.admin.auth.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.admin.auth.dto.login.UserLoginDTO;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.domain.enums.common.MenuTypeEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.domain.enums.common.UserTypeEnum;
import com.homi.domain.vo.company.CompanyListVO;
import com.homi.domain.vo.menu.AsyncRoutesVO;
import com.homi.exception.BizException;
import com.homi.model.entity.SysMenu;
import com.homi.model.entity.SysRole;
import com.homi.model.entity.SysUserRole;
import com.homi.model.entity.User;
import com.homi.model.mapper.SysRoleMapper;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.model.mapper.UserMapper;
import com.homi.service.company.CompanyPackageService;
import com.homi.service.company.CompanyService;
import com.homi.service.system.SysMenuService;
import com.homi.service.system.SysRoleService;
import com.homi.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
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

    // refresh token userId 名称
    public static final String JWT_USER_ID = "userId";
    public static final String JWT_EXP_TIME = "exp";
    private final UserMapper userMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuService sysMenuService;
    private final SysRoleService sysRoleService;

    private final CompanyPackageService companyPackageService;

    private final CompanyService companyService;
    private final UserService userService;

    // jwt 密钥
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
        if (user.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }
        // 密码校验
        String password = SaSecureUtil.md5(userLoginDTO.getPassword());
        if (!user.getPassword().equals(password)) {
            throw new BizException(ResponseCodeEnum.LOGIN_ERROR);
        }

        // 获取用户权限和菜单树
        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(user);


        UserLoginVO userLoginVO = loginSession(user.getId());
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setRoles(userAuth.getLeft().getValue());
        userLoginVO.setPermissions(userAuth.getRight());

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.ROLE_LIST, userAuth.getLeft().getValue());
        currentSession.set(SaSession.PERMISSION_LIST, userAuth.getRight());

        return userLoginVO;
    }

    public Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> getUserAuth(User user) {
        if (user.getUserType().equals(UserTypeEnum.PLATFORM_SUPER_ADMIN.getType()) || user.getUserType().equals(UserTypeEnum.COMPANY_ADMIN.getType())) {
            List<SysMenu> menuList;

            // 平台管理员
            if (user.getUserType().equals(UserTypeEnum.PLATFORM_SUPER_ADMIN.getType())) {
                // 获取平台管理员菜单列表
                menuList = sysMenuService.getPlatformMenuList();
            } else {
                // 获取公司管理员菜单列表
                CompanyListVO companyById = companyService.getCompanyById(user.getCompanyId());
                List<Long> menusById = companyPackageService.getMenusById(companyById.getPackageId());
                menuList = sysMenuService.getMenuByIds(menusById);
            }

            List<SysMenu> menuTreeList = menuList.stream()
                    .filter(m -> !m.getMenuType().equals(MenuTypeEnum.BUTTON.getType()))
                    .collect(Collectors.toList());

            List<String> permList = menuList.stream()
                    .filter(m -> m.getMenuType().equals(MenuTypeEnum.BUTTON.getType()))
                    .map(SysMenu::getAuths)
                    .collect(Collectors.toList());

            List<AsyncRoutesVO> asyncRoutesVOS = sysMenuService.buildMenuTree(menuTreeList);

            return Triple.of(Pair.of(new ArrayList<>(), new ArrayList<>()), asyncRoutesVOS, permList);
        }

        // 查询用户角色
        Pair<List<Long>, List<String>> roleList = getRoleList(user.getId());
        // 构建菜单树
        List<AsyncRoutesVO> asyncRoutesVOList = sysMenuService.buildMenuTreeByRoles(roleList.getKey());
        if (asyncRoutesVOList.isEmpty()) {
            throw new BizException(ResponseCodeEnum.USER_NO_ACCESS);
        }

        List<String> menuPermissionByRoles = sysRoleService.getMenuPermissionByRoles(roleList.getKey());

        return Triple.of(roleList, asyncRoutesVOList, menuPermissionByRoles);
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
    public Pair<List<Long>, List<String>> getRoleList(Long userId) {
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoleList.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        List<Long> roleIdList = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIdList)
                .eq(SysRole::getStatus, StatusEnum.ACTIVE.getValue())
        );

        if (CollUtil.isEmpty(sysRoles)) {
            throw new BizException(ResponseCodeEnum.USER_NO_ACCESS);
        }

        List<String> roleCodeList = sysRoles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        return Pair.of(roleIdList, roleCodeList);
    }

    public List<AsyncRoutesVO> getUserRoutes(Long userId) {
        User userById = userService.getUserById(userId);

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(userById);
        // 构建菜单树
        return userAuth.getMiddle();
    }
}
