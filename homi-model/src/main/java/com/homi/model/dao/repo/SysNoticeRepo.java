package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysNotice;
import com.homi.model.dao.mapper.SysNoticeMapper;
import com.homi.model.notice.dto.SysNoticePageDTO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 系统公告表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysNoticeRepo extends ServiceImpl<SysNoticeMapper, SysNotice> {

    public PageVO<SysNotice> getNoticePage(SysNoticePageDTO dto, Long companyId) {
        Page<SysNotice> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getCompanyId, companyId).eq(SysNotice::getStatus, 1);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysNotice::getTitle, dto.getKeyword())
                .or()
                .like(SysNotice::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysNotice::getPublishTime);
        IPage<SysNotice> pageResult = getBaseMapper().selectPage(page, wrapper);

        PageVO<SysNotice> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(pageResult.getRecords());
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());

        return pageVO;
    }

    public List<SysNotice> getRecentNotices(Long companyId, Date startTime) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getCompanyId, companyId)
            .eq(SysNotice::getStatus, 1)
            .ge(SysNotice::getPublishTime, startTime)
            .orderByDesc(SysNotice::getPublishTime);
        return getBaseMapper().selectList(wrapper);
    }
}
