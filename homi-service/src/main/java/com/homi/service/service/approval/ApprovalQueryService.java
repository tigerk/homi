package com.homi.service.service.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalQueryDTO;
import com.homi.model.approval.vo.ApprovalActionVO;
import com.homi.model.approval.vo.ApprovalInstanceVO;
import com.homi.model.approval.vo.ApprovalTodoVO;
import com.homi.model.dao.entity.ApprovalAction;
import com.homi.model.dao.entity.ApprovalInstance;
import com.homi.model.dao.entity.ApprovalNode;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.repo.ApprovalActionRepo;
import com.homi.model.dao.repo.ApprovalInstanceRepo;
import com.homi.model.dao.repo.ApprovalNodeRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.service.service.approval.provider.ApprovalBizDetailProvider;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 审批查询服务（负责读操作：查询实例、待办、已办等）
 */
@Service
@RequiredArgsConstructor
public class ApprovalQueryService {

    private final ApprovalNodeRepo approvalNodeRepo;
    private final ApprovalInstanceRepo approvalInstanceRepo;
    private final ApprovalActionRepo approvalActionRepo;
    private final UserRepo userRepo;

    // 注入业务详情提供者列表
    private final List<ApprovalBizDetailProvider> bizDetailProviders;

    /**
     * 根据业务获取审批实例
     */
    public ApprovalInstanceVO getInstanceByBiz(String bizType, Long bizId) {
        ApprovalInstance instance = approvalInstanceRepo.getByBiz(bizType, bizId);
        return convertToInstanceVO(instance);
    }

    /**
     * 获取审批实例详情
     */
    public ApprovalInstanceVO getInstanceDetail(Long instanceId) {
        ApprovalInstance instance = approvalInstanceRepo.getById(instanceId);
        return convertToInstanceVO(instance);
    }

    /**
     * 获取待办列表
     */
    public PageVO<ApprovalTodoVO> pageTodoList(ApprovalQueryDTO query) {
        Page<ApprovalAction> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalAction> result = approvalActionRepo.pagePendingByApprover(query.getApproverId(), page);

        return formatApprovalTodoVOPageVO(query, result);
    }

    /**
     * 获取已办列表
     */
    public PageVO<ApprovalTodoVO> pageDoneList(ApprovalQueryDTO query) {
        Page<ApprovalAction> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalAction> result = approvalActionRepo.pageHandledByApprover(query.getApproverId(), page);

        return formatApprovalTodoVOPageVO(query, result);
    }

    /**
     * 获取我发起的审批列表
     */
    public PageVO<ApprovalInstanceVO> pageApplyList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByApplicant(query.getApplicantId(), query.getStatus(), page);

        return formatApprovalInstancePageVO(query, result);
    }

    /**
     * 获取全部审批列表
     */
    public PageVO<ApprovalInstanceVO> pageAllList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByCompany(query.getCompanyId(), query.getBizType(), query.getStatus(), page);

        return formatApprovalInstancePageVO(query, result);
    }

    /**
     * 统计待办数量
     */
    public long countTodo(Long userId) {
        return approvalActionRepo.countPendingByApprover(userId);
    }

    // ==================== 私有转换方法 ====================

    @NotNull
    private PageVO<ApprovalTodoVO> formatApprovalTodoVOPageVO(ApprovalQueryDTO query, Page<ApprovalAction> result) {
        List<ApprovalTodoVO> voList = result.getRecords().stream().map(this::convertToTodoVO).toList();

        PageVO<ApprovalTodoVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

    @NotNull
    private PageVO<ApprovalInstanceVO> formatApprovalInstancePageVO(ApprovalQueryDTO query, Page<ApprovalInstance> result) {
        List<ApprovalInstanceVO> voList = result.getRecords().stream().map(this::convertToInstanceVO).toList();

        PageVO<ApprovalInstanceVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(query.getCurrentPage());
        pageVO.setPageSize(query.getPageSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(voList);
        return pageVO;
    }

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

        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(instance.getBizType());
        if (bizTypeEnum != null) {
            vo.setBizTypeName(bizTypeEnum.getName());
        }

        if (instance.getCurrentNodeId() != null) {
            ApprovalNode currentNode = approvalNodeRepo.getById(instance.getCurrentNodeId());
            if (currentNode != null) {
                vo.setCurrentNodeName(currentNode.getNodeName());
            }
        }

        List<ApprovalAction> actions = approvalActionRepo.listByInstanceId(instance.getId());
        vo.setActions(actions.stream().map(this::convertToActionVO).toList());

        return vo;
    }

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
     * 将审批动作转换为待办VO
     * 关键：使用业务详情提供者填充业务信息
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
        if (Objects.isNull(instance)) {
            return vo;
        }

        vo.setInstanceNo(instance.getInstanceNo());
        vo.setBizType(instance.getBizType());
        vo.setBizId(instance.getBizId());

        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(instance.getBizType());
        vo.setBizTypeName(Objects.requireNonNull(bizTypeEnum).getName());
        vo.setBizCode(instance.getBizCode());
        vo.setTitle(instance.getTitle());

        User applicant = userRepo.getById(instance.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getNickname());
        }

        vo.setApplyTime(instance.getCreateTime());
        vo.setInstanceStatus(instance.getStatus());
        vo.setInstanceStatusName(Objects.requireNonNull(ApprovalStatusEnum.getByCode(instance.getStatus())).getName());

        // 🔥 关键：使用业务详情提供者填充业务详情（解耦）
        fillBizDetailWithProvider(vo, instance.getBizType(), instance.getBizId());

        return vo;
    }

    /**
     * 使用提供者填充业务详情
     */
    private void fillBizDetailWithProvider(ApprovalTodoVO vo, String bizType, Long bizId) {
        bizDetailProviders.stream()
            .filter(provider -> provider.getBizType().equals(bizType))
            .findFirst()
            .ifPresent(provider -> provider.fillBizDetail(vo, bizId));
    }

    private String getActionName(Integer action) {
        if (action == null) return null;
        return switch (action) {
            case 1 -> "通过";
            case 2 -> "驳回";
            case 3 -> "转交";
            default -> "未知";
        };
    }

    private String getActionStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待审批";
            case 1 -> "已审批";
            case 2 -> "已跳过";
            default -> "未知";
        };
    }
}
