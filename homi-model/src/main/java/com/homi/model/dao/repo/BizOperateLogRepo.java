package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.BizOperateLog;
import com.homi.model.dao.mapper.BizOperateLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BizOperateLogRepo extends ServiceImpl<BizOperateLogMapper, BizOperateLog> {
}
