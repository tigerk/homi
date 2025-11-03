package com.homi.model.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.domain.dto.house.focus.FocusCreateDTO;
import com.homi.model.entity.Focus;
import com.homi.model.mapper.FocusMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-07-22
 */
@Service
public class FocusRepo extends ServiceImpl<FocusMapper, Focus> {
    /**
     * 是否有项目编号
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 21:56
     *
     * @param id        项目ID
     * @param focusCode 参数说明
     * @return boolean
     */
    public boolean checkFocusCodeExist(Long id, String focusCode) {
        LambdaQueryWrapper<Focus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Focus::getFocusCode, focusCode);
        queryWrapper.ne(Focus::getId, id);
        return count(queryWrapper) > 0;
    }

    /**
     * 创建集中式项目
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 22:30
     *
     * @param focusCreateDto 参数说明
     * @return com.homi.model.entity.Focus
     */
    public Focus saveFocus(FocusCreateDTO focusCreateDto) {

        Focus focus = new Focus();
        BeanUtils.copyProperties(focusCreateDto, focus);
        focus.setCommunityId(focusCreateDto.getCommunity().getCommunityId());

        focus.setFacilities(JSONUtil.toJsonStr(focusCreateDto.getFacilities()));
        // 设置标签
        focus.setTags(JSONUtil.toJsonStr(focusCreateDto.getTags()));
        focus.setImageList(JSONUtil.toJsonStr(focusCreateDto.getImageList()));
        getBaseMapper().insert(focus);

        return focus;
    }
}
