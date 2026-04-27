package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.enums.biz.BizOperateSourceTypeEnum;
import com.homi.model.dao.entity.BizOperateLog;
import com.homi.model.dao.mapper.BizOperateLogMapper;
import com.homi.model.owner.vo.BizOperateLogVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BizOperateLogRepo extends ServiceImpl<BizOperateLogMapper, BizOperateLog> {

    public List<BizOperateLogVO> listByBiz(String bizType, Long bizId) {
        return list(new LambdaQueryWrapper<BizOperateLog>()
                .eq(BizOperateLog::getBizType, bizType)
                .eq(BizOperateLog::getBizId, bizId)
                .orderByDesc(BizOperateLog::getId))
            .stream()
            .map(this::toVO)
            .toList();
    }

    /**
     * 根据业务或来源查询操作日志
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/4/27 10:36
     * {@code @description} 根据业务或来源查询操作日志
     * {@code @author} tk
     * {@code @date} 2026/4/27 10:36
     * {@code @param} companyId 参数说明
     * {@code @param} bizType 参数说明
     * {@code @param} bizId 参数说明
     * {@code @param} sourceType 参数说明
     * {@code @param} sourceId 参数说明
     * {@code @return} java.util.List<com.homi.model.owner.vo.BizOperateLogVO>
     */
    public List<BizOperateLogVO> listByBizOrSource(Long companyId, String bizType, Long bizId, BizOperateSourceTypeEnum sourceType, Long sourceId) {
        if (bizId == null && sourceId == null) {
            return List.of();
        }
        LambdaQueryWrapper<BizOperateLog> wrapper = new LambdaQueryWrapper<>();
        if (companyId != null) {
            wrapper.eq(BizOperateLog::getCompanyId, companyId);
        }
        wrapper.and(item -> item
            .and(biz -> biz.eq(BizOperateLog::getBizType, bizType).eq(BizOperateLog::getBizId, bizId))
            .or(source -> source.eq(BizOperateLog::getSourceType, sourceType.getCode()).eq(BizOperateLog::getSourceId, sourceId)));
        return list(wrapper.orderByDesc(BizOperateLog::getId))
            .stream()
            .map(this::toVO)
            .toList();
    }

    public BizOperateLogVO toVO(BizOperateLog item) {
        BizOperateLogVO vo = new BizOperateLogVO();
        vo.setId(item.getId());
        vo.setBizType(item.getBizType());
        vo.setBizId(item.getBizId());
        vo.setOperateType(item.getOperateType());
        vo.setOperateDesc(item.getOperateDesc());
        vo.setRemark(item.getRemark());
        vo.setExtraData(item.getExtraData());
        vo.setSourceType(item.getSourceType());
        vo.setSourceId(item.getSourceId());
        vo.setOperatorId(item.getOperatorId());
        vo.setOperatorName(item.getOperatorName());
        vo.setCreateAt(item.getCreateAt());
        return vo;
    }
}
