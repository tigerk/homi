package com.homi.service.service.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.Dept;
import com.homi.model.dao.entity.User;
import com.homi.model.dao.mapper.DeptMapper;
import com.homi.model.dao.repo.DeptRepo;
import com.homi.model.dao.repo.UserRepo;
import com.homi.model.dept.dto.DeptCreateDTO;
import com.homi.model.dept.dto.DeptQueryDTO;
import com.homi.model.dept.vo.DeptVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/28
 */

@Service
@RequiredArgsConstructor
public class DeptService {
    private final DeptMapper deptMapper;
    private final UserRepo userRepo;
    private final DeptRepo deptRepo;

    public List<DeptVO> list(DeptQueryDTO queryDTO) {
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(queryDTO.getCompanyId())) {
            queryWrapper.eq(Dept::getCompanyId, queryDTO.getCompanyId());
        }
        if (Objects.nonNull(queryDTO.getName())) {
            queryWrapper.like(Dept::getName, queryDTO.getName());
        }

        if (Objects.nonNull(queryDTO.getStatus())) {
            queryWrapper.like(Dept::getStatus, queryDTO.getStatus());
        }

        List<Dept> deptList = deptMapper.selectList(queryWrapper);

        return deptList.stream().map(dept -> {
            DeptVO deptVO = BeanCopyUtils.copyBean(dept, DeptVO.class);
            assert deptVO != null;
            if (Objects.nonNull(dept.getSupervisorId())) {
                User supervisor = userRepo.getById(dept.getSupervisorId());
                if (Objects.nonNull(supervisor)) {
                    deptVO.setSupervisorName(Objects.nonNull(supervisor.getNickname()) && !supervisor.getNickname().isBlank() ? supervisor.getNickname() : supervisor.getUsername());
                }
            }
            return deptVO;
        }).toList();
    }

    public Dept getDeptById(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    public Boolean createDept(DeptCreateDTO createDTO) {
        Dept dept = BeanCopyUtils.copyBean(createDTO, Dept.class);
        if (Objects.isNull(createDTO.getSortOrder()) && Objects.nonNull(createDTO.getSort())) {
            dept.setSortOrder(createDTO.getSort());
        }
        dept.setPrincipal(null);
        dept.setPhone(null);
        dept.setEmail(null);

        return deptMapper.insert(dept) > 0;
    }

    public Boolean updateDept(DeptCreateDTO createDTO) {
        Dept dept = BeanCopyUtils.copyBean(createDTO, Dept.class);
        if (Objects.isNull(createDTO.getSortOrder()) && Objects.nonNull(createDTO.getSort())) {
            dept.setSortOrder(createDTO.getSort());
        }

        return deptMapper.updateById(dept) > 0;
    }


    public Boolean deleteDept(Long id) {
        return deptMapper.deleteById(id) > 0;
    }

    public List<Dept> listByIds(List<Long> deptIdList) {
        return deptRepo.listByIds(deptIdList);
    }
}
