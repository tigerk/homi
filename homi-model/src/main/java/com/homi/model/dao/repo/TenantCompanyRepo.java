package com.homi.model.dao.repo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.mapper.TenantCompanyMapper;
import com.homi.model.tenant.vo.TenantCompanyVO;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 企业租客信息表 服务实现类
 * </p>
 *
 * @author tk
 * @since 2025-11-19
 */
@Service
public class TenantCompanyRepo extends ServiceImpl<TenantCompanyMapper, TenantCompany> {

    public TenantCompanyVO getTenantCompanyById(Long tenantId) {
        TenantCompany one = getOne(new LambdaQueryWrapper<TenantCompany>().eq(TenantCompany::getId, tenantId));

        TenantCompanyVO tenantCompanyVO = BeanCopyUtils.copyBean(one, TenantCompanyVO.class);

        assert tenantCompanyVO != null;
        tenantCompanyVO.setTags(JSONUtil.toList(one.getTags(), String.class));

        return tenantCompanyVO;
    }
}
