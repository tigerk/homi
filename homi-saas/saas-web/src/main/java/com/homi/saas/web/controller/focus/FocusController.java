package com.homi.saas.web.controller.focus;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.focus.dto.FocusQueryDTO;
import com.homi.model.focus.vo.FocusListVO;
import com.homi.model.vo.room.RoomListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.focus.dto.FocusCreateDTO;
import com.homi.model.vo.IdNameVO;
import com.homi.model.dao.repo.FileMetaRepo;
import com.homi.service.service.house.focus.FocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/focus")
public class FocusController {
    private final FocusService focusService;

    private final FileMetaRepo fileMetaRepo;

    @PostMapping("/create")
    public ResponseResult<Long> createHouse(@RequestBody FocusCreateDTO focusCreateDTO) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        focusCreateDTO.setCompanyId(currentUser.getCurCompanyId());

        focusCreateDTO.setCreateBy(currentUser.getId());
        focusCreateDTO.setCreateTime(DateUtil.date());
        focusCreateDTO.setUpdateBy(currentUser.getId());
        focusCreateDTO.setUpdateTime(DateUtil.date());

        Long focusId;
        if (Objects.nonNull(focusCreateDTO.getId())) {
            focusId = focusService.updateHouseFocus(focusCreateDTO);
        } else {
            focusCreateDTO.setCreateBy(currentUser.getId());
            focusCreateDTO.setCreateTime(DateUtil.date());
            focusId = focusService.createHouseFocus(focusCreateDTO);
        }

        // 设置上传文件为已使用
        fileMetaRepo.setFileUsedByName(focusCreateDTO.getImageList());

        return ResponseResult.ok(focusId);
    }

    @GetMapping("/get")
    public ResponseResult<FocusCreateDTO> getFocus(@RequestParam("id") Long focusId) {
        return ResponseResult.ok(focusService.getFocusById(focusId));
    }

    /**
     * 集中式项目选项
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:16
     *
     * @return com.homi.common.model.response.ResponseResult<java.util.List<com.homi.domain.vo.IdNameVO>>
     */
    @GetMapping("/options")
    public ResponseResult<List<IdNameVO>> focusOptions() {
        return ResponseResult.ok(focusService.getFocusOptionList());
    }

    @GetMapping("/code/check")
    public ResponseResult<Boolean> checkFocusCodeExist(@RequestParam("id") Long id, @RequestParam("focusCode") String focusCode) {
        return ResponseResult.ok(focusService.checkFocusCodeExist(id, CharSequenceUtil.trim(focusCode)));
    }

    /**
     * 获取集中式项目列表
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/9/10 23:16
     *
     * @param query 查询参数
     * @return com.homi.common.model.response.ResponseResult<com.homi.common.model.vo.PageVO<com.homi.domain.vo.RoomListVO>>
     */
    @PostMapping("/list")
    public ResponseResult<PageVO<FocusListVO>> getFocusList(@RequestBody FocusQueryDTO query) {
        return ResponseResult.ok(focusService.getFocusList(query));
    }
}

