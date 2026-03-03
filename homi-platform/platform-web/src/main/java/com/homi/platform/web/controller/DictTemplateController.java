package com.homi.platform.web.controller;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.dao.entity.DictDataTemplate;
import com.homi.model.dict.template.dto.DictDataTemplateQueryDTO;
import com.homi.model.dict.template.dto.DictDataTemplateSaveDTO;
import com.homi.model.dict.template.dto.DictTemplateIdDTO;
import com.homi.model.dict.template.dto.DictTemplateSaveDTO;
import com.homi.model.dict.template.vo.DictTemplateListVO;
import com.homi.model.dict.template.vo.DictTemplateSyncVO;
import com.homi.service.service.sys.DictTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/dict/template")
public class DictTemplateController {
    private final DictTemplateService dictTemplateService;

    @PostMapping("/list")
    public ResponseResult<List<DictTemplateListVO>> listTemplateTree() {
        return ResponseResult.ok(dictTemplateService.listTemplateTree());
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> saveTemplate(@RequestBody DictTemplateSaveDTO saveDTO) {
        return ResponseResult.ok(dictTemplateService.saveTemplate(saveDTO));
    }

    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteTemplate(@RequestBody DictTemplateIdDTO idDTO) {
        return ResponseResult.ok(dictTemplateService.deleteTemplate(idDTO.getId()));
    }

    @PostMapping("/data/list")
    public ResponseResult<List<DictDataTemplate>> listDataTemplate(@RequestBody(required = false) DictDataTemplateQueryDTO queryDTO) {
        return ResponseResult.ok(dictTemplateService.listDataTemplate(queryDTO));
    }

    @PostMapping("/data/create")
    public ResponseResult<Boolean> saveDataTemplate(@RequestBody DictDataTemplateSaveDTO saveDTO) {
        return ResponseResult.ok(dictTemplateService.saveDataTemplate(saveDTO));
    }

    @PostMapping("/data/delete")
    public ResponseResult<Boolean> deleteDataTemplate(@RequestBody DictTemplateIdDTO idDTO) {
        return ResponseResult.ok(dictTemplateService.deleteDataTemplate(idDTO.getId()));
    }

    @PostMapping("/sync")
    public ResponseResult<DictTemplateSyncVO> syncAllCompanyDict() {
        return ResponseResult.ok(dictTemplateService.syncAllCompanyDict());
    }
}

