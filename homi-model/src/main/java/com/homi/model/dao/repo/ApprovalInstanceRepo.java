package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.ApprovalInstance;
import com.homi.model.dao.mapper.ApprovalInstanceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批实例 Repo
 */
@Repository
public class ApprovalInstanceRepo extends ServiceImpl<ApprovalInstanceMapper, ApprovalInstance> {

    /**
     * 根据业务类型和业务ID获取审批实例
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 审批实例
     */
    public ApprovalInstance getByBiz(String bizType, Long bizId) {
        return lambdaQuery()
            .eq(ApprovalInstance::getBizType, bizType)
            .eq(ApprovalInstance::getBizId, bizId)
            .eq(ApprovalInstance::getDeleted, false)
            .one();
    }

    /**
     * 根据审批单号获取实例
     *
     * @param instanceNo 审批单号
     * @return 审批实例
     */
    public ApprovalInstance getByInstanceNo(String instanceNo) {
        return lambdaQuery()
            .eq(ApprovalInstance::getInstanceNo, instanceNo)
            .eq(ApprovalInstance::getDeleted, false)
            .one();
    }

    /**
     * 获取申请人的审批列表（我发起的）
     *
     * @param applicantId 申请人ID
     * @param status      状态（可选）
     * @param page        分页参数
     * @return 审批列表
     */
    public Page<ApprovalInstance> pageByApplicant(Long applicantId, Integer status, Page<ApprovalInstance> page) {
        return lambdaQuery()
            .eq(ApprovalInstance::getApplicantId, applicantId)
            .eq(status != null, ApprovalInstance::getStatus, status)
            .eq(ApprovalInstance::getDeleted, false)
            .orderByDesc(ApprovalInstance::getCreateTime)
            .page(page);
    }

    /**
     * 获取公司的审批列表
     *
     * @param companyId 公司ID
     * @param bizType   业务类型（可选）
     * @param status    状态（可选）
     * @param page      分页参数
     * @return 审批列表
     */
    public Page<ApprovalInstance> pageByCompany(Long companyId, String bizType, Integer status, Page<ApprovalInstance> page) {
        return lambdaQuery()
            .eq(ApprovalInstance::getCompanyId, companyId)
            .eq(bizType != null, ApprovalInstance::getBizType, bizType)
            .eq(status != null, ApprovalInstance::getStatus, status)
            .eq(ApprovalInstance::getDeleted, false)
            .orderByDesc(ApprovalInstance::getCreateTime)
            .page(page);
    }

    /**
     * 更新审批实例状态
     *
     * @param instanceId 实例ID
     * @param status     状态
     * @return 是否成功
     */
    public boolean updateStatus(Long instanceId, Integer status) {
        return lambdaUpdate()
            .eq(ApprovalInstance::getId, instanceId)
            .set(ApprovalInstance::getStatus, status)
            .update();
    }

    /**
     * 检查业务是否有进行中的审批
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return true=有进行中的审批
     */
    public boolean hasPendingApproval(String bizType, Long bizId) {
        return lambdaQuery()
            .eq(ApprovalInstance::getBizType, bizType)
            .eq(ApprovalInstance::getBizId, bizId)
            .eq(ApprovalInstance::getStatus, 1) // 审批中
            .eq(ApprovalInstance::getDeleted, false)
            .exists();
    }

    /**
     * 统计指定状态的审批数量
     *
     * @param companyId 公司ID
     * @param status    状态
     * @return 数量
     */
    public long countByStatus(Long companyId, Integer status) {
        return lambdaQuery()
            .eq(ApprovalInstance::getCompanyId, companyId)
            .eq(ApprovalInstance::getStatus, status)
            .eq(ApprovalInstance::getDeleted, false)
            .count();
    }
}
