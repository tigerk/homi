package com.homi.service.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.ApprovalActionStatusEnum;
import com.homi.common.lib.enums.approval.ApprovalStatusEnum;
import com.homi.common.lib.enums.approval.ApproverTypeEnum;
import com.homi.model.approval.dto.ApprovalHandleDTO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统一审批服务
 */
@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalFlowRepo approvalFlowRepo;
    private final ApprovalNodeRepo approvalNodeRepo;
    private final ApprovalInstanceRepo approvalInstanceRepo;
    private final ApprovalActionRepo approvalActionRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final CompanyUserRepo companyUserRepo;
    private final DeptRepo deptRepo;

    /**
     * 检查业务是否需要审批
     */
    public boolean needApproval(Long companyId, String bizType) {
        ApprovalFlow flow = approvalFlowRepo.getEnabledFlow(companyId, bizType);
        if (flow == null) {
            return false;
        }
        List<ApprovalNode> nodes = approvalNodeRepo.getNodesByFlowId(flow.getId());
        return CollUtil.isNotEmpty(nodes);
    }

    /**
     * 提交审批
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submitApproval(ApprovalSubmitDTO dto) {
        // 1. 获取审批流程配置
        ApprovalFlow flow = approvalFlowRepo.getEnabledFlow(dto.getCompanyId(), dto.getBizType());
        if (flow == null) {
            throw new IllegalArgumentException("未配置审批流程");
        }

        List<ApprovalNode> nodes = approvalNodeRepo.getNodesByFlowId(flow.getId());
        if (CollUtil.isEmpty(nodes)) {
            throw new IllegalArgumentException("审批流程未配置审批节点");
        }

        // 2. 检查是否已存在审批实例
        ApprovalInstance existInstance = approvalInstanceRepo.getByBiz(dto.getBizType(), dto.getBizId());
        if (existInstance != null && Objects.equals(existInstance.getStatus(), ApprovalStatusEnum.PENDING.getCode())) {
            throw new IllegalArgumentException("该业务已在审批中，请勿重复提交");
        }

        // 3. 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceNo(generateInstanceNo());
        instance.setCompanyId(dto.getCompanyId());
        instance.setFlowId(flow.getId());
        instance.setBizType(dto.getBizType());
        instance.setBizId(dto.getBizId());
        instance.setTitle(dto.getTitle());
        instance.setApplicantId(dto.getApplicantId());
        instance.setStatus(ApprovalStatusEnum.PENDING.getCode());
        instance.setCurrentNodeOrder(1);
        instance.setCreateBy(dto.getApplicantId());
        instance.setCreateTime(new Date());

        // 设置第一个节点
        ApprovalNode firstNode = nodes.getFirst();
        instance.setCurrentNodeId(firstNode.getId());

        approvalInstanceRepo.save(instance);

        // 4. 创建审批动作（第一个节点的待审批动作）
        createApprovalActions(instance, firstNode);

        // 5. 更新业务表状态（通过事件通知）
        publishStatusChangeEvent(dto.getBizType(), dto.getBizId(), ApprovalStatusEnum.PENDING.getCode());

        return instance.getId();
    }

    /**
     * 处理审批（通过/驳回）
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(ApprovalHandleDTO dto) {
        // 1. 获取审批实例
        ApprovalInstance instance = approvalInstanceRepo.getById(dto.getInstanceId());
        if (instance == null) {
            throw new IllegalArgumentException("审批实例不存在");
        }
        if (!Objects.equals(instance.getStatus(), ApprovalStatusEnum.PENDING.getCode())) {
            throw new IllegalArgumentException("该审批已结束");
        }

        // 2. 获取当前待审批动作
        ApprovalAction action = approvalActionRepo.getPendingAction(dto.getInstanceId(), dto.getApproverId());
        if (action == null) {
            throw new IllegalArgumentException("您没有该审批的处理权限");
        }

        // 3. 更新审批动作
        action.setAction(dto.getAction());
        action.setRemark(dto.getRemark());
        action.setOperateTime(new Date());
        action.setStatus(1); // 已审批
        approvalActionRepo.updateById(action);

        // 4. 根据操作类型处理
        if (dto.getAction() == 1) {
            // 通过
            handleApprove(instance, action);
        } else if (dto.getAction() == 2) {
            // 驳回
            handleReject(instance, action, dto.getRemark());
        }
    }

    /**
     * 撤回审批
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long instanceId, Long operatorId) {
        ApprovalInstance instance = approvalInstanceRepo.getById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("审批实例不存在");
        }
        if (!Objects.equals(instance.getApplicantId(), operatorId)) {
            throw new IllegalArgumentException("只有申请人才能撤回");
        }
        if (!Objects.equals(instance.getStatus(), ApprovalStatusEnum.PENDING.getCode())) {
            throw new IllegalArgumentException("只有审批中的申请才能撤回");
        }

        // 更新实例状态
        instance.setStatus(ApprovalStatusEnum.WITHDRAWN.getCode());
        instance.setFinishTime(new Date());
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        // 将待审批动作标记为已跳过
        approvalActionRepo.skipPendingActions(instanceId);

        // 通知业务层
        publishStatusChangeEvent(instance.getBizType(), instance.getBizId(), ApprovalStatusEnum.WITHDRAWN.getCode());
    }

    // ==================== 私有方法 ====================

    private void handleApprove(ApprovalInstance instance, ApprovalAction action) {
        ApprovalNode currentNode = approvalNodeRepo.getById(instance.getCurrentNodeId());

        // 检查会签情况
        if (currentNode.getMultiApproveType() == 2) {
            Long pendingCount = approvalActionRepo.countPendingByNode(instance.getId(), currentNode.getId());
            if (pendingCount > 0) {
                return;
            }
        }

        // 获取下一个节点
        List<ApprovalNode> allNodes = approvalNodeRepo.getNodesByFlowId(instance.getFlowId());
        int nextOrder = instance.getCurrentNodeOrder() + 1;

        ApprovalNode nextNode = allNodes.stream()
            .filter(n -> n.getNodeOrder() == nextOrder)
            .findFirst()
            .orElse(null);

        if (nextNode == null) {
            // 审批完成
            instance.setStatus(ApprovalStatusEnum.APPROVED.getCode());
            instance.setFinishTime(new Date());
            instance.setResultRemark("审批通过");
            approvalInstanceRepo.updateById(instance);

            publishStatusChangeEvent(instance.getBizType(), instance.getBizId(), ApprovalStatusEnum.APPROVED.getCode());
        } else {
            // 流转到下一个节点
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeOrder(nextOrder);
            instance.setUpdateTime(new Date());
            approvalInstanceRepo.updateById(instance);

            createApprovalActions(instance, nextNode);
        }
    }

    private void handleReject(ApprovalInstance instance, ApprovalAction action, String remark) {
        instance.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        instance.setFinishTime(new Date());
        instance.setResultRemark(remark);
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        approvalActionRepo.skipPendingActions(instance.getId());

        publishStatusChangeEvent(instance.getBizType(), instance.getBizId(), ApprovalStatusEnum.REJECTED.getCode());
    }

    private void createApprovalActions(ApprovalInstance instance, ApprovalNode node) {
        List<Long> approverIds = getApproverIds(node, instance);

        for (Long approverId : approverIds) {
            ApprovalAction action = new ApprovalAction();
            action.setInstanceId(instance.getId());
            action.setNodeId(node.getId());
            action.setNodeOrder(node.getNodeOrder());
            action.setNodeName(node.getNodeName());
            action.setApproverId(approverId);
            action.setStatus(ApprovalActionStatusEnum.PENDING.getCode());
            action.setCreateTime(new Date());
            approvalActionRepo.save(action);
        }
    }

    private List<Long> getApproverIds(ApprovalNode node, ApprovalInstance instance) {
        ApproverTypeEnum approverTypeEnum = Objects.requireNonNull(ApproverTypeEnum.fromCode(node.getApproverType()));

        if (ApproverTypeEnum.SPECIFIC_USER.equals(approverTypeEnum)) {
            return JSONUtil.toList(node.getApproverIds(), Long.class);
        }

        if (ApproverTypeEnum.SPECIFIC_ROLE.equals(approverTypeEnum)) {
            List<Long> list = JSONUtil.toList(node.getApproverIds(), Long.class);
            return companyUserRepo.getListByRoleIds(list).stream()
                .map(CompanyUser::getUserId)
                .collect(Collectors.toList());
        }

        if (ApproverTypeEnum.DEPARTMENT_SUPERVISOR.equals(approverTypeEnum)) {
            Long createBy = instance.getCreateBy();
            CompanyUser companyUser = companyUserRepo.getCompanyUser(instance.getCompanyId(), createBy);
            if (companyUser == null) {
                return List.of();
            }

            Dept dept = deptRepo.getById(companyUser.getDeptId());
            if (dept == null) {
                return List.of();
            }
            return List.of(dept.getSupervisorId());
        }

        return List.of();
    }

    private String generateInstanceNo() {
        return "AP" + IdUtil.getSnowflakeNextIdStr();
    }

    private void publishStatusChangeEvent(String bizType, Long bizId, Integer status) {
        ApprovalStatusChangeEvent event = new ApprovalStatusChangeEvent(this, bizType, bizId, status);
        eventPublisher.publishEvent(event);
    }
}
