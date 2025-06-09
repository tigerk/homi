package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.SysRoleMenu;
import com.homi.model.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Service
public class SysRoleMenuRepo extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> {

}
