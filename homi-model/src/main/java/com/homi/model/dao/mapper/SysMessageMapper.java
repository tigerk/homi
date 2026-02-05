package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 站内信/个人消息表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

}
