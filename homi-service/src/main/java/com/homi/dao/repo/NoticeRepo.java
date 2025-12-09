package com.homi.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.dao.entity.Notice;
import com.homi.dao.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-07
 */
@Service
public class NoticeRepo extends ServiceImpl<NoticeMapper, Notice> {

}
