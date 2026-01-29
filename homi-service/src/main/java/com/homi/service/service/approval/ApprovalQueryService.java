package com.homi.service.service.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.approval.ApprovalActionStatusEnum;
import com.homi.common.lib.enums.approval.ApprovalActionTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.common.lib.enums.approval.ApprovalStatusEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.ApprovalQueryDTO;
import com.homi.model.approval.vo.ApprovalActionVO;
import com.homi.model.approval.vo.ApprovalInstanceVO;
import com.homi.model.approval.vo.ApprovalTodoVO;
import com.homi.model.dao.entity.ApprovalAction;
import com.homi.model.dao.entity.ApprovalInstance;
import com.homi.model.dao.repo.ApprovalActionRepo;
import com.homi.model.dao.repo.ApprovalInstanceRepo;
import com.homi.model.dao.repo.ApprovalNodeRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.service.service.approval.provider.ApprovalBizDetailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 审批查询服务（负责读操作：查询实例、待办、已办等）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalQueryService {

    private final ApprovalNodeRepo approvalNodeRepo;
    private final ApprovalInstanceRepo approvalInstanceRepo;
    private final ApprovalActionRepo approvalActionRepo;
    private final UserRepo userRepo;
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
        return buildPageVO(query, result);
    }

    /**
     * 获取已办列表
     */
    public PageVO<ApprovalTodoVO> pageDoneList(ApprovalQueryDTO query) {
        Page<ApprovalAction> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalAction> result = approvalActionRepo.pageHandledByApprover(query.getApproverId(), page);
        return buildPageVO(query, result);
    }

    /**
     * 获取我发起的审批列表
     */
    public PageVO<ApprovalInstanceVO> pageApplyList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByApplicant(
            query.getApplicantId(),
            query.getStatus(),
            page
        );
        return buildInstancePageVO(query, result);
    }

    /**
     * 获取全部审批列表
     */
    public PageVO<ApprovalInstanceVO> pageAllList(ApprovalQueryDTO query) {
        Page<ApprovalInstance> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<ApprovalInstance> result = approvalInstanceRepo.pageByCompany(
            query.getCompanyId(),
            query.getBizType(),
            query.getStatus(),
            page
        );
        return buildInstancePageVO(query, result);
    }

    /**
     * 统计待办数量
     */
    public long countTodo(Long userId) {
        return approvalActionRepo.countPendingByApprover(userId);
    }

    // ==================== 私有转换方法 ====================

    /**
     * 构建待办分页VO
     */
    private PageVO<ApprovalTodoVO> buildPageVO(ApprovalQueryDTO query, Page<ApprovalAction> result) {
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
     * 构建实例分页VO
     */
    private PageVO<ApprovalInstanceVO> buildInstancePageVO(ApprovalQueryDTO query, Page<ApprovalInstance> result) {
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
        vo.setCurrentNodeOrder(instance.getCurrentNodeOrder());
        vo.setStatus(instance.getStatus());
        vo.setResultRemark(instance.getResultRemark());
        vo.setCreateTime(instance.getCreateTime());
        vo.setFinishTime(instance.getFinishTime());

        // 获取申请人姓名
        Optional.ofNullable(userRepo.getById(instance.getApplicantId()))
            .ifPresent(user -> vo.setApplicantName(user.getNickname()));

        // 使用枚举获取状态名称
        ApprovalStatusEnum approvalStatusEnum = ApprovalStatusEnum.getByCode(instance.getStatus());
        vo.setStatusName(Objects.nonNull(approvalStatusEnum) ? approvalStatusEnum.getName() : "未知");

        // 设置业务类型名称
        Optional.ofNullable(ApprovalBizTypeEnum.getByCode(instance.getBizType()))
            .ifPresent(bizType -> vo.setBizTypeName(bizType.getName()));

        // 设置当前节点名称
        if (instance.getCurrentNodeId() != null) {
            Optional.ofNullable(approvalNodeRepo.getById(instance.getCurrentNodeId()))
                .ifPresent(node -> vo.setCurrentNodeName(node.getNodeName()));
        }

        // 查询审批动作列表
        List<ApprovalAction> actions = approvalActionRepo.listByInstanceId(instance.getId());
        vo.setActions(actions.stream().map(this::convertToActionVO).toList());

        // 使用业务详情提供者填充审批实例业务详情
        fillInstanceBizDetailWithProvider(vo, instance.getBizType(), instance.getBizId());

        return vo;
    }

    /**
     * 转换为审批动作VO
     */
    private ApprovalActionVO convertToActionVO(ApprovalAction action) {
        ApprovalActionVO vo = new ApprovalActionVO();
        vo.setId(action.getId());
        vo.setNodeName(action.getNodeName());
        vo.setNodeOrder(action.getNodeOrder());
        vo.setApproverId(action.getApproverId());
        vo.setApproverName(action.getApproverName());
        vo.setAction(action.getAction());
        vo.setRemark(action.getRemark());
        vo.setOperateTime(action.getOperateTime());
        vo.setStatus(action.getStatus());

        // 使用枚举获取操作名称
        vo.setActionName(ApprovalActionTypeEnum.getNameByCode(action.getAction()));

        // 使用枚举获取状态名称
        ApprovalActionStatusEnum approvalActionStatusEnum = ApprovalActionStatusEnum.fromCode(action.getStatus());
        vo.setStatusName(Objects.nonNull(approvalActionStatusEnum) ? approvalActionStatusEnum.getName() : "未知");

        return vo;
    }

    /**
     * 将审批动作转换为待办VO
     */
    private ApprovalTodoVO convertToTodoVO(ApprovalAction action) {
        ApprovalTodoVO vo = new ApprovalTodoVO();
        vo.setActionId(action.getId());
        vo.setInstanceId(action.getInstanceId());
        vo.setNodeName(action.getNodeName());
        vo.setNodeOrder(action.getNodeOrder());
        vo.setAction(action.getAction());
        vo.setRemark(action.getRemark());
        vo.setOperateTime(action.getOperateTime());

        // 使用枚举获取操作名称
        vo.setActionName(ApprovalActionTypeEnum.getNameByCode(action.getAction()));

        // 获取实例信息
        ApprovalInstance instance = approvalInstanceRepo.getById(action.getInstanceId());
        if (instance == null) {
            log.warn("审批实例不存在: instanceId={}", action.getInstanceId());
            return vo;
        }

        vo.setInstanceNo(instance.getInstanceNo());
        vo.setBizType(instance.getBizType());
        vo.setBizId(instance.getBizId());
        vo.setBizCode(instance.getBizCode());
        vo.setTitle(instance.getTitle());
        vo.setApplyTime(instance.getCreateTime());
        vo.setInstanceStatus(instance.getStatus());

        // 使用枚举获取业务类型名称
        Optional.ofNullable(ApprovalBizTypeEnum.getByCode(instance.getBizType()))
            .ifPresent(bizType -> vo.setBizTypeName(bizType.getName()));

        // 使用枚举获取实例状态名称
        ApprovalStatusEnum approvalStatusEnum = ApprovalStatusEnum.getByCode(instance.getStatus());
        vo.setInstanceStatusName(Objects.nonNull(approvalStatusEnum) ? approvalStatusEnum.getName() : "未知");

        // 获取申请人姓名
        Optional.ofNullable(userRepo.getById(instance.getApplicantId()))
            .ifPresent(user -> vo.setApplicantName(user.getNickname()));

        // 使用业务详情提供者填充业务详情
        fillTodoBizDetailWithProvider(vo, instance.getBizType(), instance.getBizId());

        return vo;
    }

    /**
     * 使用提供者填充业务详情
     */
    private void fillTodoBizDetailWithProvider(ApprovalTodoVO vo, String bizType, Long bizId) {
        bizDetailProviders.stream()
            .filter(provider -> provider.getBizType().equals(bizType))
            .findFirst()
            .ifPresent(provider -> {
                try {
                    provider.fillTodoBizDetail(vo, bizId);
                } catch (Exception e) {
                    log.error("填充审批待办业务详情失败: bizType={}, bizId={}", bizType, bizId, e);
                }
            });
    }

    private void fillInstanceBizDetailWithProvider(ApprovalInstanceVO vo, String bizType, Long bizId) {
        bizDetailProviders.stream()
            .filter(provider -> provider.getBizType().equals(bizType))
            .findFirst()
            .ifPresent(provider -> {
                try {
                    provider.fillInstanceBizDetail(vo, bizId);
                } catch (Exception e) {
                    log.error("填充审批实例业务详情失败: bizType={}, bizId={}", bizType, bizId, e);
                }
            });
    }
}
