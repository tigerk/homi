package com.homi.saas.web.controller.approval;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.approval.dto.*;
import com.homi.model.approval.vo.ApprovalFlowVO;
import com.homi.model.approval.vo.ApprovalInstanceVO;
import com.homi.model.approval.vo.ApprovalTodoVO;
import com.homi.model.common.vo.CodeNameVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.approval.ApprovalFlowService;
import com.homi.service.service.approval.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审批管理 Controller
 */
@Tag(name = "审批管理")
@RestController
@RequestMapping("/saas/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalFlowService approvalFlowService;

    // ==================== 审批流程配置 ====================

    @Operation(summary = "获取审批流程列表")
    @PostMapping("/flow/list")
    public ResponseResult<List<ApprovalFlowVO>> getFlowList(@RequestBody ApprovalFlowQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        query.setCompanyId(loginUser.getCurCompanyId());
        List<ApprovalFlowVO> list = approvalFlowService.getFlowList(query);
        return ResponseResult.ok(list);
    }

    @Operation(summary = "获取审批流程详情")
    @PostMapping("/flow/detail")
    public ResponseResult<ApprovalFlowVO> getFlowDetail(@RequestBody ApprovalQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        ApprovalFlowVO vo = approvalFlowService.getFlowDetail(query.getFlowId());
        return ResponseResult.ok(vo);
    }

    @Operation(summary = "保存审批流程（新增/修改）")
    @PostMapping("/flow/save")
    public ResponseResult<Long> saveFlow(@AuthenticationPrincipal UserLoginVO loginUser, @Valid @RequestBody ApprovalFlowDTO dto) {
        dto.setCompanyId(loginUser.getCurCompanyId());
        dto.setCreateBy(loginUser.getId());
        Long flowId = approvalFlowService.saveFlow(dto);
        return ResponseResult.ok(flowId);
    }

    @Operation(summary = "删除审批流程")
    @PostMapping("/flow/delete")
    public ResponseResult<Boolean> deleteFlow(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        return ResponseResult.ok(approvalFlowService.deleteFlow(query.getFlowId()));
    }

    @Operation(summary = "启用/停用审批流程")
    @PostMapping("/flow/toggle")
    public ResponseResult<Boolean> toggleFlowStatus(@RequestBody ApprovalQueryDTO query, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(approvalFlowService.toggleFlowStatus(query.getFlowId()));
    }

    @Operation(summary = "获取业务类型选项")
    @PostMapping("/flow/biz-types")
    public ResponseResult<List<CodeNameVO>> getBizTypeOptions(@AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(approvalFlowService.getBizTypeOptions());
    }

    // ==================== 审批操作 ====================

    @Operation(summary = "检查业务是否需要审批")
    @PostMapping("/check")
    public ResponseResult<Boolean> checkNeedApproval(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        Long companyId = loginUser.getCurCompanyId();
        boolean need = approvalService.needApproval(companyId, query.getBizType());
        return ResponseResult.ok(need);
    }

    @Operation(summary = "提交审批")
    @PostMapping("/submit")
    public ResponseResult<Long> submitApproval(@AuthenticationPrincipal UserLoginVO loginUser, @Valid @RequestBody ApprovalSubmitDTO dto) {
        dto.setCompanyId(loginUser.getCurCompanyId());
        dto.setApplicantId(loginUser.getId());
        Long instanceId = approvalService.submitApproval(dto);
        return ResponseResult.ok(instanceId);
    }

    @Operation(summary = "处理审批（通过/驳回）")
    @PostMapping("/handle")
    public ResponseResult<Boolean> handleApproval(@AuthenticationPrincipal UserLoginVO loginUser, @Valid @RequestBody ApprovalHandleDTO dto) {
        dto.setApproverId(loginUser.getId());
        approvalService.handleApproval(dto);
        return ResponseResult.ok(true);
    }

    @Operation(summary = "撤回审批")
    @PostMapping("/withdraw")
    public ResponseResult<Boolean> withdrawApproval(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        approvalService.withdrawApproval(query.getInstanceId(), loginUser.getId());
        return ResponseResult.ok(true);
    }

    // ==================== 审批查询 ====================

    @Operation(summary = "获取业务的审批实例")
    @PostMapping("/instance/biz")
    public ResponseResult<ApprovalInstanceVO> getApprovalInstance(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        ApprovalInstanceVO vo = approvalService.getInstanceByBiz(query.getBizType(), query.getBizId());
        return ResponseResult.ok(vo);
    }

    @Operation(summary = "获取审批实例详情")
    @PostMapping("/instance/detail")
    public ResponseResult<ApprovalInstanceVO> getInstanceDetail(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        ApprovalInstanceVO vo = approvalService.getInstanceDetail(query.getInstanceId());
        return ResponseResult.ok(vo);
    }

    @Operation(summary = "我的待办列表")
    @PostMapping("/todo/list")
    public ResponseResult<PageVO<ApprovalTodoVO>> getTodoList(@AuthenticationPrincipal UserLoginVO loginUser,
                                                              @RequestBody ApprovalQueryDTO query) {
        query.setApproverId(loginUser.getId());
        PageVO<ApprovalTodoVO> page = approvalService.pageTodoList(query);
        return ResponseResult.ok(page);
    }

    @Operation(summary = "我的待办数量")
    @PostMapping("/todo/count")
    public ResponseResult<Long> getTodoCount(@AuthenticationPrincipal UserLoginVO loginUser) {
        long count = approvalService.countTodo(loginUser.getId());
        return ResponseResult.ok(count);
    }

    @Operation(summary = "我的已办列表")
    @PostMapping("/done/list")
    public ResponseResult<PageVO<ApprovalTodoVO>> getDoneList(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        query.setApproverId(loginUser.getId());
        PageVO<ApprovalTodoVO> page = approvalService.pageDoneList(query);
        return ResponseResult.ok(page);
    }

    @Operation(summary = "我发起的审批列表")
    @PostMapping("/apply/list")
    public ResponseResult<PageVO<ApprovalInstanceVO>> getApplyList(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        query.setApplicantId(loginUser.getId());
        PageVO<ApprovalInstanceVO> page = approvalService.pageApplyList(query);
        return ResponseResult.ok(page);
    }

    @Operation(summary = "全部审批列表（管理员）")
    @PostMapping("/all/list")
    public ResponseResult<PageVO<ApprovalInstanceVO>> getAllList(@AuthenticationPrincipal UserLoginVO loginUser, @RequestBody ApprovalQueryDTO query) {
        query.setCompanyId(loginUser.getCurCompanyId());
        PageVO<ApprovalInstanceVO> page = approvalService.pageAllList(query);
        return ResponseResult.ok(page);
    }
}
