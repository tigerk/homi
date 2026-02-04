package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.UserWechat;
import com.homi.model.dao.mapper.UserWechatMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户微信绑定 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-04
 */
@Service
public class UserWechatRepo extends ServiceImpl<UserWechatMapper, UserWechat> {

    public UserWechat getByOpenIdAndAppId(String openId, String appId) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<UserWechat>()
            .eq(UserWechat::getOpenId, openId)
            .eq(UserWechat::getAppId, appId));
    }

    public UserWechat getByUserIdAndAppId(Long userId, String appId) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<UserWechat>()
            .eq(UserWechat::getUserId, userId)
            .eq(UserWechat::getAppId, appId));
    }
}
