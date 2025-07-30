package com.homi.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.domain.dto.dept.DeptCreateDTO;
import com.homi.domain.dto.dept.DeptQueryDTO;
import com.homi.domain.dto.dept.DeptVO;
import com.homi.model.entity.Dept;
import com.homi.model.entity.DeptUser;
import com.homi.model.mapper.DeptMapper;
import com.homi.model.mapper.DeptUserMapper;
import com.homi.utils.BeanCopyUtils;
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
    private final DeptUserMapper deptUserMapper;

    private final DeptMapper deptMapper;


    public List<DeptVO> list(DeptQueryDTO queryDTO) {
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(queryDTO.getName())) {
            queryWrapper.like(Dept::getName, queryDTO.getName());
        }

        if (Objects.nonNull(queryDTO.getStatus())) {
            queryWrapper.like(Dept::getStatus, queryDTO.getStatus());
        }

        List<Dept> deptList = deptMapper.selectList(queryWrapper);

        return deptList.stream().map(dept -> BeanCopyUtils.copyBean(dept, DeptVO.class)).toList();
    }

    public Dept getDeptById(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    public Dept getUserDept(Long userId) {
        LambdaQueryWrapper<DeptUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeptUser::getUserId, userId);

        DeptUser deptUser = deptUserMapper.selectOne(queryWrapper);

        if (deptUser != null) {
            return deptMapper.selectById(deptUser.getDeptId());
        }

        return null;
    }

    public Boolean createDept(DeptCreateDTO createDTO) {
        Dept dept = BeanCopyUtils.copyBean(createDTO, Dept.class);

        return deptMapper.insert(dept) > 0;
    }

    public Boolean updateDept(DeptCreateDTO createDTO) {
        Dept dept = BeanCopyUtils.copyBean(createDTO, Dept.class);

        return deptMapper.updateById(dept) > 0;
    }


    public Boolean deleteDept(Long id) {
        return deptMapper.deleteById(id) > 0;
    }
}
