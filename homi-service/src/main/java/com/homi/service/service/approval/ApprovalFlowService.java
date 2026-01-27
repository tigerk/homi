package com.homi.service.service.approval;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.approval.ApprovalBizTypeEnum;
import com.homi.model.approval.dto.ApprovalFlowDTO;
import com.homi.model.approval.dto.ApprovalFlowQueryDTO;
import com.homi.model.approval.dto.ApprovalNodeDTO;
import com.homi.model.approval.vo.ApprovalFlowVO;
import com.homi.model.approval.vo.ApprovalNodeVO;
import com.homi.model.common.vo.CodeNameVO;
import com.homi.model.dao.entity.ApprovalFlow;
import com.homi.model.dao.entity.ApprovalNode;
import com.homi.model.dao.repo.ApprovalFlowRepo;
import com.homi.model.dao.repo.ApprovalNodeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 审批流程配置服务
 */
@Service
@RequiredArgsConstructor
public class ApprovalFlowService {

    private final ApprovalFlowRepo approvalFlowRepo;
    private final ApprovalNodeRepo approvalNodeRepo;

    /**
     * 获取公司的审批流程列表
     *
     * @param query 查询参数
     * @return 流程列表
     */
    public List<ApprovalFlowVO> getFlowList(ApprovalFlowQueryDTO query) {
        List<ApprovalFlow> flows = approvalFlowRepo.listFlowByQuery(query);
        if (CollUtil.isEmpty(flows)) {
            return Collections.emptyList();
        }

        // 批量查询所有流程的节点
        List<Long> flowIds = flows.stream().map(ApprovalFlow::getId).toList();
        Map<Long, List<ApprovalNode>> nodeMap = getNodeMapByFlowIds(flowIds);

        return flows.stream().map(flow -> {
            ApprovalFlowVO vo = convertToVO(flow);
            vo.setNodes(convertNodeListToVO(nodeMap.get(flow.getId())));
            return vo;
        }).toList();
    }

    /**
     * 获取审批流程详情
     *
     * @param flowId 流程ID
     * @return 流程详情
     */
    public ApprovalFlowVO getFlowDetail(Long flowId) {
        ApprovalFlow flow = approvalFlowRepo.getById(flowId);
        if (flow == null) {
            return null;
        }

        ApprovalFlowVO vo = convertToVO(flow);
        List<ApprovalNode> nodes = approvalNodeRepo.getNodesByFlowId(flowId);
        vo.setNodes(convertNodeListToVO(nodes));
        return vo;
    }

    /**
     * 保存审批流程（新增/修改）
     *
     * @param dto 流程数据
     * @return 流程ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveFlow(ApprovalFlowDTO dto) {
        // 1. 检查业务类型是否已存在
        if (approvalFlowRepo.existsByBizType(dto.getCompanyId(), dto.getBizType(), dto.getId())) {
            throw new IllegalArgumentException("该业务类型已配置审批流程");
        }

        // 2. 保存/更新流程主表
        ApprovalFlow flow;
        if (dto.getId() != null) {
            // 修改
            flow = approvalFlowRepo.getById(dto.getId());
            if (flow == null) {
                throw new IllegalArgumentException("流程不存在");
            }
            flow.setFlowName(dto.getFlowName());
            flow.setEnabled(dto.getEnabled());
            flow.setRemark(dto.getRemark());
            flow.setUpdateBy(dto.getCreateBy());
            flow.setUpdateTime(new Date());
            approvalFlowRepo.updateById(flow);

            // 删除旧节点
            approvalNodeRepo.deletePhysicalByFlowId(flow.getId());
        } else {
            // 新增
            flow = new ApprovalFlow();
            flow.setCompanyId(dto.getCompanyId());
            flow.setFlowCode(generateFlowCode());
            flow.setFlowName(dto.getFlowName());
            flow.setBizType(dto.getBizType());
            flow.setEnabled(dto.getEnabled());
            flow.setRemark(dto.getRemark());
            flow.setDeleted(false);
            flow.setCreateBy(dto.getCreateBy());
            flow.setCreateTime(new Date());
            approvalFlowRepo.save(flow);
        }

        // 3. 保存节点
        if (CollUtil.isNotEmpty(dto.getNodes())) {
            List<ApprovalNode> nodes = new ArrayList<>();
            for (int i = 0; i < dto.getNodes().size(); i++) {
                ApprovalNodeDTO nodeDto = dto.getNodes().get(i);
                ApprovalNode node = new ApprovalNode();
                node.setFlowId(flow.getId());
                node.setNodeName(nodeDto.getNodeName());
                node.setNodeOrder(i + 1);
                node.setApproverType(nodeDto.getApproverType());
                node.setApproverIds(JSONUtil.toJsonStr(nodeDto.getApproverIds()));
                node.setMultiApproveType(nodeDto.getMultiApproveType() != null ? nodeDto.getMultiApproveType() : 1);
                node.setCreateTime(new Date());
                nodes.add(node);
            }
            approvalNodeRepo.saveBatch(nodes);
        }

        return flow.getId();
    }

    /**
     * 删除审批流程
     *
     * @param flowId 流程ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long flowId) {
        // 删除流程
        approvalFlowRepo.removeById(flowId);
        approvalNodeRepo.deleteByFlowId(flowId);
    }

    /**
     * 启用/停用审批流程
     *
     * @param flowId 流程ID
     */
    public void toggleFlowStatus(Long flowId) {
        ApprovalFlow flow = approvalFlowRepo.getById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("流程不存在");
        }
        approvalFlowRepo.updateEnabled(flowId, !flow.getEnabled());
    }

    /**
     * 获取业务类型选项
     *
     * @return 业务类型列表
     */
    public List<CodeNameVO> getBizTypeOptions() {
        List<CodeNameVO> options = new ArrayList<>();
        for (ApprovalBizTypeEnum bizType : ApprovalBizTypeEnum.values()) {
            CodeNameVO labelValue = CodeNameVO.builder()
                .code(bizType.getCode())
                .name(bizType.getName())
                .build();
            options.add(labelValue);
        }
        return options;
    }

    // ==================== 私有方法 ====================

    /**
     * 生成流程编码
     */
    private String generateFlowCode() {
        return "FLOW" + IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 批量查询节点并按流程ID分组
     */
    private Map<Long, List<ApprovalNode>> getNodeMapByFlowIds(List<Long> flowIds) {
        if (CollUtil.isEmpty(flowIds)) {
            return Collections.emptyMap();
        }
        // 简单实现：逐个查询（实际可优化为批量查询）
        Map<Long, List<ApprovalNode>> map = new HashMap<>();
        for (Long flowId : flowIds) {
            map.put(flowId, approvalNodeRepo.getNodesByFlowId(flowId));
        }
        return map;
    }

    /**
     * 转换为 VO
     */
    private ApprovalFlowVO convertToVO(ApprovalFlow flow) {
        ApprovalFlowVO vo = new ApprovalFlowVO();
        BeanUtil.copyProperties(flow, vo);

        // 业务类型名称
        ApprovalBizTypeEnum bizTypeEnum = ApprovalBizTypeEnum.getByCode(flow.getBizType());
        if (bizTypeEnum != null) {
            vo.setBizTypeName(bizTypeEnum.getName());
        }
        return vo;
    }

    /**
     * 转换节点列表为 VO
     */
    private List<ApprovalNodeVO> convertNodeListToVO(List<ApprovalNode> nodes) {
        if (CollUtil.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream().map(this::convertNodeToVO).toList();
    }

    /**
     * 转换节点为 VO
     */
    private ApprovalNodeVO convertNodeToVO(ApprovalNode node) {
        ApprovalNodeVO vo = new ApprovalNodeVO();
        vo.setId(node.getId());
        vo.setNodeName(node.getNodeName());
        vo.setNodeOrder(node.getNodeOrder());
        vo.setApproverType(node.getApproverType());
        vo.setMultiApproveType(node.getMultiApproveType());

        // 审批人类型名称
        vo.setApproverTypeName(getApproverTypeName(node.getApproverType()));

        // 多人审批方式名称
        vo.setMultiApproveTypeName(node.getMultiApproveType() == 1 ? "或签" : "会签");

        // 审批人ID列表
        if (node.getApproverIds() != null) {
            vo.setApproverIds(JSONUtil.toList(node.getApproverIds(), Long.class));
        }

        // TODO: 查询审批人名称列表（根据 approverType 查用户或角色）

        return vo;
    }

    /**
     * 获取审批人类型名称
     */
    private String getApproverTypeName(Integer approverType) {
        return switch (approverType) {
            case 1 -> "指定用户";
            case 2 -> "指定角色";
            case 3 -> "部门主管";
            case 4 -> "发起人自选";
            default -> "未知";
        };
    }
}
