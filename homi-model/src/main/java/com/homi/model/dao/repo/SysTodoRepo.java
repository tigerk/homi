package com.homi.model.dao.repo;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.SysTodo;
import com.homi.model.dao.mapper.SysTodoMapper;
import com.homi.model.notice.dto.SysNoticePageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 待办任务表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2026-02-05
 */
@Service
public class SysTodoRepo extends ServiceImpl<SysTodoMapper, SysTodo> {

    public PageVO<SysTodo> getTodoPage(SysNoticePageDTO dto, Long companyId, Long userId) {
        Page<SysTodo> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getCompanyId, companyId).eq(SysTodo::getUserId, userId);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysTodo::getTitle, dto.getKeyword())
                .or()
                .like(SysTodo::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysTodo::getCreateTime);
        IPage<SysTodo> pageResult = getBaseMapper().selectPage(page, wrapper);

        PageVO<SysTodo> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(pageResult.getRecords());
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());

        return pageVO;
    }

    public PageVO<SysTodo> getTodoPageForAdmin(SysNoticePageDTO dto, Long companyId) {
        Page<SysTodo> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getCompanyId, companyId);

        if (CharSequenceUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysTodo::getTitle, dto.getKeyword())
                .or()
                .like(SysTodo::getContent, dto.getKeyword()));
        }

        wrapper.orderByDesc(SysTodo::getCreateTime);
        IPage<SysTodo> pageResult = getBaseMapper().selectPage(page, wrapper);

        PageVO<SysTodo> pageVO = new PageVO<>();
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setList(pageResult.getRecords());
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setPages(pageResult.getPages());

        return pageVO;
    }

    public List<SysTodo> getRecentTodos(Long companyId, Long userId, int limit) {
        Page<SysTodo> page = new Page<>(1, limit);

        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getCompanyId, companyId)
            .eq(SysTodo::getUserId, userId)
            .orderByDesc(SysTodo::getCreateTime);
        return page(page, wrapper).getRecords();
    }

    public Long countPendingTodos(Long companyId, Long userId) {
        LambdaQueryWrapper<SysTodo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTodo::getCompanyId, companyId)
            .eq(SysTodo::getUserId, userId)
            .eq(SysTodo::getStatus, 0)
            .eq(SysTodo::getDeleted, false);
        return count(wrapper);
    }
}
