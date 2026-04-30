package com.homi.model.common.dto;

import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "文件附件分组DTO")
public class FileAttachGroupDTO {
    @Schema(description = "业务子类型 code", implementation = FileAttachSubtypeEnum.class)
    private String bizSubtype;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;
}
