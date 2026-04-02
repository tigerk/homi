package com.homi.external.sign;

import com.homi.common.lib.exception.BizException;
import com.homi.external.sign.config.SignProperties;
import com.homi.external.sign.dto.SignAccountRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/4/2
 */

@Service
@RequiredArgsConstructor
public class SignService {

    private final List<SignProvider> signProviders;
    private final SignProperties signProperties;

    public String registerAccount(SignAccountRegisterDTO request) {
        return registerAccount(signProperties.getDefaultSource(), request);
    }

    public String registerAccount(Integer source, SignAccountRegisterDTO request) {
        SignProvider signProvider = getProvider(source);
        return signProvider.registerAccount(request);
    }

    public SignProvider getProvider(Integer source) {
        if (source == null) {
            throw new BizException("电子签供应商不能为空");
        }

        Map<Integer, SignProvider> providerMap = signProviders.stream().collect(Collectors.toMap(SignProvider::getSource, Function.identity(), (left, right) -> left));
        SignProvider signProvider = providerMap.get(source);
        if (signProvider == null) {
            throw new BizException("暂不支持该电子签供应商，source=" + source);
        }
        return signProvider;
    }
}
