package com.homi.saas.web.controller.sys;


import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.vo.PageVO;
import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dto.dict.DictCodeDTO;
import com.homi.model.dto.dict.data.DictDataCreateDTO;
import com.homi.model.dto.dict.data.DictDataQueryDTO;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseCodeEnum;
import com.homi.model.vo.dict.DictWithDataVO;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.entity.DictData;
import com.homi.service.service.system.DictDataService;
import com.homi.service.service.system.DictService;
import com.homi.common.lib.utils.BeanCopyUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 字典数据表(DictData)表控制层
 *
 * @author sjh
 * @since 2024-04-25 10:36:43
 */

@RequestMapping("admin/sys/dict/data")
@RequiredArgsConstructor
@RestController
public class DictDataController {
    /**
     * 服务对象
     */
    private final DictDataService dictDataService;
    private final DictService dictService;

    /**
     * 分页查询字典数据项
     *
     * @param queryDTO 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
//    @SaCheckPermission("system:dict:data:query")
    public ResponseResult<PageVO<DictData>> list(@Valid DictDataQueryDTO queryDTO) {
        return ResponseResult.ok(dictDataService.list(queryDTO));
    }

    /**
     * 通过字典主键查询数据项
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/get/{id}")
    public ResponseResult<DictData> getById(@PathVariable Long id) {
        return ResponseResult.ok(dictDataService.getDictDataById(id));
    }

    @PostMapping("/listByDictCode")
    @Schema(description = "通过字典编号查询数据项")
    public ResponseResult<List<DictData>> listByDictCode(@RequestBody DictCodeDTO dictCodeQuery) {
        Dict dictByCode = dictService.getDictByCode(dictCodeQuery.getDictCode());
        if (Objects.isNull(dictByCode)) {
            return ResponseResult.fail(ResponseCodeEnum.DICT_NOT_FOUND);
        }

        return ResponseResult.ok(dictDataService.listByDictId(dictByCode.getId()));
    }

    @GetMapping("/listByParentCode")
    @Schema(description = "通过父节点编号查询数据项，使用二级结构返回，children 字段包含子项")
    public ResponseResult<List<DictWithDataVO>> listByParentCode(@RequestParam("dictCode") String dictCode) {
        Dict dictByCode = dictService.getDictByCode(dictCode);

        return ResponseResult.ok(dictDataService.listByParentCode(dictByCode.getId()));
    }

    /**
     * 新增字典数据项
     *
     * @param createDTO 实体对象
     * @return 新增结果
     */
    @PostMapping("/create")
//    @SaCheckPermission("system:dict:data:create")
    public ResponseResult<Long> create(@Valid @RequestBody DictDataCreateDTO createDTO) {
        DictData dictData = BeanCopyUtils.copyBean(createDTO, DictData.class);
        if (Objects.isNull(createDTO.getId())) {
            return ResponseResult.ok(dictDataService.createDictData(dictData));
        } else {
            return ResponseResult.ok(dictDataService.updateDictData(dictData));
        }
    }

    /**
     * 删除字典数据项
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "字典数据项管理", operationType = OperationTypeEnum.DELETE)
    @PostMapping("/delete")
//    @SaCheckPermission("system:dict:data:delete")
    public ResponseResult<Boolean> delete(@RequestBody List<Long> idList) {
        return ResponseResult.ok(dictDataService.deleteByIds(idList));
    }
}

