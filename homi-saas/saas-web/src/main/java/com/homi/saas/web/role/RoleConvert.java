package com.homi.saas.web.role;

import com.homi.model.dao.entity.Role;
import com.homi.model.vo.role.RoleSimpleVO;
import com.homi.model.vo.role.RoleVO;
import com.homi.saas.web.auth.vo.account.AccountRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    List<RoleSimpleVO> convertSimpleList(List<Role> roleList);

    /**
     * 转换为角色VO
     *
     * @param role 角色实体
     * @return 角色VO
     */
    RoleVO toRoleVO(Role role);

    List<AccountRoleVO> convertAccountRoleList(List<Role> roleList);
}
