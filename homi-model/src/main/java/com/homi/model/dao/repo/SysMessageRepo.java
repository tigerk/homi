package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysMessage;
import com.homi.model.dao.mapper.SysMessageMapper;
import com.homi.model.notice.dto.SysNoticePageDTO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 站内信/个人消息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysMessageRepo extends ServiceImpl<SysMessageMapper, SysMessage> {

    public PageVO<SysMessage> getMessagePage(SysNoticePageDTO dto, Long companyId, Long userId) {
        Page<SysMessage> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getCompanyId, companyId)
            .eq(SysMessage::getReceiverId, userId)
            .eq(SysMessage::getDeletedByReceiver, false);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysMessage::getTitle, dto.getKeyword())
                .or()
                .like(SysMessage::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysMessage::getCreateTime);
        IPage<SysMessage> pageResult = getBaseMapper().selectPage(page, wrapper);

        PageVO<SysMessage> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(pageResult.getRecords());
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());

        return pageVO;
    }

    public List<SysMessage> getRecentMessages(Long companyId, Long userId, Date startTime) {
        Page<SysMessage> page = new Page<>(1, 10); // 第1页，每页10条

        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getCompanyId, companyId)
            .eq(SysMessage::getReceiverId, userId)
            .eq(SysMessage::getDeletedByReceiver, false)
            .orderByDesc(SysMessage::getCreateTime);

        return page(page, wrapper).getRecords();
    }
}
