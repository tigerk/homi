package com.homi.service.service.approval.provider;

import com.homi.model.approval.vo.ApprovalInstanceVO;
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
    void fillTodoBizDetail(ApprovalTodoVO todoVO, Long bizId);

    void fillInstanceBizDetail(ApprovalInstanceVO instanceVO, Long bizId);
}
