package com.homi.admin.controller.sys;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dict.DictQueryDTO;
import com.homi.domain.dto.dict.DictWithDataVO;
import com.homi.domain.dto.dict.SysDictCreateDTO;
import com.homi.domain.dto.dict.SysDictUpdateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.exception.BizException;
import com.homi.model.entity.SysDict;
import com.homi.service.system.SysDictDataService;
import com.homi.service.system.SysDictService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 字典表(SysDict)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:32
 */
@RequestMapping("admin/sys/dict")
@RequiredArgsConstructor
@RestController
public class SysDictController {
    /**
     * 服务对象
     */
    private final SysDictService sysDictService;

    private final SysDictDataService sysDictDataService;

    /**
     * 查询字典列表数据
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("list")
//    @SaCheckPermission("system:dict:query")
    public ResponseResult<List<SysDict>> list(DictQueryDTO queryDTO) {
        return ResponseResult.ok(sysDictService.list(queryDTO));
    }

    /**
     * 新增字典
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @SaCheckPermission("system:dict:create")
    public ResponseResult<Long> insert(@Valid @RequestBody SysDictCreateDTO createDTO) {
        SysDict sysDict = BeanCopyUtils.copyBean(createDTO, SysDict.class);
        return ResponseResult.ok(sysDictService.createDict(sysDict));
    }

    /**
     * 修改字典
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @SaCheckPermission("system:dict:update")
    public ResponseResult<Long> update(@Valid @RequestBody SysDictUpdateDTO updateDTO) {
        SysDict sysDict = BeanCopyUtils.copyBean(updateDTO, SysDict.class);
        return ResponseResult.ok(this.sysDictService.updateDict(sysDict));
    }

    /**
     * 删除字典
     *
     * @param id 主键
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:dict:delete")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        long count = sysDictDataService.getCountByDictId(id);
        if (count > 0) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该字典下存在数据项，无法删除");
        }
        return ResponseResult.ok(this.sysDictService.removeDictById(id));
    }

    @GetMapping("/getAllDictAndData")
    public ResponseResult<List<DictWithDataVO>> getDict() {
        return ResponseResult.ok(sysDictService.listAllDictAndData());
    }
}

