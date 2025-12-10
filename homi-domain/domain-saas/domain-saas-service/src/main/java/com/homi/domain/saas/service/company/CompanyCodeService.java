package com.homi.domain.saas.service.company;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.homi.model.dao.repo.CompanyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 应用于 domix-platform
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/4
 */

@Service
@RequiredArgsConstructor
public class CompanyCodeService {
    private final CompanyRepo companyRepo;

    public String generateCompanyCode(String companyName) {
        String base = extractCode(companyName);  // 生成缩写，比如 BJWL
        String code = base;

        int index = 1;
        while (companyRepo.existsByCode(code)) {
            code = base + index;
            index++;
        }

        return code;
    }

    private String extractCode(String companyName) {
        // 简单拼音首字母提取
        StringBuilder sb = new StringBuilder();
        for (char c : companyName.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                String firstLetter = PinyinUtil.getFirstLetter(String.valueOf(c), "").toUpperCase(Locale.ROOT);
                sb.append(firstLetter);
            }
        }

        String code = sb.toString();
        if (code.length() > 4) {
            code = code.substring(0, 4);
        } else if (code.length() < 4) {
            code = (code + "XXXX").substring(0, 4);
        }

        return code;
    }
}

