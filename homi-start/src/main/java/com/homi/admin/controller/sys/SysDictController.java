package com.homi.admin.controller.sys;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.dto.dict.DictQueryDTO;
import com.homi.domain.dto.dict.DictCreateDTO;
import com.homi.domain.dto.dict.DictUpdateDTO;
import com.homi.domain.enums.common.ResponseCodeEnum;
import com.homi.domain.vo.dict.DictWithDataVO;
import com.homi.domain.vo.dict.DictVO;
import com.homi.exception.BizException;
import com.homi.dao.entity.Dict;
import com.homi.service.system.DictDataService;
import com.homi.service.system.DictService;
import com.homi.utils.BeanCopyUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 字典表(Dict)表控制层
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
    private final DictService dictService;

    private final DictDataService dictDataService;

    /**
     * 查询字典列表数据
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("list")
//    @SaCheckPermission("system:dict:query")
    public ResponseResult<List<DictVO>> list(DictQueryDTO queryDTO) {
        return ResponseResult.ok(dictService.list(queryDTO));
    }

    /**
     * 新增字典
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
    @SaCheckPermission("system:dict:create")
    public ResponseResult<Long> insert(@Valid @RequestBody DictCreateDTO createDTO) {
        Dict dict = BeanCopyUtils.copyBean(createDTO, Dict.class);
        return ResponseResult.ok(dictService.createDict(dict));
    }

    /**
     * 修改字典
     *
     * @param updateDTO 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @SaCheckPermission("system:dict:update")
    public ResponseResult<Long> update(@Valid @RequestBody DictUpdateDTO updateDTO) {
        Dict dict = BeanCopyUtils.copyBean(updateDTO, Dict.class);
        return ResponseResult.ok(this.dictService.updateDict(dict));
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
        long count = dictDataService.getCountByDictId(id);
        if (count > 0) {
            throw new BizException(ResponseCodeEnum.FAIL.getCode(), "该字典下存在数据项，无法删除");
        }
        return ResponseResult.ok(this.dictService.removeDictById(id));
    }

    @GetMapping("/getAllDictAndData")
    public ResponseResult<List<DictWithDataVO>> getDict() {
        return ResponseResult.ok(dictService.listAllDictAndData());
    }
}

