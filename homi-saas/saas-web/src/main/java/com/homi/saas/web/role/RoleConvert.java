package com.homi.saas.web.role;

import com.homi.saas.web.auth.vo.account.AccountRoleVO;
import com.homi.model.vo.role.RoleSimpleVO;
import com.homi.model.dao.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    List<RoleSimpleVO> convertSimpleList(List<Role> roleList);

    List<AccountRoleVO> convertAccountRoleList(List<Role> roleList);
}
