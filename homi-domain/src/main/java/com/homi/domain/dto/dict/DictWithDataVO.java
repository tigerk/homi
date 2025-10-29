package com.homi.domain.dto.dict;

import com.homi.domain.dto.dict.data.DictDataVO;
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
