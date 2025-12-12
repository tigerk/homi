package com.homi.nest.web.service;

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
import com.homi.common.lib.enums.MenuTypeEnum;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.platform.PlatformUserTypeEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.model.dao.entity.PlatformMenu;
import com.homi.model.dao.entity.PlatformRole;
import com.homi.model.dao.entity.PlatformUser;
import com.homi.model.dao.entity.PlatformUserRole;
import com.homi.model.dao.mapper.PlatformUserMapper;
import com.homi.model.dao.repo.PlatformRoleRepo;
import com.homi.model.dao.repo.PlatformUserRoleRepo;
import com.homi.model.vo.menu.AsyncRoutesVO;
import com.homi.nest.service.service.perms.NestMenuService;
import com.homi.nest.service.service.perms.NestUserService;
import com.homi.nest.web.config.NestLoginManager;
import com.homi.nest.web.dto.login.UserLoginDTO;
import com.homi.nest.web.vo.login.NestUserLoginVO;
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
public class NestAuthService {

    // refresh token userId 名称
    public static final String JWT_USER_ID = "userId";
    public static final String JWT_EXP_TIME = "exp";

    private final PlatformUserMapper platformUserMapper;
    private final NestMenuService nestMenuService;
    private final NestUserService nestUserService;
    private final PlatformUserRoleRepo platformUserRoleRepo;

    private final PlatformRoleRepo platformRoleRepo;

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

    public NestUserLoginVO loginSession(Long userId) {
        // 验证成功后的登录处理
        StpUtil.login(userId, "web");

        String refreshToken = generateJwtToken(userId);

        PlatformUser platformUser = platformUserMapper.selectById(userId);
        if (Objects.isNull(platformUser)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.USER, platformUser);

        // 获取当前回话的token
        String token = StpUtil.getTokenValue();
        NestUserLoginVO nestUserLoginVO = new NestUserLoginVO();
        nestUserLoginVO.setAccessToken(token);
        nestUserLoginVO.setRefreshToken(refreshToken);
        nestUserLoginVO.setExpires(DateUtil.date().offset(DateField.SECOND, (int) StpUtil.getTokenTimeout()).getTime());
        return nestUserLoginVO;
    }

    /**
     * 用户登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/19 23:43
     *
     * @param userLoginDTO 参数说明
     * @return com.nest.admin.auth.vo.login.NestUserLoginVO
     */
    @Transactional(rollbackFor = Exception.class)
    public NestUserLoginVO login(UserLoginDTO userLoginDTO) {
        // 校验手机号 or 用户名
        PlatformUser platformUser = platformUserMapper.selectOne(new LambdaQueryWrapper<PlatformUser>()
            .eq(PlatformUser::getUsername, userLoginDTO.getUsername())
            .or()
            .eq(PlatformUser::getPhone, userLoginDTO.getUsername()));
        if (Objects.isNull(platformUser)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        if (platformUser.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }
        // 密码校验
        String password = SaSecureUtil.md5(userLoginDTO.getPassword());
        if (!platformUser.getPassword().equals(password)) {
            throw new BizException(ResponseCodeEnum.LOGIN_ERROR);
        }

        // 获取用户权限和菜单树
        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(platformUser);

        NestUserLoginVO nestUserLoginVO = loginSession(platformUser.getId());
        BeanUtils.copyProperties(platformUser, nestUserLoginVO);
        nestUserLoginVO.setRoles(userAuth.getLeft().getValue());
        nestUserLoginVO.setPermissions(userAuth.getRight());

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.ROLE_LIST, userAuth.getLeft().getValue());
        currentSession.set(SaSession.PERMISSION_LIST, userAuth.getRight());

        return nestUserLoginVO;
    }

    public Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> getUserAuth(PlatformUser platformUser) {
        if (platformUser.getUserType().equals(PlatformUserTypeEnum.REGULAR_USER.getType())) {
            // 查询用户角色
            Pair<List<Long>, List<String>> roleList = getRoleList(platformUser.getId());

            if (roleList.getKey().isEmpty()) {
                throw new BizException(ResponseCodeEnum.USER_NO_MENU_ACCESS);
            }

            // 构建菜单树
            List<AsyncRoutesVO> asyncRoutesVOList = nestMenuService.buildMenuTreeByRoles(roleList.getKey());
            if (asyncRoutesVOList.isEmpty()) {
                throw new BizException(ResponseCodeEnum.USER_NO_MENU_ACCESS);
            }

            // 获取权限点
            List<String> menuPermissionByRoles = platformRoleRepo.getMenuPermissionByRoles(roleList.getKey());

            return Triple.of(roleList, asyncRoutesVOList, menuPermissionByRoles);
        } else if (platformUser.getUserType().equals(PlatformUserTypeEnum.SUPER_USER.getType())) {
            // 平台管理员 获取 所有菜单
            List<PlatformMenu> menuList = nestMenuService.getMenuList();

            // 获取菜单树
            List<PlatformMenu> menuTreeList = menuList.stream()
                .filter(m -> MenuTypeEnum.getMenuList().contains(m.getMenuType()))
                .collect(Collectors.toList());
            // 获取按钮权限
            List<String> permList = menuList.stream()
                .filter(m -> MenuTypeEnum.getButtonList().contains(m.getMenuType()))
                .map(PlatformMenu::getAuths)
                .collect(Collectors.toList());

            List<AsyncRoutesVO> asyncRoutesVOS = nestMenuService.buildMenuTree(menuTreeList);

            return Triple.of(Pair.of(new ArrayList<>(), new ArrayList<>()), asyncRoutesVOS, permList);
        } else {
            throw new BizException(ResponseCodeEnum.AUTHORIZED);
        }
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
        List<PlatformUserRole> roleListByUserId = platformUserRoleRepo.getRoleListByUserId(userId);
        if (roleListByUserId.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        List<Long> roleIdList = roleListByUserId.stream().map(PlatformUserRole::getRoleId).collect(Collectors.toList());

        List<PlatformRole> roleList = platformRoleRepo.getRoleListByIdList(roleIdList);

        if (CollUtil.isEmpty(roleList)) {
            throw new BizException(ResponseCodeEnum.USER_NO_ACCESS);
        }

        // 去掉 status = disabled 的角色，重置 roleIdList 和 roleIdList
        roleList = roleList.stream().filter(r -> r.getStatus().equals(StatusEnum.ACTIVE.getValue())).collect(Collectors.toList());
        roleIdList = roleList.stream().map(PlatformRole::getId).collect(Collectors.toList());

        List<String> roleCodeList = roleList.stream().map(PlatformRole::getCode).collect(Collectors.toList());

        return Pair.of(roleIdList, roleCodeList);
    }

    public List<AsyncRoutesVO> getUserRoutes(Long userId) {
        PlatformUser platformUserById = nestUserService.getUserById(userId);

        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(platformUserById);
        // 构建菜单树
        return userAuth.getMiddle();
    }

    /**
     * 只有超管才能修改超管账号
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/24 21:46
     *
     * @param userId      参数说明
     * @param currentUser 参数说明
     */
    public void canUpdateUser(Long userId, NestUserLoginVO currentUser) {
        PlatformUser platformUserById = nestUserService.getUserById(userId);
        // 只有超管才能重置超管的账号
        if (platformUserById.getUserType().equals(PlatformUserTypeEnum.SUPER_USER.getType())
            && !NestLoginManager.getCurrentUser().getUserType().equals(PlatformUserTypeEnum.SUPER_USER.getType())) {
            throw new BizException("无权限操作");
        }
    }
}
