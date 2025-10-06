package com.homi.admin.auth.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.admin.auth.dto.login.LoginDTO;
import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.domain.RedisKey;
import com.homi.domain.dto.company.CompanyUserListDTO;
import com.homi.domain.dto.menu.AsyncRoutesVO;
import com.homi.domain.enums.common.CompanyUserTypeEnum;
import com.homi.domain.enums.common.MenuTypeEnum;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.enums.common.StatusEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.*;
import com.homi.model.mapper.SysRoleMapper;
import com.homi.model.mapper.SysUserRoleMapper;
import com.homi.model.mapper.SysUserMapper;
import com.homi.model.repo.CompanyRepo;
import com.homi.model.repo.CompanyUserRepo;
import com.homi.model.repo.SysUserRepo;
import com.homi.service.company.CompanyPackageService;
import com.homi.service.company.CompanyService;
import com.homi.service.system.SysMenuService;
import com.homi.service.system.SysRoleService;
import com.homi.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuService sysMenuService;
    private final SysRoleService sysRoleService;

    private final CompanyPackageService companyPackageService;

    private final CompanyRepo companyRepo;
    private final CompanyService companyService;

    private final UserService userService;

    private final CompanyUserRepo companyUserRepo;
    private final SysUserRepo sysUserRepo;

    // jwt 密钥
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 单位 秒
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成jwt token
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/28 17:00
     *
     * @param userId 参数说明
     * @return java.lang.String
     */
    private String generateJwtToken(Long userId) {
        long time = DateUtil.date().offset(DateField.SECOND, (int) jwtExpiration).getTime();
        HashMap<String, Object> stringLongHashMap = new HashMap<>();
        stringLongHashMap.put(JWT_USER_ID, userId);
        stringLongHashMap.put(JWT_EXP_TIME, time);
        return JWTUtil.createToken(stringLongHashMap, jwtSecret.getBytes());
    }

    /**
     * 从token中获取userId
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/7/28 17:00
     *
     * @param token 参数说明
     * @return java.lang.Long
     */
    private Long getUserIdByToken(String token) {
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

    /**
     * 登录成功后，创建会话
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 09:20
     *
     * @param user 参数说明
     * @return com.homi.admin.auth.vo.login.UserLoginVO
     */
    private UserLoginVO loginSession(UserLoginVO user) {
        // 验证成功后的登录处理
        StpUtil.login(user.getId(), "web");

        String refreshToken = generateJwtToken(user.getId());

        // 保存到 Redis，key: captcha:uuid，value: code，有效期10分钟
        stringRedisTemplate.opsForValue().set(RedisKey.LOGIN_REFRESH_TOKEN.format(user.getId()),
            JSONUtil.toJsonStr(Pair.of(refreshToken, user.getCurCompanyId())),
            RedisKey.LOGIN_REFRESH_TOKEN.getTimeout(),
            RedisKey.LOGIN_REFRESH_TOKEN.getUnit());

        // 获取当前回话的token
        String token = StpUtil.getTokenValue();
        user.setAccessToken(token);
        user.setRefreshToken(refreshToken);
        user.setExpires(DateUtil.date().offset(DateField.SECOND, (int) StpUtil.getTokenTimeout()).getTime());

        List<CompanyUserListDTO> companyListByUserId = companyUserRepo.getCompanyListByUserId(user.getId());
        user.setCompanyList(companyListByUserId);

        // 用户角色code与权限,用户名存入缓存
        SaSession currentSession = StpUtil.getSession();
        currentSession.set(SaSession.USER, user);
        currentSession.set(SaSession.ROLE_LIST, user.getRoles());
        currentSession.set(SaSession.PERMISSION_LIST, user.getPermissions());

        return user;
    }

    /**
     * 用户登录
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/4/19 23:43
     *
     * @param userLogin 参数说明
     * @return com.homi.admin.auth.vo.login.UserLoginVO
     */
    public UserLoginVO login(UserLoginVO userLogin) {
        // 获取用户权限和菜单树
        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(userLogin);
        userLogin.setRoles(userAuth.getLeft().getValue());
        userLogin.setPermissions(userAuth.getRight());

        // 创建登录会话
        loginSession(userLogin);

        return userLogin;
    }

    /**
     * 校验用户密码是否正确
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 11:04
     *
     * @param loginDTO 参数说明
     * @return com.homi.admin.auth.vo.login.UserLoginVO
     */
    public UserLoginVO checkUserLogin(LoginDTO loginDTO) {
        // 校验用户是否存在
        User user = sysUserMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername())
            .or().eq(User::getPhone, loginDTO.getUsername()));

        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        if (user.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }
        // 密码校验
        String password = SaSecureUtil.md5(loginDTO.getPassword());
        if (!user.getPassword().equals(password)) {
            throw new BizException(ResponseCodeEnum.LOGIN_ERROR);
        }

        // 获取绑定该用户的公司列表
        List<CompanyUserListDTO> companyUserList = companyUserRepo.getCompanyListByUserId(user.getId());
        if (companyUserList.isEmpty()) {
            throw new BizException(ResponseCodeEnum.USER_NOT_BIND_COMPANY);
        }

        UserLoginVO userLogin = new UserLoginVO();
        BeanUtils.copyProperties(user, userLogin);

        CompanyUserListDTO first = companyUserList.getFirst();
        userLogin.setCurCompanyId(first.getCompanyId());
        userLogin.setCompanyUserType(first.getCompanyUserType());

        return userLogin;
    }

    /**
     * 获取角色、权限和菜单树
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/12 09:41
     *
     * @param user 参数说明
     * @return org.apache.commons.lang3.tuple.Triple<cn.hutool.core.lang.Pair<java.util.List<java.lang.Long>,java.util.List<java.lang.String>>,java.util.List<com.homi.domain.dto.menu.AsyncRoutesVO>,java.util.List<java.lang.String>>
     */
    public Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> getUserAuth(UserLoginVO user) {
        Company companyById = companyRepo.getById(user.getCurCompanyId());

        // 管理员获取所有权限点
        if (user.getCompanyUserType().equals(CompanyUserTypeEnum.COMPANY_ADMIN.getType())) {
            List<Long> menusById = companyPackageService.getMenusById(companyById.getPackageId());
            List<SysMenu> menuList = sysMenuService.getMenuByIds(menusById);


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

    public List<AsyncRoutesVO> getUserRoutes(UserLoginVO user) {
        Triple<Pair<List<Long>, List<String>>, List<AsyncRoutesVO>, List<String>> userAuth = getUserAuth(user);
        // 构建菜单树
        return userAuth.getMiddle();
    }

    public Boolean kickUserByUsername(String username) {
        User user = userService.getUserByUsername(username);
        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        StpUtil.kickout(user.getId());
        stringRedisTemplate.delete(RedisKey.LOGIN_REFRESH_TOKEN.format(user.getId()));
        return true;
    }

    public UserLoginVO refreshLogin(String refreshToken) {
        Long userId = getUserIdByToken(refreshToken);

        String refreshTokenByUserId = stringRedisTemplate.opsForValue().get(RedisKey.LOGIN_REFRESH_TOKEN.format(userId));
        if (CharSequenceUtil.isBlank(refreshTokenByUserId)) {
            throw new BizException(ResponseCodeEnum.TOKEN_ERROR);
        }

        Pair<String, Long> refreshTokenPair = JSONUtil.toBean(refreshTokenByUserId, Pair.class);

        return loginWithCompanyId(userId, refreshTokenPair.getValue());
    }

    public UserLoginVO loginWithCompanyId(Long userId, Long companyId) {
        // 校验用户是否存在
        User user = sysUserRepo.getById(userId);

        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_EXIST);
        }
        if (user.getStatus().equals(StatusEnum.DISABLED.getValue())) {
            throw new BizException(ResponseCodeEnum.USER_FREEZE);
        }

        UserLoginVO userLogin = new UserLoginVO();
        BeanUtils.copyProperties(user, userLogin);

        CompanyUser companyUser = companyUserRepo.getCompanyUser(companyId, userId);

        userLogin.setCurCompanyId(companyUser.getCompanyId());
        userLogin.setCompanyUserType(companyUser.getCompanyUserType());

        return login(userLogin);
    }
}
