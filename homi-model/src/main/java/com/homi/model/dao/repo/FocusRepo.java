package com.homi.model.dao.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.model.dao.entity.Focus;
import com.homi.model.dao.mapper.FocusMapper;
import com.homi.model.focus.dto.FocusCreateDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
        Focus toSave = new Focus();
        BeanUtils.copyProperties(focusCreateDto, toSave);
        toSave.setCommunityId(focusCreateDto.getCommunity().getCommunityId());

        toSave.setFacilities(JSONUtil.toJsonStr(focusCreateDto.getFacilities()));
        // 设置标签
        toSave.setTags(JSONUtil.toJsonStr(focusCreateDto.getTags()));
        toSave.setImageList(JSONUtil.toJsonStr(focusCreateDto.getImageList()));

        if (Objects.nonNull(focusCreateDto.getId())) {
            Focus focus = getById(focusCreateDto.getId());
            BeanUtils.copyProperties(toSave, focus);
            updateById(focus);

            return focus;
        } else {
            save(toSave);
            return toSave;
        }
    }
}
