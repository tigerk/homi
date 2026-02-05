package com.homi.model.dao.repo;

import com.homi.model.dao.entity.SysMessage;
import com.homi.model.dao.mapper.SysMessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 站内信/个人消息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysMessageRepo extends ServiceImpl<SysMessageMapper, SysMessage> {

}
