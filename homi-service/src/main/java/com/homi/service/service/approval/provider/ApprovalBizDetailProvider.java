package com.homi.service.service.approval.provider;

import com.homi.model.approval.vo.ApprovalTodoVO;

/**
 * 审批业务详情提供者
 */
public interface ApprovalBizDetailProvider {
    /**
     * 获取业务类型
     */
    String getBizType();

    /**
     * 填充业务详情到待办VO
     */
    void fillBizDetail(ApprovalTodoVO todoVO, Long bizId);
}
