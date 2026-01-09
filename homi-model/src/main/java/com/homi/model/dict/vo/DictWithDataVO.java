package com.homi.model.dict.vo;

import com.homi.model.dict.data.vo.DictDataVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "字典以及字典数据")
public class DictWithDataVO {

    private String dictCode;
    @Schema(description = "字典名称")
    private String dictName;

    private List<DictDataVO> dictDataList;
}
