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

    /**
     * 记录业务日志
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/17 16:18

     * @param companyId 公司 ID
     * @param bizType 业务
     * @param bizId 业务 ID
     * @param operateType 操作类型
     * @param operateDesc 操作描述
     * @param remark 备注
     * @param beforeSnapshot 业务操作前快照
     * @param afterSnapshot 业务操作后快照
     * @param extraData 额外数据
     * @param sourceType 源类型
     * @param sourceId 源 ID
     * @param operatorId 操作员 ID
     * @param operatorName 操作员名称

     */
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
        log.setCreateAt(new Date());
        log.setUpdateAt(log.getCreateAt());
        bizOperateLogRepo.save(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        return JSONUtil.toJsonStr(value);
    }
}
