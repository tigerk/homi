package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.model.dao.entity.Role;
import com.homi.model.dto.role.RoleQueryDTO;
import com.homi.model.vo.role.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 角色信息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 分页查询角色列表
     *
     * @param page  分页对象
     * @param query 查询参数
     * @return 角色列表
     */
    IPage<RoleVO> selectRolePage(IPage<RoleVO> page, @Param("query") RoleQueryDTO query);
}
