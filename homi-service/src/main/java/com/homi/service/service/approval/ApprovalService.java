package com.homi.service.service.approval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalHandleDTO;
import com.homi.model.approval.dto.ApprovalQueryDTO;
import com.homi.model.approval.dto.ApprovalSubmitDTO;
import com.homi.model.approval.vo.ApprovalActionVO;
import com.homi.model.approval.vo.ApprovalInstanceVO;
import com.homi.model.approval.vo.ApprovalTodoVO;
import com.homi.model.dao.entity.ApprovalAction;
import com.homi.model.dao.entity.ApprovalFlow;
import com.homi.model.dao.entity.ApprovalInstance;
import com.homi.model.dao.entity.ApprovalNode;
import com.homi.model.dao.repo.ApprovalActionRepo;
import com.homi.model.dao.repo.ApprovalFlowRepo;
import com.homi.model.dao.repo.ApprovalInstanceRepo;
import com.homi.model.dao.repo.ApprovalNodeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    /**
     * 检查业务是否需要审批
     *
     * @param companyId 公司ID
     * @param bizType   业务类型
     * @return true=需要审批
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
     *
     * @param dto 提交参数
     * @return 审批实例ID
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
        if (existInstance != null && existInstance.getStatus() == ApprovalStatusEnum.PENDING.getCode()) {
            throw new IllegalArgumentException("该业务已在审批中，请勿重复提交");
        }

        // 3. 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceNo(generateInstanceNo());
        instance.setCompanyId(dto.getCompanyId());
        instance.setFlowId(flow.getId());
        instance.setBizType(dto.getBizType());
        instance.setBizId(dto.getBizId());
        instance.setBizCode(dto.getBizCode());
        instance.setTitle(dto.getTitle());
        instance.setApplicantId(dto.getApplicantId());
        instance.setApplicantName(dto.getApplicantName());
        instance.setStatus(ApprovalStatusEnum.PENDING.getCode());
        instance.setCurrentNodeOrder(1);
        instance.setCreateBy(dto.getApplicantId());
        instance.setCreateTime(new Date());

        // 设置第一个节点
        ApprovalNode firstNode = nodes.get(0);
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
     *
     * @param dto 处理参数
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

    /**
     * 处理通过逻辑
     */
    private void handleApprove(ApprovalInstance instance, ApprovalAction action) {
        ApprovalNode currentNode = approvalNodeRepo.getById(instance.getCurrentNodeId());

        // 检查会签情况（如果是会签，需要所有人都通过）
        if (currentNode.getMultiApproveType() == 2) {
            Long pendingCount = approvalActionRepo.countPendingByNode(instance.getId(), currentNode.getId());
            if (pendingCount > 0) {
                // 还有人未审批，等待
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
            // 没有下一个节点，审批完成
            instance.setStatus(ApprovalStatusEnum.APPROVED.getCode());
            instance.setFinishTime(new Date());
            instance.setResultRemark("审批通过");
            approvalInstanceRepo.updateById(instance);

            // 通知业务层
            publishStatusChangeEvent(instance.getBizType(), instance.getBizId(), ApprovalStatusEnum.APPROVED.getCode());
        } else {
            // 流转到下一个节点
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeOrder(nextOrder);
            instance.setUpdateTime(new Date());
            approvalInstanceRepo.updateById(instance);

            // 创建下一个节点的审批动作
            createApprovalActions(instance, nextNode);
        }
    }

    /**
     * 处理驳回逻辑
     */
    private void handleReject(ApprovalInstance instance, ApprovalAction action, String remark) {
        instance.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        instance.setFinishTime(new Date());
        instance.setResultRemark(remark);
        instance.setUpdateTime(new Date());
        approvalInstanceRepo.updateById(instance);

        // 将其他待审批动作标记为已跳过
        approvalActionRepo.skipPendingActions(instance.getId());

        // 通知业务层
        publishStatusChangeEvent(instance.getBizType(), instance.getBizId(), ApprovalStatusEnum.REJECTED.getCode());
    }

    /**
     * 创建审批动作
     */
    private void createApprovalActions(ApprovalInstance instance, ApprovalNode node) {
        List<Long> approverIds = getApproverIds(node, instance);

        for (Long approverId : approverIds) {
            ApprovalAction action = new ApprovalAction();
            action.setInstanceId(instance.getId());
            action.setNodeId(node.getId());
            action.setNodeOrder(node.getNodeOrder());
            action.setNodeName(node.getNodeName());
            action.setApproverId(approverId);
            // TODO: 查询审批人姓名
            action.setStatus(0); // 待审批
            action.setCreateTime(new Date());
            approvalActionRepo.save(action);
        }

        // TODO: 发送审批通知（站内信、短信、推送等）
    }

    /**
     * 获取节点的审批人列表
     */
    private List<Long> getApproverIds(ApprovalNode node, ApprovalInstance instance) {
        return switch (node.getApproverType()) {
            case 1 -> // 指定用户
                JSONUtil.toList(node.getApproverIds(), Long.class);
            case 2 -> // 指定角色
                // TODO: 根据角色查询用户
                JSONUtil.toList(node.getApproverIds(), Long.class);
            case 3 -> // 部门主管
                // TODO: 查询申请人部门主管
                List.of();
            case 4 -> // 发起人自选
                // TODO: 从提交参数中获取
                List.of();
            default -> List.of();
        };
    }

    /**
     * 生成审批单号
     */
    private String generateInstanceNo() {
        return "AP" + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 发布状态变更事件
     */
    private void publishStatusChangeEvent(String bizType, Long bizId, Integer status) {
        ApprovalStatusChangeEvent event = new ApprovalStatusChangeEvent(this, bizType, bizId, status);
        eventPublisher.publishEvent(event);
    }

    // ==================== 查询方法 ====================

    /**
     * 根据业务获取审批实例
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 审批实例VO
     */
    public ApprovalInstanceVO getInstanceByBiz(String bizType, Long bizId) {
        ApprovalInstance instance = approvalInstanceRepo.getByBiz(bizType, bizId);
        return convertToInstanceVO(instance);
    }

    /**
     * 获取审批实例详情
     *
     * @param instanceId 实例ID
     * @return 审批实例VO
     */
    public ApprovalInstanceVO getInstanceDetail(Long instanceId) {
        ApprovalInstance instance = approvalInstanceRepo.getById(instanceId);
        return convertToInstanceVO(instance);
    }

    /**
     * 获取待办列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageVO<ApprovalTodoVO> pageTodoList(ApprovalQueryDTO query) {
        Page<ApprovalAction> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalAction> result = approvalActionRepo.pagePendingByApprover(query.getApproverId(), page);

        List<ApprovalTodoVO> voList = result.getRecords().stream()
            .map(this::convertToTodoVO)
            .toList();

        PageVO<ApprovalTodoVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    /**
     * 获取已办列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageVO<ApprovalTodoVO> pageDoneList(ApprovalQueryDTO query) {
        Page<ApprovalAction> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalAction> result = approvalActionRepo.pageHandledByApprover(query.getApproverId(), page);

        List<ApprovalTodoVO> voList = result.getRecords().stream()
            .map(this::convertToTodoVO)
            .toList();

        PageVO<ApprovalTodoVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    /**
     * 获取我发起的审批列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageVO<ApprovalInstanceVO> pageApplyList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByApplicant(
            query.getApplicantId(), query.getStatus(), page);

        List<ApprovalInstanceVO> voList = result.getRecords().stream()
            .map(this::convertToInstanceVO)
            .toList();

        PageVO<ApprovalInstanceVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    /**
     * 获取全部审批列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageVO<ApprovalInstanceVO> pageAllList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByCompany(
            query.getCompanyId(), query.getBizType(), query.getStatus(), page);

        List<ApprovalInstanceVO> voList = result.getRecords().stream()
            .map(this::convertToInstanceVO)
            .toList();

        PageVO<ApprovalInstanceVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    /**
     * 统计待办数量
     *
     * @param userId 用户ID
     * @return 待办数量
     */
    public long countTodo(Long userId) {
        return approvalActionRepo.countPendingByApprover(userId);
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为实例VO
     */
    private ApprovalInstanceVO convertToInstanceVO(ApprovalInstance instance) {
        if (instance == null) {
            return null;
        }

        ApprovalInstanceVO vo = new ApprovalInstanceVO();
        vo.setId(instance.getId());
        vo.setInstanceNo(instance.getInstanceNo());
        vo.setBizType(instance.getBizType());
        vo.setBizId(instance.getBizId());
        vo.setBizCode(instance.getBizCode());
        vo.setTitle(instance.getTitle());
        vo.setApplicantId(instance.getApplicantId());
        vo.setApplicantName(instance.getApplicantName());
        vo.setCurrentNodeOrder(instance.getCurrentNodeOrder());
        vo.setStatus(instance.getStatus());
        vo.setStatusName(Objects.requireNonNull(ApprovalStatusEnum.getByCode(instance.getStatus())).getName());
        vo.setResultRemark(instance.getResultRemark());
        vo.setCreateTime(instance.getCreateTime());
        vo.setFinishTime(instance.getFinishTime());

        // 业务类型名称
        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(instance.getBizType());
        if (bizTypeEnum != null) {
            vo.setBizTypeName(bizTypeEnum.getName());
        }

        // 当前节点名称
        if (instance.getCurrentNodeId() != null) {
            ApprovalNode currentNode = approvalNodeRepo.getById(instance.getCurrentNodeId());
            if (currentNode != null) {
                vo.setCurrentNodeName(currentNode.getNodeName());
            }
        }

        // 审批动作列表
        List<ApprovalAction> actions = approvalActionRepo.listByInstanceId(instance.getId());
        vo.setActions(actions.stream().map(this::convertToActionVO).toList());

        return vo;
    }

    /**
     * 转换为动作VO
     */
    private ApprovalActionVO convertToActionVO(ApprovalAction action) {
        ApprovalActionVO vo = new ApprovalActionVO();
        vo.setId(action.getId());
        vo.setNodeName(action.getNodeName());
        vo.setNodeOrder(action.getNodeOrder());
        vo.setApproverId(action.getApproverId());
        vo.setApproverName(action.getApproverName());
        vo.setAction(action.getAction());
        vo.setActionName(getActionName(action.getAction()));
        vo.setRemark(action.getRemark());
        vo.setOperateTime(action.getOperateTime());
        vo.setStatus(action.getStatus());
        vo.setStatusName(getActionStatusName(action.getStatus()));
        return vo;
    }

    /**
     * 转换为待办VO
     */
    private ApprovalTodoVO convertToTodoVO(ApprovalAction action) {
        ApprovalTodoVO vo = new ApprovalTodoVO();
        vo.setActionId(action.getId());
        vo.setInstanceId(action.getInstanceId());
        vo.setNodeName(action.getNodeName());
        vo.setNodeOrder(action.getNodeOrder());
        vo.setAction(action.getAction());
        vo.setActionName(getActionName(action.getAction()));
        vo.setRemark(action.getRemark());
        vo.setOperateTime(action.getOperateTime());

        // 获取实例信息
        ApprovalInstance instance = approvalInstanceRepo.getById(action.getInstanceId());
        if (instance != null) {
            vo.setInstanceNo(instance.getInstanceNo());
            vo.setBizType(instance.getBizType());
            vo.setBizId(instance.getBizId());
            vo.setBizCode(instance.getBizCode());
            vo.setTitle(instance.getTitle());
            vo.setApplicantName(instance.getApplicantName());
            vo.setApplyTime(instance.getCreateTime());
            vo.setInstanceStatus(instance.getStatus());
            vo.setInstanceStatusName(Objects.requireNonNull(ApprovalStatusEnum.getByCode(instance.getStatus())).getName());

            ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(instance.getBizType());
            if (bizTypeEnum != null) {
                vo.setBizTypeName(bizTypeEnum.getName());
            }
        }

        return vo;
    }

    /**
     * 获取操作名称
     */
    private String getActionName(Integer action) {
        if (action == null) return null;
        return switch (action) {
            case 1 -> "通过";
            case 2 -> "驳回";
            case 3 -> "转交";
            default -> "未知";
        };
    }

    /**
     * 获取动作状态名称
     */
    private String getActionStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已审批";
            case 2 -> "已跳过";
            default -> "未知";
        };
    }
}
