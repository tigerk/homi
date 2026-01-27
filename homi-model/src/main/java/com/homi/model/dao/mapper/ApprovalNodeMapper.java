package com.homi.model.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homi.model.dao.entity.ApprovalNode;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 审批节点配置表 Mapper 接口
 * </p>
 *
 * @author tk
 * @since 2026-01-26
 */
@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    /**
     * 物理删除指定审批流的审批节点
     * <p>
     * {@code @author} tk
     * {@code @date} 2026/1/27 15:01
     *
     * @param flowId 参数说明
     * @return int
     */
    @Delete("DELETE FROM approval_node WHERE flow_id = #{flowId}")
    int deletePhysicalByFlowId(Long flowId);
}
