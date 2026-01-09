package com.homi.platform.web.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homi.model.dao.entity.Dict;
import com.homi.model.dao.entity.DictData;
import com.homi.model.dao.repo.DictDataRepo;
import com.homi.model.dao.repo.DictRepo;
import com.homi.model.company.dto.init.InitDictDTO;
import com.homi.model.company.dto.init.InitDictDataDTO;
import com.homi.service.service.company.CompanyInitService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */

@Slf4j
@RequestMapping("/platform/test")
@RestController
@RequiredArgsConstructor
public class PlatformTestController {
    private final DictRepo dictRepo;
    private final DictDataRepo dictDataRepo;

    private final CompanyInitService companyInitService;

    @Operation(summary = "获取公司字典初始化数据")
    @GetMapping("/company/dict/get")
    public String getCompanyDictInit(@RequestParam("companyId") Long companyId) {
        List<Dict> parentList = dictRepo.list(new LambdaQueryWrapper<Dict>()
                .eq(Dict::getCompanyId, companyId)
                .eq(Dict::getParentId, 0));

        List<InitDictDTO> list = new ArrayList<>();

        parentList.forEach(parent -> {
            InitDictDTO initDictDTO = new InitDictDTO();
            initDictDTO.setDictCode(parent.getDictCode());
            initDictDTO.setDictName(parent.getDictName());
            initDictDTO.setSortOrder(parent.getSortOrder());
            initDictDTO.setHidden(parent.getHidden());
            // 使用 ArrayList 替换 List.of() 创建的不可变列表
            initDictDTO.setChildren(new ArrayList<>());

            List<Dict> childList = dictRepo.list(new LambdaQueryWrapper<Dict>()
                    .eq(Dict::getCompanyId, companyId)
                    .eq(Dict::getParentId, parent.getId()));

            childList.forEach(child -> {
                InitDictDTO childInitDictDTO = new InitDictDTO();
                childInitDictDTO.setDictCode(child.getDictCode());
                childInitDictDTO.setDictName(child.getDictName());
                childInitDictDTO.setSortOrder(child.getSortOrder());
                childInitDictDTO.setHidden(child.getHidden());
                // 同样地，这里也使用 ArrayList
                childInitDictDTO.setDictDataList(new ArrayList<>());
                initDictDTO.getChildren().add(childInitDictDTO);

                List<DictData> dictDataList = dictDataRepo.list(new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getDictId, child.getId()));
                dictDataList.forEach(dictData -> {
                    InitDictDataDTO dictDataDTO = new InitDictDataDTO();
                    dictDataDTO.setName(dictData.getName());
                    dictDataDTO.setValue(dictData.getValue());
                    dictDataDTO.setSortOrder(dictData.getSortOrder());
                    dictDataDTO.setColor(dictData.getColor());
                    childInitDictDTO.getDictDataList().add(dictDataDTO);
                });
            });

            list.add(initDictDTO);
        });

        companyInitService.saveCompanyDictInit(list);

        return JSONUtil.toJsonStr(list);
    }
}
