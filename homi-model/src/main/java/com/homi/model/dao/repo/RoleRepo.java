package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Role;
import com.homi.model.dao.mapper.RoleMapper;
import com.homi.model.dto.role.RoleQueryDTO;
import com.homi.model.vo.role.RoleVO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class RoleRepo extends ServiceImpl<RoleMapper, Role> {

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询实体
     * @return 分页数据
     */
    public IPage<RoleVO> pageRoleList(RoleQueryDTO queryDTO) {
        Page<RoleVO> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());

        return getBaseMapper().selectRolePage(page, queryDTO);
    }
}
