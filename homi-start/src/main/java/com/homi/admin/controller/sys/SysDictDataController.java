package com.homi.admin.controller.sys;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.homi.annotation.Log;
import com.homi.domain.base.PageVO;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dict.data.DictDataQueryDTO;
import com.homi.domain.dto.dict.data.SysDictDataCreateDTO;
import com.homi.domain.dto.dict.data.SysDictDataUpdateDTO;
import com.homi.domain.enums.common.OperationTypeEnum;
import com.homi.model.entity.SysDictData;
import com.homi.service.system.SysDictDataService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 字典数据表(SysDictData)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:43
 */

@RequestMapping("admin/sys/dict/data")
@RequiredArgsConstructor
@RestController
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
//    @SaCheckPermission("system:dict:data:query")
    public ResponseResult<PageVO<SysDictData>> list(@Valid DictDataQueryDTO queryDTO) {
        return ResponseResult.ok(sysDictDataService.list(queryDTO));
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
//    @SaCheckPermission("system:dict:data:create")
    public ResponseResult<Long> create(@Valid @RequestBody SysDictDataCreateDTO createDTO) {
        SysDictData sysDictData = BeanCopyUtils.copyBean(createDTO, SysDictData.class);
        if(Objects.isNull(createDTO.getId())) {
            return ResponseResult.ok(sysDictDataService.createDictData(sysDictData));
        } else {
            return ResponseResult.ok(sysDictDataService.updateDictData(sysDictData));
        }
    }

    /**
     * 删除字典数据项
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "字典数据项管理", operationType = OperationTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @SaCheckPermission("system:dict:data:delete")
    public ResponseResult<Boolean> delete(@RequestBody List<Long> idList) {
        return ResponseResult.ok(sysDictDataService.deleteByIds(idList));
    }
}

