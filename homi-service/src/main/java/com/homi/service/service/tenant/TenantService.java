package com.homi.service.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.json.JSONUtil;
import com.homi.common.lib.enums.StatusEnum;
import com.homi.common.lib.enums.file.FileAttachBizTypeEnum;
import com.homi.common.lib.enums.tenant.TenantContractStatusEnum;
import com.homi.common.lib.enums.tenant.TenantTypeEnum;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.dao.entity.Tenant;
import com.homi.model.dao.entity.TenantCompany;
import com.homi.model.dao.entity.TenantContract;
import com.homi.model.dao.repo.*;
import com.homi.model.dto.tenant.*;
import com.homi.model.vo.tenant.TenantCompanyVO;
import com.homi.model.vo.tenant.TenantContractListVO;
import com.homi.model.vo.tenant.TenantPersonalVO;
import com.homi.model.vo.tenant.TenantTotalItemVO;
import com.homi.service.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租客
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/9
 */

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepo tenantRepo;
    private final TenantCompanyRepo tenantCompanyRepo;
    private final TenantContractRepo tenantContractRepo;
    private final FileAttachRepo fileAttachRepo;
    private final RoomService roomService;

    /**
     * 获取租客列表
     *
     * @param query 查询参数
     * @return 租客列表
     */
    public PageVO<TenantContractListVO> getTenantList(TenantQueryDTO query) {
        PageVO<TenantContractListVO> tenantContractListVOPageVO = tenantContractRepo.queryTenantContractList(query);

        tenantContractListVOPageVO.getList().forEach(tenantContractListVO -> {
            TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, tenantContractListVO.getTenantType());
            if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
                TenantPersonalVO tenantPersonalVO = tenantRepo.getTenantById(tenantContractListVO.getTenantId());
                tenantContractListVO.setTenantPersonal(tenantPersonalVO);
            } else {
                TenantCompanyVO tenantCompanyVO = tenantCompanyRepo.getTenantCompanyById(tenantContractListVO.getTenantId());
                tenantContractListVO.setTenantCompany(tenantCompanyVO);
            }

            tenantContractListVO.setRoomList(roomService.getRoomListByRoomIds(JSONUtil.toList(tenantContractListVO.getRoomIds(), Long.class)));
        });

        return tenantContractListVOPageVO;
    }

    /**
     * 添加租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/13 17:13
     *
     * @param createDTO 创建租客信息
     * @return java.lang.Long
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTenant(TenantCreateDTO createDTO) {
        TenantTypeEnum tenantTypeEnum = EnumUtil.getBy(TenantTypeEnum::getCode, createDTO.getContract().getTenantType());
        if (tenantTypeEnum == null) {
            throw new IllegalArgumentException("租户类型不存在");
        }

        Triple<Long, String, String> addedTenant;
        if (tenantTypeEnum == TenantTypeEnum.PERSONAL) {
            createDTO.getTenant().setCreateBy(createDTO.getCreateBy());
            // 保存个人租客
            addedTenant = addTenantPersonal(createDTO.getTenant());
        } else {
            createDTO.getTenantCompany().setCreateBy(createDTO.getCreateBy());
            // 保存企业租客
            addedTenant = addTenantEnterprise(createDTO.getTenantCompany());
        }

        createDTO.getContract().setTenantId(addedTenant.getLeft());
        createDTO.getContract().setTenantName(addedTenant.getMiddle());
        createDTO.getContract().setTenantPhone(addedTenant.getRight());

        createDTO.getContract().setCreateBy(createDTO.getCreateBy());
        addTenantContract(createDTO.getContract());

        return addedTenant.getLeft();
    }

    /**
     * 添加租客合同
     *
     * @param contract 合同信息
     */
    private void addTenantContract(ContractDTO contract) {
        TenantContract tenantContract = new TenantContract();
        BeanUtils.copyProperties(contract, tenantContract);

        tenantContract.setRoomIds(JSONUtil.toJsonStr(contract.getRoomIds()));

        tenantContract.setStatus(TenantContractStatusEnum.TO_SIGN.getCode());
        tenantContract.setCreateTime(DateUtil.date());
        tenantContractRepo.save(tenantContract);
    }

    /**
     * 添加企业租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 16:51
     *
     * @param tenantCompany 参数说明
     * @return java.lang.Long
     */
    private Triple<Long, String, String> addTenantEnterprise(TenantCompanyDTO tenantCompany) {
        TenantCompany tenantCompanyEntity = new TenantCompany();
        BeanUtils.copyProperties(tenantCompany, tenantCompanyEntity);

        tenantCompanyEntity.setTags(JSONUtil.toJsonStr(tenantCompany.getTags()));

        tenantCompanyEntity.setStatus(StatusEnum.ACTIVE.getValue());
        tenantCompanyEntity.setCreateTime(DateUtil.date());
        tenantCompanyRepo.save(tenantCompanyEntity);

        return Triple.of(tenantCompanyEntity.getId(), tenantCompanyEntity.getCompanyName(), tenantCompanyEntity.getContactPhone());
    }

    /**
     * 添加个人租客
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 16:51
     *
     * @param tenantDTO 参数说明
     * @return java.lang.Long
     */
    private Triple<Long, String, String> addTenantPersonal(TenantDTO tenantDTO) {
        Tenant tenant = new Tenant();
        Tenant tenantExist = tenantRepo.getTenantByIdNo(tenantDTO.getIdNo());
        if (tenantExist != null) {
            tenantExist.setUpdateBy(tenantDTO.getCreateBy());
            tenantExist.setUpdateTime(DateUtil.date());

            tenant = tenantExist;
        } else {
            tenant.setCreateBy(tenantDTO.getCreateBy());
            tenant.setCreateTime(DateUtil.date());
        }

        BeanUtils.copyProperties(tenantDTO, tenant);
        tenant.setTags(JSONUtil.toJsonStr(tenantDTO.getTags()));
        tenant.setStatus(StatusEnum.ACTIVE.getValue());
        tenantRepo.save(tenant);

        // 保存租客身份证反面
        if (CollUtil.isNotEmpty(tenantDTO.getIdCardBackList())) {
            fileAttachRepo.addFileAttachBatch(tenant.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_BACK.getBizType(), tenantDTO.getIdCardBackList());
        }

        // 保存租客身份证正面
        if (CollUtil.isNotEmpty(tenantDTO.getIdCardFrontList())) {
            fileAttachRepo.addFileAttachBatch(tenant.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(), tenantDTO.getIdCardFrontList());
        }

        // 保存租客手持照片
        if (CollUtil.isNotEmpty(tenantDTO.getIdCardInHandList())) {
            fileAttachRepo.addFileAttachBatch(tenant.getId(), FileAttachBizTypeEnum.TENANT_ID_CARD_FRONT.getBizType(), tenantDTO.getIdCardInHandList());
        }

        // 保存租客其他照片
        if (CollUtil.isNotEmpty(tenantDTO.getOtherImageList())) {
            fileAttachRepo.addFileAttachBatch(tenant.getId(), FileAttachBizTypeEnum.TENANT_OTHER_IMAGE.getBizType(), tenantDTO.getOtherImageList());
        }

        return Triple.of(tenant.getId(), tenant.getName(), tenant.getPhone());
    }


    /**
     * 获取租客状态总数
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/14 04:00
     *
     * @param query 参数说明
     * @return java.util.List<com.homi.model.vo.tenant.TenantTotalItemVO>
     */
    public List<TenantTotalItemVO> getTenantStatusTotal(TenantQueryDTO query) {
        Map<Integer, TenantTotalItemVO> result = initTenantTotalItemMap();

        List<TenantTotalItemVO> statusTotal = tenantContractRepo.getBaseMapper().getStatusTotal(query);
        statusTotal.forEach(tenantTotalItemVO -> {
            TenantTotalItemVO orDefault = result.getOrDefault(tenantTotalItemVO.getStatus(), tenantTotalItemVO);
            orDefault.setTotal(tenantTotalItemVO.getTotal());
        });

        return result.values().stream().toList();
    }

    /**
     * 获取房间状态枚举映射
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/8/7 19:12
     *
     * @return java.util.Map<java.lang.Integer, com.homi.domain.vo.room.RoomTotalItemVO>
     */
    private @NotNull Map<Integer, TenantTotalItemVO> initTenantTotalItemMap() {
        Map<Integer, TenantTotalItemVO> result = new HashMap<>();
        TenantContractStatusEnum[] values = TenantContractStatusEnum.values();
        for (TenantContractStatusEnum contractStatusEnum : values) {
            TenantTotalItemVO tenantTotalItemVO = new TenantTotalItemVO();
            tenantTotalItemVO.setStatus(contractStatusEnum.getCode());
            tenantTotalItemVO.setStatusName(contractStatusEnum.getName());
            tenantTotalItemVO.setTotal(0);
            result.put(contractStatusEnum.getCode(), tenantTotalItemVO);
        }
        return result;
    }
}
