package com.homi.model.dao.repo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.ApprovalNode;
import com.homi.model.dao.mapper.ApprovalNodeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批节点 Repo
 */
@Repository
public class ApprovalNodeRepo extends ServiceImpl<ApprovalNodeMapper, ApprovalNode> {

    /**
     * 根据流程ID获取所有节点（按顺序排列）
     *
     * @param flowId 流程ID
     * @return 节点列表
     */
    public List<ApprovalNode> getNodesByFlowId(Long flowId) {
        return lambdaQuery()
            .eq(ApprovalNode::getFlowId, flowId)
            .eq(ApprovalNode::getDeleted, false)
            .orderByAsc(ApprovalNode::getNodeOrder)
            .list();
    }

    /**
     * 获取流程的第一个节点
     *
     * @param flowId 流程ID
     * @return 第一个节点
     */
    public ApprovalNode getFirstNode(Long flowId) {
        return lambdaQuery()
            .eq(ApprovalNode::getFlowId, flowId)
            .eq(ApprovalNode::getDeleted, false)
            .orderByAsc(ApprovalNode::getNodeOrder)
            .last("LIMIT 1")
            .one();
    }

    /**
     * 获取指定顺序的节点
     *
     * @param flowId    流程ID
     * @param nodeOrder 节点顺序
     * @return 节点
     */
    public ApprovalNode getByFlowIdAndOrder(Long flowId, Integer nodeOrder) {
        return lambdaQuery()
            .eq(ApprovalNode::getFlowId, flowId)
            .eq(ApprovalNode::getNodeOrder, nodeOrder)
            .eq(ApprovalNode::getDeleted, false)
            .one();
    }

    /**
     * 删除流程的所有节点
     *
     * @param flowId 流程ID
     * @return 是否成功
     */
    public boolean deleteByFlowId(Long flowId) {
        return lambdaUpdate()
            .eq(ApprovalNode::getFlowId, flowId)
            .set(ApprovalNode::getDeleted, true)
            .update();
    }

    /**
     * 物理删除流程的所有节点（用于重新保存）
     *
     * @param flowId 流程ID
     * @return 是否成功
     */
    public boolean removeByFlowId(Long flowId) {
        return lambdaUpdate()
            .eq(ApprovalNode::getFlowId, flowId)
            .remove();
    }

    /**
     * 获取流程节点数量
     *
     * @param flowId 流程ID
     * @return 节点数量
     */
    public long countByFlowId(Long flowId) {
        return lambdaQuery()
            .eq(ApprovalNode::getFlowId, flowId)
            .eq(ApprovalNode::getDeleted, false)
            .count();
    }
}
