package com.homi.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homi.domain.dto.user.UserQueryDTO;
import com.homi.domain.dto.user.UserVO;
import com.homi.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2025-04-17
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 自定义分页查询
     *
     * @param page  分页对象
     * @param query 查询对象
     * @return 查询结果
     */
    IPage<UserVO> selectUserList(IPage<UserVO> page, @Param("query") UserQueryDTO query);
}
