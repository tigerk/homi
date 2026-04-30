package com.homi.service.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.homi.common.lib.enums.file.FileAttachSubtypeEnum;
import com.homi.model.common.dto.FileAttachGroupDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件附件分组解析工具。
 * <p>
 * 不同业务 DTO 字段名一致但类型不同，统一在这里解析成 file_attach 可直接落库的 subtype -> urls 结构。
 */
public final class FileAttachGroupResolver {
    private FileAttachGroupResolver() {
    }

    public static Map<String, List<String>> resolveSubtypeGroups(List<FileAttachGroupDTO> attachmentGroupList, List<String> attachmentUrls) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(attachmentGroupList)) {
            for (FileAttachGroupDTO group : attachmentGroupList) {
                if (group == null) {
                    continue;
                }
                String bizSubtype = FileAttachSubtypeEnum.normalizeCode(group.getBizSubtype());
                List<String> urls = sanitizeAttachmentUrls(group.getAttachmentUrls());
                if (CollUtil.isNotEmpty(urls)) {
                    result.computeIfAbsent(bizSubtype, key -> new ArrayList<>()).addAll(urls);
                }
            }
            return result;
        }
        List<String> urls = sanitizeAttachmentUrls(attachmentUrls);
        if (CollUtil.isNotEmpty(urls)) {
            result.put(FileAttachSubtypeEnum.OTHER.getCode(), urls);
        }
        return result;
    }

    private static List<String> sanitizeAttachmentUrls(List<String> attachmentUrls) {
        return Objects.requireNonNullElse(attachmentUrls, List.<String>of())
            .stream()
            .map(CharSequenceUtil::trim)
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toList();
    }
}
