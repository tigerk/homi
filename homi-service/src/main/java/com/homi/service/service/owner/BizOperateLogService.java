package com.homi.service.service.owner;

import cn.hutool.json.JSONUtil;
import com.homi.model.dao.entity.BizOperateLog;
import com.homi.model.dao.repo.BizOperateLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BizOperateLogService {
    private final BizOperateLogRepo bizOperateLogRepo;

    public void saveLog(Long companyId, String bizType, Long bizId, String operateType, String operateDesc,
                        String remark, Object beforeSnapshot, Object afterSnapshot, Map<String, Object> extraData,
                        String sourceType, Long sourceId, Long operatorId, String operatorName) {
        BizOperateLog log = new BizOperateLog();
        log.setCompanyId(companyId);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setOperateType(operateType);
        log.setOperateDesc(operateDesc);
        log.setRemark(remark);
        log.setBeforeSnapshot(toJson(beforeSnapshot));
        log.setAfterSnapshot(toJson(afterSnapshot));
        log.setExtraData(toJson(extraData));
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(new Date());
        log.setUpdateTime(log.getCreateTime());
        bizOperateLogRepo.save(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        return JSONUtil.toJsonStr(value);
    }
}
