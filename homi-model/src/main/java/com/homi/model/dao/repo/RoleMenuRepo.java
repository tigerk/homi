package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.RoleMenu;
import com.homi.model.dao.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class RoleMenuRepo extends ServiceImpl<RoleMenuMapper, RoleMenu> {

}
