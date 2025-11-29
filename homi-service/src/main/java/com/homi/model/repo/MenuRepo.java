package com.homi.model.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.entity.Menu;
import com.homi.model.mapper.MenuMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class MenuRepo extends ServiceImpl<MenuMapper, Menu> {

}
