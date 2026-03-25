package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.TrialApplication;
import com.homi.model.dao.mapper.TrialApplicationMapper;
import org.springframework.stereotype.Service;

@Service
public class TrialApplicationRepo extends ServiceImpl<TrialApplicationMapper, TrialApplication> {
}
