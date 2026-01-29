package com.homi.service.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.*;
import com.homi.model.approval.dto.ApprovalHandleDTO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.dao.entity.*;
import com.homi.model.dao.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一审批服务
 */
@Slf4j
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
        if (existInstance != null && ApprovalStatusEnum.PENDING.getCode().equals(existInstance.getStatus())) {
            throw new IllegalArgumentException("该业务已在审批中，请勿重复提交");
        }

        // 3. 创建审批实例
        ApprovalInstance instance = buildApprovalInstance(dto, flow, nodes);
        approvalInstanceRepo.save(instance);

        // 4. 创建审批动作（第一个节点的待审批动作）
        ApprovalNode firstNode = nodes.get(0);
        createApprovalActions(instance, firstNode);

        // 5. 发布状态变更事件
        publishStatusChangeEvent(
            dto.getBizType(),
            dto.getBizId(),
            ApprovalStatusEnum.PENDING.getCode()
        );

        log.info("审批提交成功: instanceId={}, bizType={}, bizId={}",
            instance.getId(), dto.getBizType(), dto.getBizId());

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
        if (!ApprovalStatusEnum.PENDING.getCode().equals(instance.getStatus())) {
            throw new IllegalArgumentException("该审批已结束");
        }

        // 2. 获取当前待审批动作
        ApprovalAction action = approvalActionRepo.getPendingAction(
            dto.getInstanceId(),
            dto.getApproverId()
        );
        if (action == null) {
            throw new IllegalArgumentException("您没有该审批的处理权限");
        }

        // 3. 更新审批动作
        updateApprovalAction(action, dto);

        // 4. 根据操作类型处理
        if (ApprovalActionTypeEnum.APPROVE.getCode().equals(dto.getAction())) {
            handleApprove(instance, action);
        } else if (ApprovalActionTypeEnum.REJECT.getCode().equals(dto.getAction())) {
            handleReject(instance, action, dto.getRemark());
        } else if (ApprovalActionTypeEnum.TRANSFER.getCode().equals(dto.getAction())) {
            handleTransfer(instance, action, dto.getTransferToId());
        }

        log.info("审批处理成功: instanceId={}, approverId={}, action={}",
            dto.getInstanceId(), dto.getApproverId(), dto.getAction());
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
        if (!instance.getApplicantId().equals(operatorId)) {
            throw new IllegalArgumentException("只有申请人才能撤回");
        }
        if (!ApprovalStatusEnum.PENDING.getCode().equals(instance.getStatus())) {
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
        publishStatusChangeEvent(
            instance.getBizType(),
            instance.getBizId(),
            ApprovalStatusEnum.WITHDRAWN.getCode()
        );

        log.info("审批撤回成功: instanceId={}, operatorId={}", instanceId, operatorId);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建审批实例
     */
    private ApprovalInstance buildApprovalInstance(ApprovalSubmitDTO dto, ApprovalFlow flow, List<ApprovalNode> nodes) {
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
        instance.setCurrentNodeId(nodes.get(0).getId());
        instance.setCreateBy(dto.getApplicantId());
        instance.setCreateTime(new Date());
        return instance;
    }

    /**
     * 更新审批动作
     */
    private void updateApprovalAction(ApprovalAction action, ApprovalHandleDTO dto) {
        action.setAction(dto.getAction());
        action.setRemark(dto.getRemark());
        action.setOperateTime(new Date());
        action.setStatus(ApprovalActionStatusEnum.APPROVED.getCode());
        approvalActionRepo.updateById(action);
    }

    /**
     * 处理通过
     */
    private void handleApprove(ApprovalInstance instance, ApprovalAction action) {
        ApprovalNode currentNode = approvalNodeRepo.getById(instance.getCurrentNodeId());

        // 检查会签情况
        if (MultiApproveEnum.AND_SIGN.getCode() == currentNode.getMultiApproveType()) {
            Long pendingCount = approvalActionRepo.countPendingByNode(
                instance.getId(),
                currentNode.getId()
            );
            if (pendingCount > 0) {
                log.info("会签模式，还有{}人未审批", pendingCount);
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
            completeApproval(instance);
        } else {
            // 流转到下一个节点
            moveToNextNode(instance, nextNode);
        }
    }

    /**
     * 完成审批
     */
    private void completeApproval(ApprovalInstance instance) {
        instance.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        instance.setFinishTime(new Date());
        instance.setResultRemark("审批通过");
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        publishStatusChangeEvent(
            instance.getBizType(),
            instance.getBizId(),
            ApprovalStatusEnum.APPROVED.getCode()
        );

        log.info("审批流程完成: instanceId={}", instance.getId());
    }

    /**
     * 流转到下一个节点
     */
    private void moveToNextNode(ApprovalInstance instance, ApprovalNode nextNode) {
        instance.setCurrentNodeId(nextNode.getId());
        instance.setCurrentNodeOrder(nextNode.getNodeOrder());
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        createApprovalActions(instance, nextNode);

        log.info("审批流转到下一节点: instanceId={}, nodeOrder={}, nodeName={}",
            instance.getId(), nextNode.getNodeOrder(), nextNode.getNodeName());
    }

    /**
     * 处理驳回
     */
    private void handleReject(ApprovalInstance instance, ApprovalAction action, String remark) {
        instance.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        instance.setFinishTime(new Date());
        instance.setResultRemark(remark);
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        // 跳过所有待审批动作
        approvalActionRepo.skipPendingActions(instance.getId());

        publishStatusChangeEvent(
            instance.getBizType(),
            instance.getBizId(),
            ApprovalStatusEnum.REJECTED.getCode()
        );

        log.info("审批驳回: instanceId={}, remark={}", instance.getId(), remark);
    }

    /**
     * 处理转交
     */
    private void handleTransfer(ApprovalInstance instance, ApprovalAction action, Long transferToId) {
        if (transferToId == null) {
            throw new IllegalArgumentException("转交目标人不能为空");
        }

        // 创建新的审批动作给转交目标人
        ApprovalAction newAction = new ApprovalAction();
        newAction.setInstanceId(instance.getId());
        newAction.setNodeId(action.getNodeId());
        newAction.setNodeOrder(action.getNodeOrder());
        newAction.setNodeName(action.getNodeName());
        newAction.setApproverId(transferToId);
        newAction.setStatus(ApprovalActionStatusEnum.PENDING.getCode());
        newAction.setCreateTime(new Date());
        approvalActionRepo.save(newAction);

        log.info("审批转交成功: instanceId={}, fromUserId={}, toUserId={}",
            instance.getId(), action.getApproverId(), transferToId);
    }

    /**
     * 创建审批动作
     */
    private void createApprovalActions(ApprovalInstance instance, ApprovalNode node) {
        List<Long> approverIds = getApproverIds(node, instance);

        if (CollUtil.isEmpty(approverIds)) {
            log.warn("节点没有审批人: nodeId={}, nodeName={}", node.getId(), node.getNodeName());
            return;
        }

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

        log.info("创建审批动作: instanceId={}, nodeId={}, approverCount={}", instance.getId(), node.getId(), approverIds.size());
    }

    /**
     * 获取审批人ID列表
     */
    private List<Long> getApproverIds(ApprovalNode node, ApprovalInstance instance) {
        ApproverTypeEnum approverType = ApproverTypeEnum.fromCode(node.getApproverType());
        if (approverType == null) {
            log.error("未知的审批人类型: {}", node.getApproverType());
            return List.of();
        }

        return switch (approverType) {
            case SPECIFIC_USER -> getSpecificUsers(node);
            case SPECIFIC_ROLE -> getUsersByRole(node);
            case DEPARTMENT_SUPERVISOR -> getDepartmentSupervisor(instance);
            case SELF_OPTION -> List.of(); // 自选需要在提交时指定
        };
    }

    /**
     * 获取指定用户
     */
    private List<Long> getSpecificUsers(ApprovalNode node) {
        return JSONUtil.toList(node.getApproverIds(), Long.class);
    }

    /**
     * 根据角色获取用户
     */
    private List<Long> getUsersByRole(ApprovalNode node) {
        List<Long> roleIds = JSONUtil.toList(node.getApproverIds(), Long.class);
        return companyUserRepo.getListByRoleIds(roleIds).stream()
            .map(CompanyUser::getUserId)
            .collect(Collectors.toList());
    }

    /**
     * 获取部门主管
     */
    private List<Long> getDepartmentSupervisor(ApprovalInstance instance) {
        CompanyUser companyUser = companyUserRepo.getCompanyUser(
            instance.getCompanyId(),
            instance.getCreateBy()
        );
        if (companyUser == null || companyUser.getDeptId() == null) {
            log.warn("申请人没有所属部门: userId={}", instance.getCreateBy());
            return List.of();
        }

        Dept dept = deptRepo.getById(companyUser.getDeptId());
        if (dept == null || dept.getSupervisorId() == null) {
            log.warn("部门没有主管: deptId={}", companyUser.getDeptId());
            return List.of();
        }

        return List.of(dept.getSupervisorId());
    }

    /**
     * 生成实例编号
     */
    private String generateInstanceNo() {
        return "AP" + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 发布状态变更事件
     */
    private void publishStatusChangeEvent(String bizType, Long bizId, Integer status) {
        ApprovalStatusChangeEvent event = new ApprovalStatusChangeEvent(
            this,
            bizType,
            bizId,
            status
        );
        eventPublisher.publishEvent(event);
    }
}
