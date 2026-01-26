package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.ApprovalAction;
import com.homi.model.dao.mapper.ApprovalActionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批动作 Repo
 */
@Repository
public class ApprovalActionRepo extends ServiceImpl<ApprovalActionMapper, ApprovalAction> {

    /**
     * 获取实例的所有审批动作（按节点顺序排列）
     *
     * @param instanceId 实例ID
     * @return 审批动作列表
     */
    public List<ApprovalAction> listByInstanceId(Long instanceId) {
        return lambdaQuery()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .orderByAsc(ApprovalAction::getNodeOrder)
            .orderByAsc(ApprovalAction::getCreateTime)
            .list();
    }

    /**
     * 获取审批人的待审批动作
     *
     * @param instanceId 实例ID
     * @param approverId 审批人ID
     * @return 待审批动作
     */
    public ApprovalAction getPendingAction(Long instanceId, Long approverId) {
        return lambdaQuery()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .eq(ApprovalAction::getApproverId, approverId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .one();
    }

    /**
     * 统计节点的待审批数量
     *
     * @param instanceId 实例ID
     * @param nodeId     节点ID
     * @return 待审批数量
     */
    public Long countPendingByNode(Long instanceId, Long nodeId) {
        return lambdaQuery()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .eq(ApprovalAction::getNodeId, nodeId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .count();
    }

    /**
     * 将实例的所有待审批动作标记为已跳过
     *
     * @param instanceId 实例ID
     * @return 是否成功
     */
    public boolean skipPendingActions(Long instanceId) {
        return lambdaUpdate()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .set(ApprovalAction::getStatus, 2) // 已跳过
            .update();
    }

    /**
     * 获取审批人的待办列表（我待审批的）
     *
     * @param approverId 审批人ID
     * @param page       分页参数
     * @return 待办列表
     */
    public Page<ApprovalAction> pagePendingByApprover(Long approverId, Page<ApprovalAction> page) {
        return lambdaQuery()
            .eq(ApprovalAction::getApproverId, approverId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .orderByDesc(ApprovalAction::getCreateTime)
            .page(page);
    }

    /**
     * 获取审批人的已办列表（我已审批的）
     *
     * @param approverId 审批人ID
     * @param page       分页参数
     * @return 已办列表
     */
    public Page<ApprovalAction> pageHandledByApprover(Long approverId, Page<ApprovalAction> page) {
        return lambdaQuery()
            .eq(ApprovalAction::getApproverId, approverId)
            .eq(ApprovalAction::getStatus, 1) // 已审批
            .orderByDesc(ApprovalAction::getOperateTime)
            .page(page);
    }

    /**
     * 统计审批人的待办数量
     *
     * @param approverId 审批人ID
     * @return 待办数量
     */
    public long countPendingByApprover(Long approverId) {
        return lambdaQuery()
            .eq(ApprovalAction::getApproverId, approverId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .count();
    }

    /**
     * 检查审批人是否有待审批动作
     *
     * @param instanceId 实例ID
     * @param approverId 审批人ID
     * @return true=有待审批
     */
    public boolean hasPendingAction(Long instanceId, Long approverId) {
        return lambdaQuery()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .eq(ApprovalAction::getApproverId, approverId)
            .eq(ApprovalAction::getStatus, 0) // 待审批
            .exists();
    }

    /**
     * 获取节点的所有审批动作
     *
     * @param instanceId 实例ID
     * @param nodeId     节点ID
     * @return 审批动作列表
     */
    public List<ApprovalAction> listByNode(Long instanceId, Long nodeId) {
        return lambdaQuery()
            .eq(ApprovalAction::getInstanceId, instanceId)
            .eq(ApprovalAction::getNodeId, nodeId)
            .orderByAsc(ApprovalAction::getCreateTime)
            .list();
    }
}
