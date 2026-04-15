package com.homi.service.trial.application;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.TrialApplicationStatusEnum;
import com.homi.common.lib.exception.BizException;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Region;
import com.homi.model.dao.entity.TrialApplication;
import com.homi.model.dao.repo.RegionRepo;
import com.homi.model.dao.repo.TrialApplicationRepo;
import com.homi.model.trial.application.dto.TrialApplicationCreateDTO;
import com.homi.model.trial.application.dto.TrialApplicationHandleDTO;
import com.homi.model.trial.application.dto.TrialApplicationQueryDTO;
import com.homi.model.trial.application.vo.TrialApplicationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrialApplicationService {

    private final TrialApplicationRepo trialApplicationRepo;
    private final RegionRepo regionRepo;

    public Long create(TrialApplicationCreateDTO dto) {
        LambdaQueryWrapper<TrialApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrialApplication::getPhone, dto.getPhone());
        queryWrapper.eq(TrialApplication::getStatus, TrialApplicationStatusEnum.PENDING.getCode());
        if (trialApplicationRepo.exists(queryWrapper)) {
            throw new BizException("当前手机号已有待处理试用申请");
        }

        Region region = regionRepo.getById(dto.getRegionId());
        if (Objects.isNull(region)) {
            throw new BizException("城市信息不存在");
        }

        TrialApplication trialApplication = new TrialApplication();
        trialApplication.setPhone(dto.getPhone());
        trialApplication.setRegionId(dto.getRegionId());
        trialApplication.setCityName(region.getName());
        trialApplication.setUsageRemark(dto.getUsageRemark());
        trialApplication.setStatus(TrialApplicationStatusEnum.PENDING.getCode());
        trialApplication.setCreateAt(DateUtil.date());
        trialApplication.setUpdateAt(DateUtil.date());
        trialApplicationRepo.save(trialApplication);
        return trialApplication.getId();
    }

    public PageVO<TrialApplicationVO> getList(TrialApplicationQueryDTO dto) {
        Page<TrialApplication> page = new Page<>(dto.getCurrentPage(), dto.getPageSize());
        LambdaQueryWrapper<TrialApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CharSequenceUtil.isNotBlank(dto.getPhone()), TrialApplication::getPhone, dto.getPhone());
        queryWrapper.like(CharSequenceUtil.isNotBlank(dto.getCityName()), TrialApplication::getCityName, dto.getCityName());
        queryWrapper.eq(Objects.nonNull(dto.getStatus()), TrialApplication::getStatus, dto.getStatus());
        queryWrapper.orderByDesc(TrialApplication::getCreateAt);
        var result = trialApplicationRepo.page(page, queryWrapper);

        PageVO<TrialApplicationVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(result.getCurrent());
        pageVO.setPageSize(result.getSize());
        pageVO.setTotal(result.getTotal());
        pageVO.setPages(result.getPages());
        pageVO.setList(result.getRecords().stream().map(this::toVO).toList());
        return pageVO;
    }

    public Boolean handle(TrialApplicationHandleDTO dto, Long operatorId) {
        if (!Objects.equals(dto.getStatus(), TrialApplicationStatusEnum.APPROVED.getCode())
            && !Objects.equals(dto.getStatus(), TrialApplicationStatusEnum.REJECTED.getCode())) {
            throw new BizException("处理状态不合法");
        }

        TrialApplication trialApplication = trialApplicationRepo.getById(dto.getId());
        if (Objects.isNull(trialApplication)) {
            throw new BizException("试用申请不存在");
        }

        trialApplication.setStatus(dto.getStatus());
        trialApplication.setHandleRemark(dto.getHandleRemark());
        trialApplication.setUpdateBy(operatorId);
        trialApplication.setUpdateAt(DateUtil.date());
        return trialApplicationRepo.updateById(trialApplication);
    }

    private TrialApplicationVO toVO(TrialApplication trialApplication) {
        TrialApplicationVO vo = new TrialApplicationVO();
        vo.setId(trialApplication.getId());
        vo.setPhone(trialApplication.getPhone());
        vo.setRegionId(trialApplication.getRegionId());
        vo.setCityName(trialApplication.getCityName());
        vo.setUsageRemark(trialApplication.getUsageRemark());
        vo.setStatus(trialApplication.getStatus());
        vo.setHandleRemark(trialApplication.getHandleRemark());
        vo.setCreateAt(trialApplication.getCreateAt());
        vo.setUpdateAt(trialApplication.getUpdateAt());
        return vo;
    }
}
