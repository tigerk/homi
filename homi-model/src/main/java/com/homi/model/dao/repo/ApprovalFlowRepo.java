package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.ApprovalFlow;
import com.homi.model.dao.mapper.ApprovalFlowMapper;
import org.springframework.stereotype.Repository;

/**
 * 审批流程 Repo
 */
@Repository
public class ApprovalFlowRepo extends ServiceImpl<ApprovalFlowMapper, ApprovalFlow> {

    /**
     * 获取公司指定业务类型的启用流程
     *
     * @param companyId 公司ID
     * @param bizType   业务类型
     * @return 审批流程
     */
    public ApprovalFlow getEnabledFlow(Long companyId, String bizType) {
        return lambdaQuery()
            .eq(ApprovalFlow::getCompanyId, companyId)
            .eq(ApprovalFlow::getBizType, bizType)
            .eq(ApprovalFlow::getEnabled, true)
            .one();
    }

    /**
     * 根据流程编码获取流程
     *
     * @param flowCode 流程编码
     * @return 审批流程
     */
    public ApprovalFlow getByFlowCode(String flowCode) {
        return lambdaQuery()
            .eq(ApprovalFlow::getFlowCode, flowCode)
            .one();
    }

    /**
     * 获取公司所有流程列表
     *
     * @param companyId 公司ID
     * @return 流程列表
     */
    public java.util.List<ApprovalFlow> listByCompanyId(Long companyId) {
        return lambdaQuery()
            .eq(ApprovalFlow::getCompanyId, companyId)
            .eq(ApprovalFlow::getDeleted, false)
            .orderByDesc(ApprovalFlow::getCreateTime)
            .list();
    }

    /**
     * 检查业务类型是否已存在流程
     *
     * @param companyId  公司ID
     * @param bizType    业务类型
     * @param excludeId  排除的流程ID（编辑时使用）
     * @return true=已存在
     */
    public boolean existsByBizType(Long companyId, String bizType, Long excludeId) {
        return lambdaQuery()
            .eq(ApprovalFlow::getCompanyId, companyId)
            .eq(ApprovalFlow::getBizType, bizType)
            .eq(ApprovalFlow::getDeleted, false)
            .ne(excludeId != null, ApprovalFlow::getId, excludeId)
            .exists();
    }

    /**
     * 启用/停用流程
     *
     * @param flowId  流程ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    public boolean updateEnabled(Long flowId, Boolean enabled) {
        return lambdaUpdate()
            .eq(ApprovalFlow::getId, flowId)
            .set(ApprovalFlow::getEnabled, enabled)
            .update();
    }
}
