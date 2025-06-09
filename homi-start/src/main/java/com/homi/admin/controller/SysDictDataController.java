package com.homi.admin.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.annotation.Log;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dict.data.DictDataQueryDTO;
import com.homi.domain.dto.dict.data.SysDictDataCreateDTO;
import com.homi.domain.dto.dict.data.SysDictDataUpdateDTO;
import com.homi.domain.enums.common.BizOperateTypeEnum;
import com.homi.model.entity.SysDictData;
import com.homi.service.system.SysDictDataService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据表(SysDictData)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:43
 */

@RequestMapping("admin/sys/dict/data")
@RequiredArgsConstructor
public class SysDictDataController {
    /**
     * 服务对象
     */
    private final SysDictDataService sysDictDataService;

    /**
     * 分页查询字典数据项
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @SaCheckPermission("system:dict:data:query")
    public ResponseResult<Page<SysDictData>> selectAll(@Valid DictDataQueryDTO queryDTO) {
        return ResponseResult.ok(this.sysDictDataService.list(queryDTO));
    }

    /**
     * 通过字典主键查询数据项
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/get/{id}")
    public ResponseResult<SysDictData> selectOne(@PathVariable Long id) {
        return ResponseResult.ok(sysDictDataService.getDictDataById(id));
    }

    /**
     * 新增字典数据项
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @SaCheckPermission("system:dict:data:create")
    public ResponseResult<Long> insert(@Valid @RequestBody SysDictDataCreateDTO createDTO) {
        SysDictData sysDictData = BeanCopyUtils.copyBean(createDTO, SysDictData.class);
        return ResponseResult.ok(sysDictDataService.createDictData(sysDictData));
    }

    /**
     * 修改字典数据项
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @Log(title = "字典数据项管理", businessType = BizOperateTypeEnum.UPDATE)
    @PutMapping("update")
    @SaCheckPermission("system:dict:data:update")
    public ResponseResult<Long> update(@Valid @RequestBody SysDictDataUpdateDTO updateDTO) {
        SysDictData sysDictData = BeanCopyUtils.copyBean(updateDTO, SysDictData.class);
        return ResponseResult.ok(this.sysDictDataService.updateDictData(sysDictData));
    }

    /**
     * 删除字典数据项
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "字典数据项管理", businessType = BizOperateTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @SaCheckPermission("system:dict:data:delete")
    public ResponseResult<Boolean> delete(@RequestBody List<Long> idList) {
        return ResponseResult.ok(sysDictDataService.deleteByIds(idList));
    }
}

